package com.example.alarmascota

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.alarmascota.R
import android.os.Build
import android.content.pm.PackageManager
import android.widget.Toast

class mensajesSMS : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_alarmascota)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), 1000)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISO ACEPTADO", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "PERMISO DENEGADO", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}