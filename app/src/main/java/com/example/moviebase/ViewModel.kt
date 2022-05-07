package com.example.moviebase

import android.content.Context
import android.graphics.Bitmap
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlin.random.Random

class ViewModel: ViewModel() {
    var returnTo:String = "";
    var imbdID:String = "";
    var searchText:String = "";
    var seenIDs:MutableList<String> = mutableListOf();

}