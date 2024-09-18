package com.example.pushreceiversample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class PushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent.action) {
            Log.d("PushReceiver", "WAP Push message received")
        }
    }
}
