package com.example.photogallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

private const val TAG = "PhotoPageActivity"
class PhotoPageActivity : AppCompatActivity(R.layout.activity_photo_page) {
    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_page) as NavHostFragment
        navHostFragment.navController.setGraph(R.navigation.nav_graph_page,PhotoPageFragment.getBundle(intent.data!!))
    }

    companion object {
        fun newIntent(context: Context, uri: Uri) =
            Intent(context, PhotoPageActivity::class.java).apply {
                data = uri
            }
    }


    override fun onBackPressed() {
        val callback = navHostFragment.childFragmentManager.fragments.get(0) as Callback
        if(callback.onBackPressed())
            super.onBackPressed()
    }
}