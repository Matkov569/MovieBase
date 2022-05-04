package com.example.moviebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.runBlocking

class splash : Fragment() {

    lateinit var lSM:loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_splash, container, false);

        lSM = loadingScreenManager(activity,view,context);

        if(!lSM.networkTest())
            view.findViewById<TextView>(R.id.alertText).visibility = View.VISIBLE;

        // Inflate the layout for this fragment
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runBlocking {
            lSM.networkCircle({findNavController().navigate(R.id.action_splash_to_login)});
        }
    }
}