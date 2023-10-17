package com.example.opsc_birdwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MapActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val navigationView = findViewById<NavigationView>(R.id.bottomNavigationView)
        navigationView.setNavigationItemSelectedListener(this)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_exit->{
                    startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.menu_home->{
                    startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.menu_list->{
                    startActivity(Intent(this, ObservationsActivity::class.java))
            }
        }
        return true
    }

}