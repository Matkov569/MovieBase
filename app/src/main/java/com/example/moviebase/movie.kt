package com.example.moviebase

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.util.concurrent.Executors

class movie : Fragment() {

    lateinit var lSM: loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_movie, container, false);

        val viewModel by activityViewModels<ViewModel>();
        lSM = loadingScreenManager(activity,view,context);

        if (!lSM.networkTest()) lSM.timer()
        println(viewModel.imbdID);


        // Inflate the layout for this fragment
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by activityViewModels<ViewModel>();

        if(viewModel.imbdID!="")
            lSM.networkCircle({runBlocking{downloadData(viewModel.imbdID)}});

        view.findViewById<ImageButton>(R.id.retBtn).setOnClickListener {
            if(viewModel.returnTo=="searchResult")
                findNavController().navigate(R.id.action_movie_to_searchResult)
            else
                findNavController().navigate(R.id.action_movie_to_main)
        }

        view.findViewById<ImageButton>(R.id.addToSeen).setOnClickListener {
            //dodawanie informacji do bazy
        }

    }

    private suspend fun downloadData(movieID:String){
        println("downloading")
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
            builder.setMessage("Nie otrzymano danych od serwera. Spróbuj ponownie.");
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