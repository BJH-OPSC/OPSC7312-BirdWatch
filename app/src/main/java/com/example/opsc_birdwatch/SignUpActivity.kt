package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class SignUpActivity : AppCompatActivity() {
   // private val account: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextConfirmPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val signIn = findViewById<Button>(R.id.buttonSignIn)

        //Register Button
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // After successful registration, navigate to the login screen
            if ( username.isEmpty() || password.isEmpty()) {
                // Show an error message
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Unsuccessful Register")
                alertDialog.setMessage("Invalid Username and/or Password")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    // when the user clicks OK
                    dialog.dismiss()
                }
                alertDialog.show()
                //usernameEditText.setText("Error: Invalid")
            } else if (AccountManager.getUserPassword(username) != null) {
                // Show an error message if the username already exists
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Unsuccessful Register")
                alertDialog.setMessage("Username already exists. Please choose another username.")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }else if (!password.equals(confirmPassword)) {
                // Show an error message if the username already exists
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Unsuccessful Register")
                alertDialog.setMessage("Passwords Do Not Match")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }else {
                // Register the user
                AccountManager.addUser(username, password)
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Successful Register")
                alertDialog.setMessage("You Have Successfully Registered")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    // when the user clicks OK
                    dialog.dismiss()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                alertDialog.show()


            }
        }
        //-----------------------------------------------------------------------------------------------\\
        //sign in link
        signIn.setOnClickListener {
            // Start the RegisterActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}
//------------------------------------------------End of File-----------------------------------------\\