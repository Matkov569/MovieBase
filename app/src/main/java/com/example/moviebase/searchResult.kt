package com.example.moviebase

import android.app.AlertDialog
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLEncoder.encode
import kotlin.math.ceil

class searchResult : Fragment() {

    lateinit var resultAdapter: resultAdapter;
    lateinit var lSM: loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search_result, container, false);

        lSM = loadingScreenManager(activity,view,context);

        val viewModel by activityViewModels<ViewModel>();

        resultAdapter = resultAdapter(viewModel,"searchResult",requireContext());
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

        view.findViewById<ImageButton>(R.id.retBtn).setOnClickListener {
            findNavController().navigate(R.id.action_searchResult_to_main)
        }

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
                        lSM.networkCircle({
                                runBlocking {
                                    getResults(view.findViewById<SearchView>(R.id.barSearch)?.query.toString());
                                }

                        });
                    }
                }
                return false
            }
        } )

    }

    private suspend fun getResults(keyword:String){
        var json = "";
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val apiID:String = secrets().moviesAPIkey;
        lSM.showLoading();
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
                        runBlocking {

                            resultAdapter.setData(table, false);
                            launch {
                                lSM.hideLoading()
                            }
                        }
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
            builder.setMessage("Nie otrzymano odpowiedzi od serwera. Spróbuj ponownie.");
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