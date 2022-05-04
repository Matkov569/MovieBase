package com.example.moviebase

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*
import kotlin.concurrent.schedule

class loadingScreenManager(var activity: Activity?, var view: View?) {
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
}