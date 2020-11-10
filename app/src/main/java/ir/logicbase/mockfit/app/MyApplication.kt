package ir.logicbase.mockfit.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class MyApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}