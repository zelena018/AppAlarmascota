package com.example.alarmascota

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ReceiveSms : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Sms Received!", Toast.LENGTH_SHORT).show()
    }
}