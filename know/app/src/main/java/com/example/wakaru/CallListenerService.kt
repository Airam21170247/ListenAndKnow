package com.example.wakaru

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import java.io.PrintWriter
import java.net.Socket

class CallListenerService : Service() {
    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                if (state == TelephonyManager.CALL_STATE_RINGING && incomingNumber != null) {
                    sendToPC("Llamada entrante: $incomingNumber")
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun sendToPC(message: String) {
        Thread {
            try {
                val socket = Socket(IPPC, 5000)
                val output = PrintWriter(socket.getOutputStream(), true)
                output.println(message)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
