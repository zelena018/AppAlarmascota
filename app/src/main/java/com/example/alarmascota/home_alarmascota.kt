package com.example.alarmascota

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Button

class home_alarmascota : AppCompatActivity() {
    lateinit var coordenadas : String
    lateinit var latitud : String
    lateinit var longitud : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_alarmascota)

        //Para ir de pantalla HOME al mapa
        val btnMapa1: Button = findViewById(R.id.btn_mapa)
        btnMapa1.setOnClickListener {
           // var sms = SmsManager.getDefault()
         //   sms.sendTextMessage(binding.numero.text.toString(),"ME","@GPS", null, null)
            //receiveMsg()
            val intent: Intent = Intent(this, mapaGPS::class.java)
            startActivity(intent)
        }

        //Para ir de la pantalla HOME al envio de SMS
        val btn_mensajes: Button = findViewById(R.id.btn_sms)
        btn_mensajes.setOnClickListener {

            val intent: Intent = Intent(this, mensajeSMS::class.java)
            startActivity(intent)
        }
    }

   /* private fun receiveMsg() {
        var br = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for(sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)){
                        binding.numero.setText(sms.originatingAddress)
                        binding.mensaje.setText(sms.displayMessageBody)
                        var coordenadas = sms.displayMessageBody
                    }
                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    } */
}