package ir.logicbase.mockfit.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val executors = AppExecutors()
    private val dataSource = RemoteDataSource(this)
    private var loadingApi = false
    private var currentImageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        button_loadRemote.setOnClickListener {
            if (!loadingApi) {
                dataSource.mockFitEnable = false
                loadApiInfo()
            }
        }

        button_loadMock.setOnClickListener {
            if (!loadingApi) {
                dataSource.mockFitEnable = true
                loadApiInfo()
            }
        }
    }

    private fun loadApiInfo() {
        loadingApi = true
        textView_author.text = "Loading..."
        if (currentImageIndex < 19) {
            currentImageIndex++
        } else {
            currentImageIndex = 0
        }
        executors.networkIO().execute {
            dataSource.api().getListOfPicsums(2, 20).enqueue(object : Callback<List<Picsum>> {
                override fun onResponse(call: Call<List<Picsum>>, response: Response<List<Picsum>>) {
                    loadingApi = false
                    executors.mainThread().execute {
                        val picsum = response.body()?.getOrNull(currentImageIndex) ?: return@execute
                        Glide.with(this@MainActivity).load(picsum.downloadUrl).into(imageView)
                        textView_author.text = "Image author : ${picsum.author}"
                    }
                }

                override fun onFailure(call: Call<List<Picsum>>, t: Throwable) {
                    loadingApi = false
                    executors.mainThread().execute {
                        textView_author.text = "Press one of the buttons below"
                        Toast.makeText(this@MainActivity, "Error loading data", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}