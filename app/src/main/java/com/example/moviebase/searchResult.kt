package com.example.moviebase

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLEncoder
import java.net.URLEncoder.encode
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.schedule
import kotlin.math.ceil
import kotlin.text.Regex.Companion.escape

class searchResult : Fragment() {

    lateinit var resultAdapter: resultAdapter;
    lateinit var lSM: loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search_result, container, false);

        lSM = loadingScreenManager(activity,view);

        val viewModel by activityViewModels<ViewModel>();

        resultAdapter = resultAdapter(viewModel);
        val recyclerView = view.findViewById<RecyclerView>(R.id.resultRecycler);
        recyclerView.adapter=resultAdapter;
        var layout = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layout

        if(viewModel.searchText!="")
            runBlocking {
                getResults(viewModel.searchText);
            }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<SearchView>(R.id.barSearch).isSubmitButtonEnabled = true;
        view.findViewById<SearchView>(R.id.barSearch).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(view.findViewById<SearchView>(R.id.barSearch)?.query.toString()!="") {
                    val viewModel by activityViewModels<ViewModel>();
                    viewModel.searchText=view.findViewById<SearchView>(R.id.barSearch)?.query.toString();
                    lSM.showLoading();
                    runBlocking {
                        onSubmitFunction();
                    }
                }
                return false
            }
        } )

    }

    private suspend fun onSubmitFunction(){
        if(!networkTest()){
            Timer().schedule(1000){
                runBlocking {onSubmitFunction()};
            }
        }
        else {
            activity?.runOnUiThread {
                runBlocking {
                    getResults(view?.findViewById<SearchView>(R.id.barSearch)?.query.toString());
                }
            }
        }
    }

    private suspend fun getResults(keyword:String){
        var json = "";
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val apiID:String = secrets().moviesAPIkey;
        try {
            runBlocking {

                json = URL("https://www.omdbapi.com/?apikey=$apiID&s=${encode(keyword,"utf-8")}").readText();

                var obj = JSONObject(json);
                if(obj.optString("totalResults","N/A")!="N/A") {
                    var count = ceil(obj.getString("totalResults").toDouble() / 10).toInt();

                    var table = mutableListOf<movieRecord>();

                    var results = obj.getJSONArray("Search");
                    for (i in 0..results.length() - 1) {
                        val record = results.getJSONObject(i);
                        //println(record.getString("Title")+" - "+record.getString("Type"));
                        if (record.getString("Type") != "game")
                            table.add(
                                movieRecord(
                                    record.getString("Title"),
                                    record.getString("imdbID")
                                )
                            )
                    }

                    if (count > 1) {
                        for (i in 2..count) {
                            json =
                                URL("https://www.omdbapi.com/?apikey=$apiID&s=$keyword&page=$i").readText();
                            var obj = JSONObject(json);
                            var results = obj.getJSONArray("Search");
                            for (j in 0..results.length() - 1) {
                                val record = results.getJSONObject(j);
                                //println(record.getString("Title")+" - "+record.getString("Type"));
                                if (record.getString("Type") != "game")
                                    table.add(
                                        movieRecord(
                                            record.getString("Title"),
                                            record.getString("imdbID")
                                        )
                                    )
                            }
                        }
                    }

                    launch {
                        resultAdapter.setData(table, false);
                        // hide loading screen
                        lSM.hideLoading();
                    }
                }
                else{
                    var builder = AlertDialog.Builder(context);
                    builder.setTitle("Brak wyników");
                    builder.setMessage("Nie znaleziono wyników dla hasła \"$keyword\".");
                    builder.setPositiveButton("Zamknij"){ dialog, which ->
                    }
                    builder.show();
                    lSM.hideLoading();
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