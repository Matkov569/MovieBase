package com.example.moviebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.JsonReader
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.json.JSONStringer
import java.net.URL
import java.util.concurrent.Executors

class main : Fragment() {

    class moviesRecordLiveData(val lSM:loadingScreenManager, val viewModel: ViewModel):LiveData<List<movieRecord>>(){
        override fun onActive() {
            super.onActive()

            FirebaseDatabase.getInstance()
                .getReference("movies")
                .child("${FirebaseAuth.getInstance().currentUser?.uid}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            lSM.showLoading()
                            var list = mutableListOf<movieRecord>();
                            var seen = mutableListOf<String>();

                            runBlocking {
                                for (position in snapshot.children){
                                    var obj = position.getValue(movieRecord::class.java);
                                    list.add(obj!!);
                                    seen.add(obj.id);
                                }

                                value = list;
                                viewModel.seenIDs = seen;

                                launch {
                                    lSM.hideLoading()
                                }
                            }

                        }
                        else{
                            lSM.hideLoading()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

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

        resultAdapter = resultAdapter(viewModel,"main",requireContext());
        val recyclerView = view.findViewById<RecyclerView>(R.id.seenRecycler);
        recyclerView.adapter=resultAdapter;
        var layout = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layout

        //pobierz dane z firebase o obejrzanych pozycjach
        moviesRecordLiveData(lSM,viewModel).observe(viewLifecycleOwner, Observer { records ->
            resultAdapter.setData(records)
        })

        if (!lSM.networkTest()) lSM.timer()

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
                    delay(1000)
                    view.findViewById<ImageView>(R.id.profileImage).setImageBitmap(image!!);
                }
            }

        }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseApp.initializeApp(requireContext())
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        lSM.timer();

        view.findViewById<CircularImageView>(R.id.profileImage).setOnClickListener {
            findNavController().navigate(R.id.action_main_to_user);
        }

        view.findViewById<Button>(R.id.goSearching).setOnClickListener {
            findNavController().navigate(R.id.action_main_to_searchResult);
        }

    }

}