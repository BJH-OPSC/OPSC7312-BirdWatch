package com.example.opsc_birdwatch

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    // private val account: HashMap<String, String> = HashMap()
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
    //----------------------------------------------------------------------------------------\\
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
            if (username.isEmpty() || password.isEmpty()) {
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
                alertDialog.setMessage("Username already exists or must be a valid email. Please choose another username.")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            } else if (!password.equals(confirmPassword)) {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Unsuccessful Register")
                alertDialog.setMessage("Passwords Do Not Match")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            } else {
                // Register the user
                AccountManager.addUser(username, password)
                createUser(username, password);
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
//----------------------------------------------------------------------------------------------------------\\
    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
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