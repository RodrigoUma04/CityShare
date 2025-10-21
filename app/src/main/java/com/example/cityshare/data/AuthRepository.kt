package com.example.cityshare.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

fun loginWithEmailPassword(
    email: String,
    password: String,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
){
    val auth = FirebaseAuth.getInstance()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Login", "signInWithEmail:success")
                onSuccess()
            } else {
                Log.w("Login", "signInWithEmail:failure", task.exception)
                onFailure(task.exception?.message ?: "Unknown error")
            }
        }
}

fun loginWithGoogle() {

}

fun registerWithEmailPassword(
    email: String,
    password: String,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Register", "createUserWithEmail:success")
                onSuccess()
            } else {
                Log.w("Register", "createUserWithEmail:failure", task.exception)
                onFailure(task.exception?.message ?: "Unknown error")
            }
        }
}