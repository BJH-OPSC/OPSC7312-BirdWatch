package com.example.opsc_birdwatch
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.login_button)
        val registerText = findViewById<TextView>(R.id.register_text)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)


        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = findViewById<EditText>(R.id.login_password).text.toString()
            val storedPassword = UserManager.getUserPassword(username)

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

            registerText.setOnClickListener {
                // Start the RegisterActivity
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                finish()

            }
        }
    }

