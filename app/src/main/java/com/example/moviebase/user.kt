package com.example.moviebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController

class user : Fragment() {

    lateinit var lSM:loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_user, container, false);

        lSM = loadingScreenManager(activity,view,context);

        //pobierz dane i avatar i umieść w profileImage
        //zmienić na odpowiednią funckję
        if (!lSM.networkTest()) lSM.timer()
        lSM.networkCircle({lSM.hideLoading()})

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.retBtn).setOnClickListener {
            findNavController().navigate(R.id.action_user_to_main);
        }

        view.findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            //wylogowywanie
            findNavController().navigate(R.id.action_user_to_login);
        }
    }
}