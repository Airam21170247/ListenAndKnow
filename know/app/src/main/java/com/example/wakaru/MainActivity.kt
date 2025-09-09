package com.example.wakaru

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wakaru.ui.theme.WakaruTheme
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

val IPPC = "192.168.1.5"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sendToPC("Start")
        setContent {
            WakaruTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    startActivity(intent)

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WakaruTheme {
        Greeting("Android")
    }
}
private fun sendToPC(message: String) {
    Thread {
        try {
            val socket = Socket()
            val address = InetSocketAddress(IPPC, 5000)
            socket.connect(address, 2000)

            // Clave derivada del SHA-256 de la frase
            val secret = "dudda&alda"
            val key = MessageDigest.getInstance("SHA-256").digest(secret.toByteArray(Charsets.UTF_8))
            val aesKey = SecretKeySpec(key, "AES")

            // Generar IV aleatorio
            val iv = ByteArray(16)  // AES requiere un IV de 16 bytes
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec)

            // Cifrar el mensaje
            val encrypted = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

            // Concatenar IV y el mensaje cifrado
            val outputMessage = iv + encrypted

            // Codificar en Base64 para enviarlo
            val base64Message = Base64.encodeToString(outputMessage, Base64.NO_WRAP)

            val output = PrintWriter(socket.getOutputStream(), true)
            output.println(base64Message)
            socket.close()
        } catch (e: Exception) {
            Log.e("SocketError", "Error enviando al servidor: ${e.message}")
        }
    }.start()
}