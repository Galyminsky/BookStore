package me.proton.jobforandroid.bookstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import me.proton.jobforandroid.bookstore.ui.theme.BookStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fb = Firebase.firestore
            fb.collection("books")
                .document().set(mapOf("name" to "Proton"))
        }
    }
}

