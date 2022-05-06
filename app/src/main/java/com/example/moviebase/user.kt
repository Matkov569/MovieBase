package com.example.moviebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.concurrent.Executors


class user : Fragment() {

    lateinit var lSM:loadingScreenManager;
    lateinit var image:Bitmap;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_user, container, false);

        lSM = loadingScreenManager(activity,view,context);

        //pobierz dane i avatar i umieść w profileImage
        var user = FirebaseAuth.getInstance().currentUser;
        view.findViewById<TextView>(R.id.userName).text=user?.displayName;

        activity?.runOnUiThread {
            runBlocking {
                var image:Bitmap?=null;
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val imageURL = user?.photoUrl.toString()
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

        //zmienić na odpowiednią funckję
        if (!lSM.networkTest()) lSM.timer()
        lSM.networkCircle({lSM.hideLoading()})

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by activityViewModels<ViewModel>();


        view.findViewById<ImageButton>(R.id.retBtn).setOnClickListener {
            findNavController().navigate(R.id.action_user_to_main);
        }

        view.findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            //wylogowywanie
            //
            runBlocking {
                //viewModel.googleClient?.signOut();
                //client.signOut();
                FirebaseAuth.getInstance().signOut();
                AuthUI.getInstance().signOut(requireContext());

                launch {
                    findNavController().navigate(R.id.action_user_to_login);
                }
            }

        }
    }

}