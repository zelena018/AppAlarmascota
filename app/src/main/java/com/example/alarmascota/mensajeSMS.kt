package com.example.alarmascota

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.alarmascota.databinding.ActivityHomeAlarmascotaBinding
import com.example.alarmascota.databinding.ActivityMensajeSmsBinding

class mensajeSMS : AppCompatActivity() {
    private lateinit var binding : ActivityMensajeSmsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajeSmsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS),
                111)
        }
        else
            receiveMsg()

        binding.btnEnviarSms.setOnClickListener{
            var sms = SmsManager.getDefault()
            sms.sendTextMessage(binding.numero.text.toString(),"ME",binding.mensaje.text.toString(), null, null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==111 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            receiveMsg()
    }

    private fun receiveMsg() {
        var br = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for(sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)){
                        binding.numero.setText(sms.originatingAddress)
                        binding.mensaje.setText(sms.displayMessageBody)
                        var latitud = sms.displayMessageBody
                    }
                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }
}

