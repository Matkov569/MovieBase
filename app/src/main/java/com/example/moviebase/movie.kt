package com.example.moviebase

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
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

    lateinit var lSM: loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_movie, container, false);

        lSM = loadingScreenManager(activity,view);

        // Inflate the layout for this fragment
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val viewModel by activityViewModels<ViewModel>();


        view.findViewById<ImageButton>(R.id.retBtn).setOnClickListener {
            //sprawdź w viewmodelu gdzie wrócić i wróć tam
            findNavController().navigate(R.id.action_movie_to_searchResult)
        }

        runBlocking {
            if(viewModel.imbdID!="")
                networkCircle(viewModel.imbdID);
            lSM.timer();
        }

    }

    private suspend fun downloadData(movieID:String){
        var json = "";
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val apiID:String = secrets().moviesAPIkey;
        try {
            runBlocking {

                json = URL("https://www.omdbapi.com/?apikey=$apiID&i=$movieID&plot=full").readText()

                //val viewModel by activityViewModels<ViewModel>();

                var obj = JSONObject(json)

                var title = obj.getString("Title");
                var year = obj.optString("Year","N/A");
                var director = obj.optString("Director","N/A");
                var actors = obj.optString("Actors","N/A");
                var plot = obj.optString("Plot","N/A");
                var posterURL = obj.optString("Poster","N/A");

                activity?.runOnUiThread {
                    view?.findViewById<TextView>(R.id.barTitle)?.text = title;
                    view?.findViewById<TextView>(R.id.movieTitle)?.text = title;
                    view?.findViewById<TextView>(R.id.movieYear)?.text = year;
                    view?.findViewById<TextView>(R.id.movieDirector)?.text = director;
                    view?.findViewById<TextView>(R.id.movieActors)?.text = actors;
                    view?.findViewById<TextView>(R.id.moviePlot)?.text = plot;

                    if(posterURL != "N/A") {
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
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                launch {
                    lSM.hideLoading()
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

    suspend fun networkCircle(movieID: String){
        if(!networkTest()){
            Timer().schedule(1000){
                runBlocking {networkCircle(movieID)};
            }
        }
        else
            downloadData(movieID);
    }

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
}