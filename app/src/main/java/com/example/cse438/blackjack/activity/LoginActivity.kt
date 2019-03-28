package com.example.cse438.blackjack.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.cse438.blackjack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        button_sign_in.setOnClickListener { signIn() }
        button_sign_up.setOnClickListener { signUp() }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and proceed to MainActivity if so
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //someone is already signed in
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }
    }

    private fun signUp() {

        // check email and password fields not empty
        if (!validateForm()) {
            return
        }

        val email = email_field.text.toString()
        val password = password_field.text.toString()


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("", "createUserWithEmail:success")
                    createUserCollection()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // [START_EXCLUDE]
//                hideProgressDialog()
                // [END_EXCLUDE]
            }

    }

    private fun signIn() {

        // check email and password fields not empty
        if (!validateForm()) {
            return
        }

        val email = email_field.text.toString()
        val password = password_field.text.toString()


        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("", "signInWithEmail:success")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // [START_EXCLUDE]
                if (!task.isSuccessful) {
                }
//                hideProgressDialog()
                // [END_EXCLUDE]
            }
        // [END sign_in_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = email_field.text.toString()
        if (TextUtils.isEmpty(email)) {
            email_field.error = "Required."
            valid = false
        } else {
            email_field.error = null
        }

        val password = password_field.text.toString()
        if (TextUtils.isEmpty(password)) {
            password_field.error = "Required."
            valid = false
        } else {
            password_field.error = null
        }

        return valid
    }

    private fun createUserCollection() {
        // Create a new user with info relevant to blackjack database
        val user = HashMap<String, Any>()
        user["email"] = auth.currentUser!!.email.toString()
        user["wins"] = 0
        user["losses"] = 0
        user["money"] = 1000

        // Add a new document with a generated ID
        database.collection("users").document(auth.currentUser!!.uid)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.d("", "DocumentSnapshot added with docRef: $documentReference")
            }
            .addOnFailureListener { e ->
                Log.w("", "Error adding document", e)
            }
    }


}