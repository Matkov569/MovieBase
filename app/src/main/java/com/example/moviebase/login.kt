package com.example.moviebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton

class login : Fragment() {

    lateinit var lSM:loadingScreenManager;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_login, container, false)

        lSM = loadingScreenManager(activity,view,context);

        //sprawdź czy jest zalogowany i jeśli tak to przekieruj do main
        //zmienić przejście na funkcje do logowania itd
        if (!lSM.networkTest()) lSM.timer()

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<SignInButton>(R.id.signInButton).setOnClickListener{
            //lSM.networkCircle({})
            findNavController().navigate(R.id.action_login_to_main)
        }

    }
}