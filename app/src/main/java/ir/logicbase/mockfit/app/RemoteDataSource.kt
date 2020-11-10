@file:Suppress("HardCodedStringLiteral")

package ir.logicbase.mockfit.app

import android.content.Context
import android.util.Log
import ir.logicbase.mockfit.MockFitConfig.REQUEST_TO_JSON
import ir.logicbase.mockfit.MockFitInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteDataSource(private val context: Context, var mockFitEnable: Boolean = true) {

    fun api(): Api {
        val mockFitInterceptor = provideMockFitInterceptor(context)
        val okHttpClient = provideOkHttpClient(mockFitInterceptor)
        val retrofit = provideRetrofit(okHttpClient)
        return retrofit.create(Api::class.java)
    }

    private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    private fun provideMockFitInterceptor(context: Context) = MockFitInterceptor(
        bodyFactory = { input -> context.resources.assets.open(input) },
        logger = { tag, message -> Log.d(tag, message) },
        baseUrl = BASE_URL,
        requestPathToJsonMap = REQUEST_TO_JSON,
        mockFilesPath = MOCK_FILES_PATH,
        mockFitEnable = mockFitEnable,
        apiEnableMock = true
    )

    private fun provideOkHttpClient(mockFitInterceptor: MockFitInterceptor) = OkHttpClient.Builder()
        .addInterceptor(mockFitInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://picsum.photos/v2/"
        private const val MOCK_FILES_PATH = "mock_json"
        private const val CONNECT_TIMEOUT = 20L
        private const val WRITE_TIMEOUT = 20L
        private const val READ_TIMEOUT = 30L
    }
}