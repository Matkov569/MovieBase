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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.util.concurrent.Executors

class movie : Fragment() {

    lateinit var lSM: loadingScreenManager;
    lateinit var helpView:View;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_movie, container, false);

        val viewModel by activityViewModels<ViewModel>();
        lSM = loadingScreenManager(activity,view,context);
        helpView=view;
        if(viewModel.imbdID!="")
            lSM.networkCircle({downloadData(viewModel.imbdID)});

        if (!lSM.networkTest()) lSM.timer()

        // Inflate the layout for this fragment
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by activityViewModels<ViewModel>();

        view.findViewById<ImageButton>(R.id.retBtn).setOnClickListener {
            if(viewModel.returnTo=="searchResult")
                findNavController().navigate(R.id.action_movie_to_searchResult)
            else {
                findNavController().navigate(R.id.action_movie_to_main)
            }
        }

        if (viewModel.imbdID in viewModel.seenIDs){
            view.findViewById<ConstraintLayout>(R.id.toSeenHolder).visibility = View.GONE;
        }

        view.findViewById<ImageButton>(R.id.addToSeen).setOnClickListener {
            //dodawanie informacji do bazy
            addToDB();
            view.findViewById<ImageButton>(R.id.addToSeen).visibility = View.GONE;
            view.findViewById<TextView>(R.id.addToSeenTV).text = "Dodano do obejrzanych."
            viewModel.seenIDs.add(viewModel.imbdID);
        }

    }

    lateinit var Mtitle: String;
    lateinit var Mid: String;

    private fun downloadData(movieID:String){
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
                println(title + " - " + year)
                println("AAAA");

                Mid=movieID;
                Mtitle=title;

                helpView.findViewById<TextView>(R.id.barTitle)?.text = title;
                helpView.findViewById<TextView>(R.id.movieTitle)?.text = title;
                helpView.findViewById<TextView>(R.id.movieYear)?.text = year;
                helpView.findViewById<TextView>(R.id.movieDirector)?.text = director;
                helpView.findViewById<TextView>(R.id.movieActors)?.text = actors;
                helpView.findViewById<TextView>(R.id.moviePlot)?.text = plot;


                if(posterURL != "N/A") {
                    val icon = helpView.findViewById<ImageView>(R.id.moviePoster);
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

                /* activity?.runOnUiThread {
                    println("AAAA");
                    view?.findViewById<TextView>(R.id.barTitle)?.text = title;
                    view?.findViewById<TextView>(R.id.movieTitle)?.text = title;
                    view?.findViewById<TextView>(R.id.movieYear)?.text = year;
                    view?.findViewById<TextView>(R.id.movieDirector)?.text = director;
                    view?.findViewById<TextView>(R.id.movieActors)?.text = actors;
                    view?.findViewById<TextView>(R.id.moviePlot)?.text = plot;

                    view?.findViewById<ImageButton>(R.id.addToSeen)?.setOnClickListener {
                        println("AAAA");
                        //addToDB(title, movieID);
                    }

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
*/

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

    private fun addToDB(){
        println("AAA")
        runBlocking {
            val database = Firebase.database;
            val myRef = database.getReference("movies");
            myRef
                .child("${FirebaseAuth.getInstance().currentUser?.uid}")
                .child(Mid)
                .setValue(movieRecord(Mtitle,Mid))

            launch {
                Toast.makeText(context,"Zapisano jako obejrzane.",Toast.LENGTH_LONG).show();
            }
        }


    }

}