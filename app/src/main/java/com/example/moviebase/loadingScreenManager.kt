package com.example.moviebase

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.concurrent.schedule

class loadingScreenManager(var activity: Activity?, var view: View?, var context: Context?) {
    // hiding loading screen
    fun hideLoading(){
        activity?.runOnUiThread {
            view?.findViewById<ConstraintLayout>(R.id.loadingScreen)?.visibility = View.GONE;
            view?.findViewById<TextView>(R.id.alertText)?.visibility = View.INVISIBLE;
        }
    }

    // showing loading screen
    fun showLoading(){
        activity?.runOnUiThread {
            view?.findViewById<ConstraintLayout>(R.id.loadingScreen)?.visibility = View.VISIBLE;
            timer();
        }
    }

    // showing alert if not ready in 5s
    fun timer() {
        Timer().schedule(5000) {
            activity?.runOnUiThread {
                view?.findViewById<TextView>(R.id.alertText)?.visibility = View.VISIBLE;
            }
        }
    }

    //checking internet connection
    fun networkTest():Boolean{
        var cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val activeNetwork = cm.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                cm.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    fun networkCircle(function: (Any?) -> Unit, parameter: Any? = null){
        if(!networkTest()){
            Timer().schedule(1000){
                runBlocking {networkCircle(function,parameter)};
            }
        }
        else {
            activity?.runOnUiThread {
                function(parameter);
            }

        }
    }
}