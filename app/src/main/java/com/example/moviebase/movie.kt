package com.example.moviebase

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.FileNotFoundException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.schedule

class movie : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer();
        runBlocking {
            // imbd ID for witcher - just for tests
            downloadData("tt5180504");
        }

    }

    // showing alert if not ready in 5s
    private fun timer() {
        Timer().schedule(5000) {
            activity?.runOnUiThread {
                view?.findViewById<TextView>(R.id.alertText)?.visibility = View.VISIBLE;
            }
        }
    }

    // hiding loading screen
    private fun hideLoading(){
        activity?.runOnUiThread {
            view?.findViewById<ConstraintLayout>(R.id.loadingScreen)?.visibility = View.GONE;
        }
    }

    private suspend fun downloadData(movieID:String){
        var json = "";
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val apiID:String = secrets().moviesAPIkey;
        println("https://www.omdbapi.com/?apikey=$apiID&i=$movieID");
        try {
            runBlocking {

                json = URL("https://www.omdbapi.com/?apikey=$apiID&i=$movieID").readText()

                //val viewModel by activityViewModels<ViewModel>();

                var obj = JSONObject(json)

                var title = obj.getString("Title");
                var year = obj.getString("Year");
                var director = obj.getString("Director");
                var actors = obj.getString("Actors");
                var plot = obj.getString("Plot");
                var posterURL = obj.getString("Poster");

                activity?.runOnUiThread {
                    view?.findViewById<TextView>(R.id.barTitle)?.text = title;
                    view?.findViewById<TextView>(R.id.movieTitle)?.text = title;
                    view?.findViewById<TextView>(R.id.movieYear)?.text = year;
                    view?.findViewById<TextView>(R.id.movieDirector)?.text = director;
                    view?.findViewById<TextView>(R.id.movieActors)?.text = actors;
                    view?.findViewById<TextView>(R.id.moviePlot)?.text = plot;

                    val icon = view?.findViewById<ImageView>(R.id.moviePoster);
                    val executor = Executors.newSingleThreadExecutor()
                    val handler = Handler(Looper.getMainLooper())

                    var image: Bitmap? = null

                    executor.execute {
                        val imageURL = posterURL
                        try {
                            val `in` = URL(imageURL).openStream()
                            image = BitmapFactory.decodeStream(`in`)
                            handler.post {
                                icon?.setImageBitmap(image)
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch {
                    hideLoading()
                }
            }
        }
        catch (e: FileNotFoundException){
            var builder = AlertDialog.Builder(context);
            builder.setTitle("Brak danych");
            builder.setMessage("Dane pogodowe dla podanej lokalizacji są niedostępne. Spróbuj ustawić inną lokalizację, lub sprawdź spis dostępnych lokalizacji na stronie OpenWeatherMap.");
            builder.setPositiveButton("Ok"){ dialog, which ->
            }
            builder.show();
        }
        catch (e: Exception){
            println("JSON EXCEPTION")
            println(e.toString())
        }
    }
}