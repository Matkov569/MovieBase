package com.example.moviebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView

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