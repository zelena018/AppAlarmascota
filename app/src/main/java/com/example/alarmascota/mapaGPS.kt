package com.example.alarmascota

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.alarmascota.databinding.ActivityHomeAlarmascotaBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.alarmascota.databinding.ActivityMapaGpsBinding
import com.example.alarmascota.mensajeSMS.Companion.DogLatitude
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class mapaGPS : AppCompatActivity(), OnMapReadyCallback {

    companion object{
        var DogLatitud:Double = 0.0
        var DogLongitud:Double = 0.0
    }
    private lateinit var binding : ActivityMapaGpsBinding

    private lateinit var mMap: GoogleMap
    private val DEFAULT_ZOOM = 15f
    private val CLOSE_ZOOM = 20f
    val TAG:String = "MAP "

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationPermissionsGranted = false

    private lateinit var currentLocation: Location
    private var userLat:Double = 0.0
    private var userLong:Double = 0.0

    var markers = arrayListOf<MarkerOptions>()

    val currentPos = Location("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaGpsBinding.inflate(layoutInflater)
        setContentView(binding.root)




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS),
                111)
        }
        else
            receiveMsg()

        binding.btnLocate.setOnClickListener{
            var sms = SmsManager.getDefault()
            sms.sendTextMessage("8124349752","ME","@GPS", null, null)


        }

        getLocationPermission()
    }

    private fun initMap()
    {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap)
    {
        mMap = googleMap


        //createMarker(25.7299374,-100.2096866)
        if (mLocationPermissionsGranted) {
            getDeviceLocation()

            //Esta linea solo se pone para que nops deje poner el icono azuliño
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            //Añade icono azul de la current location del dispositivo
            //Añade icono azul de la current location del dispositivo
            mMap.isMyLocationEnabled = true
        }
    }

    fun createMarker(lati:Double, longi:Double, name: String, type:Int) {
        //val coordinates = LatLng(25.7299374,-100.2096866)
        val coordinates = LatLng(lati,longi)
        val marker:MarkerOptions
        if(type == 1){
            marker = MarkerOptions().position(coordinates).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.home_icon))
            markers.add(marker)
        }else if(type == 2){
            marker = MarkerOptions().position(coordinates).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_icon))
            markers.add(marker)
        }



        //val pointer = mMap.addMarker(marker)
        //pointer?.tag = 0

    }

    private fun updateMap(){

        mMap.clear()
        for (marker:MarkerOptions in markers){
            mMap.addMarker(marker)
        }

        drawCircle(LatLng(Ubicacion.staticLatitud, Ubicacion.staticLongitude), Ubicacion.meters)
    }

    private fun updateMapDog(){

        for(marker:MarkerOptions in markers){
            if(marker.title.equals("Dog")){
                mMap.addMarker(marker)
            }
        }

        //drawCircle(LatLng(userLat, userLong), Ubicacion.meters)
    }

    private fun verifyMarker(){
        for(marker:MarkerOptions in markers){
            if(marker.title.equals("DOG")){
                val indexMarker = markers.indexOf(marker)
                markers.removeAt(indexMarker)
            }
        }
    }



        //val pointer = mMap.addMarker(marker)
        //pointer?.tag = 0



    fun getDeviceLocation()
    {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                currentPos.latitude = Ubicacion.staticLatitud
                currentPos.longitude = Ubicacion.staticLongitude

                createMarker(currentPos.latitude, currentPos.longitude, "HOME",1)

                for (marker:MarkerOptions in markers){
                    mMap.addMarker(marker)
                }

                moveCamera(LatLng(Ubicacion.staticLatitud, Ubicacion.staticLongitude), CLOSE_ZOOM, "My location")
                drawCircle(LatLng(Ubicacion.staticLatitud, Ubicacion.staticLongitude), Ubicacion.meters )


            }
        }catch (e: SecurityException){
            Log.d(TAG, "getUserLocation: Security Exception ${e.message}")
        }


    }

    fun moveCamera(lati: LatLng, zoom:Float, title:String )
    {
        Log.d(TAG, "moveCamera: moving camero to -> lat: " + lati.latitude + ", longitude: " + lati.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lati, zoom))
    }

    private fun addLine(loc1: Location, loc2: Location)
    {
        // Instantiates a new Polyline object and adds points to define a rectangle
        val polylineOptions = PolylineOptions()
            .add(LatLng(loc1.latitude, loc1.longitude))
            .add(LatLng(loc2.latitude, loc2.longitude))


        // Get back the mutable Polyline
        val polyline = mMap.addPolyline(polylineOptions)
    }


    private fun drawCircle(point: LatLng, radio:Double) {

        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(point)

        // Radius of the circle
        circleOptions.radius(radio)

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK)

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000)

        // Border width of the circle
        circleOptions.strokeWidth(2f)

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions)
    }



    //------------------------------------------PERMISOS----------------------------------------------------
    fun getLocationPermission()
    {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ContextCompat.checkSelfPermission(this.applicationContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this.applicationContext, COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    //------------------------------------------RECIBIMIENTO DE  OORDENADAS POR MENSAJE----------------------------------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==111 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            receiveMsg()
    }

    private fun receiveMsg()
    {
        var br = object : BroadcastReceiver()
        {
            override fun onReceive(p0: Context?, p1: Intent?)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
                    for(sms in Telephony.Sms.Intents.getMessagesFromIntent(p1))
                    {

                        Toast.makeText(this@mapaGPS, "Mensaje -> ${sms.displayMessageBody}", Toast.LENGTH_SHORT).show()
                        var coordenadas = sms.displayMessageBody
                        var both = coordenadas.split(',')
                        mapaGPS.DogLatitud =  both.get(0).toDouble()
                        mapaGPS.DogLongitud = both.get(1).toDouble()

                        updateMap()
                        verifyMarker()
                        createMarker(mapaGPS.DogLatitud, mapaGPS.DogLongitud, "DOG", 2)
                        updateMapDog()

                        val coordinates = LatLng(mapaGPS.DogLatitud,mapaGPS.DogLongitud)
                        moveCamera(coordinates,CLOSE_ZOOM,"Dog")
                        //Toast.makeText(this@mapaGPS, "Latitud -> ${both.get(0)}", Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this@mapaGPS, "Longitud -> ${both.get(1)}", Toast.LENGTH_SHORT).show()

                    }
                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

}