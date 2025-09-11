package com.example.wakaru

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.SecureRandom


class MyNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        var text = extras.getCharSequence("android.text")?.toString() ?: ""
        // Recortar el texto a 15 caracteres si es más largo
        text = if (text.length > 35) text.substring(0, 35) + "..." else text


        // Si es red social, mostrar más info
        val esRedSocial = sbn.packageName in listOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "com.facebook.orca",
            "com.instagram.android",
            "com.google.android.gm"  // Gmail también se puede incluir
        )

        // Si es una llamada, mostrar mensaje especial
        val esLlamada = sbn.packageName == "com.google.android.dialer"

        val mensaje = when {
            esRedSocial && title.isNotBlank() && text.isNotBlank() -> {
                "👤 $title\n💬 $text\n $appName"
            }
            esLlamada -> {
                "📞 => $appName"
            }
            else -> {
                "🔔 => $appName"
            }
        }

        // Si el mensaje contiene "music" (sin importar mayúsculas/minúsculas), no enviar
        if (!mensaje.contains("musicolet", ignoreCase = true)) {
            sendToPC(mensaje)
        }
    }






    private fun sendToPC(message: String) {
        Thread {
            try {
                val socket = Socket()
                val address = InetSocketAddress(IPPC, 5000)
                socket.connect(address, 2000)

                // Clave derivada del SHA-256 de la frase
                val secret = "(Password for encryption)"
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


}
