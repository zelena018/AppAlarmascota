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
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.alarmascota.databinding.ActivityHomeAlarmascotaBinding
import com.example.alarmascota.databinding.ActivityMensajeSmsBinding

class home_alarmascota : AppCompatActivity() {
    private lateinit var binding : ActivityHomeAlarmascotaBinding
    private var TAG: String = "Main"
    private var coordenadas : String = ""
    lateinit var latitud : String
    lateinit var longitud : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAlarmascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS),
                111)
        }
        else{
            receiveMsg()
        }


        //Para ir de pantalla HOME al mapa
        //
        binding.btnMapa.setOnClickListener {
           // var sms = SmsManager.getDefault()
         //   sms.sendTextMessage(binding.numero.text.toString(),"ME","@GPS", null, null)
            //receiveMsg()

            if (Ubicacion.globalBand){
                var sms = SmsManager.getDefault()
                sms.sendTextMessage("8124349752","ME","@GPS", null, null)



                //Toast.makeText(this, coordenadas, Toast.LENGTH_SHORT).show()


                val intent: Intent = Intent(this, mapaGPS::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, "PRIMERO DEBE GUARDAR UNA UBICACION", Toast.LENGTH_LONG).show()
            }

        }

        binding.btnMicro.setOnClickListener {
            var sms = SmsManager.getDefault()
            sms.sendTextMessage("8124349752","ME","@CALL", null, null)

            Toast.makeText(this, "ESTABLECIENDO COMUNICACION CON GPS", Toast.LENGTH_SHORT).show()
        }

        //Para ir de la pantalla HOME al envio de SMS
        binding.btnSms.setOnClickListener {

            var sms = SmsManager.getDefault()
            sms.sendTextMessage("8124349752","ME","@GPS", null, null)

            val intent: Intent = Intent(this, mensajeSMS::class.java)
            startActivity(intent)
        }

        binding.btnUbicacion.setOnClickListener {
            val intent: Intent = Intent(this, Ubicacion::class.java)
            startActivity(intent)
        }
    }



   private fun receiveMsg() {
        var br = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for(sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)){
                        //binding.numero.setText(sms.originatingAddress)
                        coordenadas = sms.displayMessageBody
                        Log.d(TAG, "ReceiveMsg: Recibi mensaje -> $coordenadas")
                    }
                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }


}