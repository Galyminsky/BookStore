package me.proton.jobforandroid.bookstore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import me.proton.jobforandroid.bookstore.data.Book
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fb = Firebase.firestore
            val storage = Firebase.storage.reference.child("images")
            val launcher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri == null) return@rememberLauncherForActivityResult
                    val task = storage.child("testImage.jpg").putBytes(
                        bitmapToByteArray(this, uri)
                    )
                    task.addOnSuccessListener { uploadTask ->
                        uploadTask.metadata?.reference?.downloadUrl?.addOnCompleteListener { urlTask ->
                            saveBook(fb, urlTask.result.toString())
                        }
                    }
                }
            MainScreen {
                launcher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

@Composable
fun MainScreen(onClick: () -> Unit) {
    val fb = Firebase.firestore
    val list = remember {
        mutableStateOf(emptyList<Book>())
    }

    val listener = fb.collection("books").addSnapshotListener { snapShot, exeption ->
        list.value = snapShot?.toObjects(Book::class.java) ?: emptyList()
    }
    // listener.remove() - убрать обновление при переходе в другую активити или выходе из приложения

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            items(list.value) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .height(100.dp)
                                .width(100.dp)
                        )
                    }
                    Text(
                        text = book.name, modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth()
                            .padding(15.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            onClick = {
                onClick()
            }) {
            Text(
                text = "Add Book"
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

private fun bitmapToByteArray(context: Context, uri: Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
    return baos.toByteArray()
}

private fun saveBook(fb: FirebaseFirestore, url: String) {
    fb.collection("books")
        .document().set(
            Book(
                "Van Helsing",
                "Wow cool book",
                "300",
                "fantastic",
                url
            )
        )
}