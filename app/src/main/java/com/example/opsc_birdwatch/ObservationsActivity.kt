package com.example.opsc_birdwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class ObservationsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var helperClass: HelperClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observations)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListObservationsFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_list)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val currentActivity = this::class.java

                //check whats the current activity, if not the main activity it will start the main activity
                if (currentActivity == ObservationsActivity::class.java){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                }
            }

            R.id.nav_map -> {startActivity(Intent(this, MapActivity::class.java))}

            R.id.nav_list -> {startActivity(Intent(this, ObservationsActivity::class.java))}

            R.id.nav_settings -> {startActivity(Intent(this, SettingsActivity::class.java))}

            R.id.nav_about -> {
                val currentActivity = this::class.java

                if (currentActivity == ObservationsActivity::class.java){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AboutFragment()).commit()
                }
            }
            R.id.nav_login -> {startActivity(Intent(this, SignInActivity::class.java))}

            R.id.nav_logout -> Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //onBackPressed function will determine what to do when the back button on the phone is pressed
    //checks if nav drawer is open, if yes then close it
    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    //method to check if user is logged in
    //if not sends them to the log in page
    //do not want unlogged users to be able to save
    fun isUserLogged(userName: String) : Boolean{

        return true
    }
}