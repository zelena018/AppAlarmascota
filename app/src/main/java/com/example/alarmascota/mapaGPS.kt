package com.example.alarmascota

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.alarmascota.databinding.ActivityMapaGpsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task

class mapaGPS : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val DEFAULT_ZOOM = 15f
    private val CLOSE_ZOOM = 20f
    val TAG:String = "main"

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationPermissionsGranted = false

    private lateinit var currentLocation: Location
    private var userLat:Double = 0.0
    private var userLong:Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

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

    fun createMarker(lati:Double, longi:Double, name: String)
    {
        //val coordinates = LatLng(25.7299374,-100.2096866)
        val coordinates = LatLng(lati,longi)
        val marker = MarkerOptions().position(coordinates).title(name)


        val pointer = mMap.addMarker(marker)
        pointer?.tag = 0

    }

    fun getDeviceLocation()
    {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                var location: Task<*> = mFusedLocationProviderClient.getLastLocation()
                location.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Log.d(TAG, "getUserLocation: Found Location")
                        currentLocation = task.getResult() as Location
                        val arr1 = floatArrayOf(.1f)


                        val home = Location("")
                        home.latitude = 25.7299374
                        home.longitude = -100.2096866

                        userLat = currentLocation.latitude
                        userLong = currentLocation.longitude


                        var distanceInMeters = currentLocation.distanceTo(home)
                        Log.d(TAG, "Distance in meters ->  ${distanceInMeters}")

                        //addLine(currentLocation,home)

                        //Location.distanceBetween(userLat, userLong, 25.7299374, -100.2096866, arr1)
                        //Log.d(TAG, "Distance -> ${arr1[0]} ")




                        moveCamera(LatLng(userLat, userLong), DEFAULT_ZOOM, "My location")
                        drawCircle(LatLng(userLat, userLong))



                    }else{
                        Log.d(TAG, "onComplete: current location is null")
                        Toast.makeText(this@mapaGPS, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                }

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


    private fun drawCircle(point: LatLng)
    {

        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(point)

        // Radius of the circle
        circleOptions.radius(25.0)

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

}