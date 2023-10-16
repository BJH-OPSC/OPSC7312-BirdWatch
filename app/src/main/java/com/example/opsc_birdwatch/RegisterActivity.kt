package com.example.opsc_birdwatch
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText



class RegisterActivity : AppCompatActivity() {
    private val account: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // After successful registration, navigate to the login screen
            if (UserManager.getUserPassword(username) != null) {
                // Show an error message
                usernameEditText.setText("Error: Invalid")
            } else {
                // Register the user
                UserManager.addUser(username, password)
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Successful Register")
                alertDialog.setMessage("You Have Successfully Registered")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    // when the user clicks OK
                    dialog.dismiss()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                alertDialog.show()



            }
        }


    }
}