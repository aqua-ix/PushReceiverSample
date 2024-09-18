package com.example.pushreceiversample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent.action) {
            Log.d("PushReceiver", "WAP Push message received")
            val currentTime = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedTime = formatter.format(currentTime)

            val sharedPref = context.getSharedPreferences("PushMessages", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("lastReceived", formattedTime)
                apply()
            }

            Toast.makeText(context, "New WAP Push message received", Toast.LENGTH_SHORT).show()
        }
    }
}
