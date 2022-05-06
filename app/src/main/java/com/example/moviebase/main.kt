package com.example.moviebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.concurrent.Executors

class main : Fragment() {

    lateinit var resultAdapter: resultAdapter;
    lateinit var lSM:loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_main, container, false);

        lSM = loadingScreenManager(activity,view,context);


        val viewModel by activityViewModels<ViewModel>();

        resultAdapter = resultAdapter(viewModel,"searchResult");
        val recyclerView = view.findViewById<RecyclerView>(R.id.seenRecycler);
        recyclerView.adapter=resultAdapter;
        var layout = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layout

        //pobierz dane z firebase o obejrzanych pozycjach
        //zmienić hideloading na funkcje która wywoła hideloading po skończeniu pracy i zrobi te pobieranie danych itp
        if (!lSM.networkTest()) lSM.timer()
        lSM.networkCircle({lSM.hideLoading()});

        //daj avatar do profileImage
        activity?.runOnUiThread {
            runBlocking {
                var image:Bitmap?=null;
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val imageURL = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()
                    try {
                        val `in` = URL(imageURL).openStream()
                        image = BitmapFactory.decodeStream(`in`)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                launch {
                    delay(100)
                    view.findViewById<ImageView>(R.id.profileImage).setImageBitmap(image);
                }
            }

        }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lSM.timer();

        view.findViewById<CircularImageView>(R.id.profileImage).setOnClickListener {
            findNavController().navigate(R.id.action_main_to_user);
        }

        view.findViewById<Button>(R.id.goSearching).setOnClickListener {
            findNavController().navigate(R.id.action_main_to_searchResult);
        }

    }

}