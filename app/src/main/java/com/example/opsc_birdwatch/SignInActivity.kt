package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("User Already Logged-In")
            alertDialog.setMessage("You Have Already Successfully Logged In")
            alertDialog.setPositiveButton("OK") { dialog, _ ->
                // when the user clicks OK
                dialog.dismiss()
                finish()
            }
            alertDialog.show()        }
    }
//----------------------------------------------------------------------------------------------------\\
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val register = findViewById<Button>(R.id.buttonSignUp)
        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)

        //login Button
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
            val storedPassword = AccountManager.getUserPassword(username)

            if (storedPassword != null && storedPassword == password) {
                // Successful login
                loginUser(username,password);
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
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Unsuccessful Login")
                alertDialog.setMessage("Invalid Username and/or Password")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    // when the user clicks OK
                    dialog.dismiss()
                }
                alertDialog.show()
            }
        }
        //-------------------------------------------------------------------------------\\
        //register page button
        register.setOnClickListener {
            // Start the RegisterActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()

        }
    }

    private fun loginUser(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}
//------------------------------------------------End of File-----------------------------------------\\