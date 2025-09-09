package com.example.wakaru

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class CallListener(context: Context) : PhoneStateListener() {
    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            sendToPC("Llamada entrante de: $incomingNumber")
        }
    }
    private fun sendToPC(message: String) {
        Thread {
            try {
                val socket = Socket()
                val address = InetSocketAddress(IPPC, 5000) // Tu IP local

                // Intenta conectarse con timeout
                socket.connect(address, 2000)

                val output = PrintWriter(socket.getOutputStream(), true)
                output.println(message)
                socket.close()
            } catch (e: Exception) {
                Log.e("SocketError", "Error enviando al servidor: ${e.message}")
            }
        }.start()
    }


}
