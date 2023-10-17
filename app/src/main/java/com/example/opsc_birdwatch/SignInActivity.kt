package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        //val registerText = findViewById<TextView>(R.id.)
        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)


        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
            val storedPassword = AccountManager.getUserPassword(username)

            if (storedPassword != null && storedPassword == password)
            {
                // Successful login
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Successful Login")
                alertDialog.setMessage("You Have Successfully Logged In")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    // when the user clicks OK
                    dialog.dismiss()
                    finish()
                }
                alertDialog.show()

            } else {
                //login Failed, show an error message
                usernameEditText.setText("Error: Invalid")
            }
        }

       /* registerText.setOnClickListener {
            // Start the RegisterActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()

        }*/
    }
}