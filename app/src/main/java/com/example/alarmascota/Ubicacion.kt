package com.example.alarmascota

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task

class Ubicacion : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    companion object{
        var staticLatitud:Double = 0.0
        var staticLongitude:Double = 0.0

        var realLatitude:Double = 0.0
        var realLongitude:Double = 0.0

        var band:Boolean = false
        var globalBand:Boolean = false
    }


    private lateinit var mMap: GoogleMap
    private val DEFAULT_ZOOM = 15f
    private val CLOSE_ZOOM = 20f
    private var ACTUAL_ZOOM = 20f
    val TAG:String = "main"

    var address:String = ""
    lateinit var addressTxt: TextView
    lateinit var txtCosto: TextView
    lateinit var btnUpdate: Button
    lateinit var btnReset: Button
    lateinit var btnGuardar: Button
    lateinit var btnLocation: Button

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationPermissionsGranted = false

    private lateinit var currentLocation: Location
    private var userLat:Double = 0.0
    private var userLong:Double = 0.0


    val items = arrayOf("25m","30m", "50m")
    private var meters:Double = 20.0

    var markers = arrayListOf<MarkerOptions>()
    lateinit var autoCompleteText: AutoCompleteTextView
    lateinit var adapterItems: ArrayAdapter<String>
    lateinit var puntero: MarkerOptions

    var bandera:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion)

        autoCompleteText = findViewById(R.id.autoCompleteText)
        btnUpdate = findViewById(R.id.btn_update)
        btnReset = findViewById(R.id.btn_reset)
        btnGuardar = findViewById(R.id.btn_guardar)
        //btnLocation = findViewById(R.id.btn_location)

        adapterItems = ArrayAdapter(this, R.layout.list_options, items)
        autoCompleteText.setAdapter(adapterItems)


        btnUpdate.setOnClickListener{

            try{
                var ubicacion:LatLng
                ubicacion = puntero.position
                userLat = ubicacion.latitude
                userLong = ubicacion.longitude

                markers.removeAll(markers)

                createMarker(userLat, userLong, "HOME",1)
                updateMap()

                moveCamera(LatLng(userLat, userLong), ACTUAL_ZOOM, "My location")
                //drawCircle(LatLng(userLat, userLong), 400.0)
                //txtCosto.setText("")
            }catch(e:Exception){
                markers.removeAll(markers)

                createMarker(userLat, userLong, "HOME",1)
                updateMap()

                moveCamera(LatLng(userLat, userLong), ACTUAL_ZOOM, "My location")
            }


        }

        btnReset.setOnClickListener {
            userLat = realLatitude
            userLong = realLongitude

            markers.removeAll(markers)
            updateMap()

            moveCamera(LatLng(userLat, userLong), CLOSE_ZOOM, "My location")
            //drawCircle(LatLng(userLat, userLong), 400.0)
            //txtCosto.setText("")
        }

        btnGuardar.setOnClickListener {
            staticLatitud = userLat
            staticLongitude = userLong
            band = true;
            globalBand = true
            Toast.makeText(this@Ubicacion, "Se actualizo Ubicacion", Toast.LENGTH_SHORT).show()
        }

        /*
        btnLocation.setOnClickListener {
            userLat = staticLatitud
            userLong = staticLongitude

            markers.removeAll(markers)

            createMarker(userLat, userLong, "HOME",1)
            updateMap()

            moveCamera(LatLng(userLat, userLong), ACTUAL_ZOOM, "My location")
        }
        */



        // When click the hint selection, will trigger close keyboard function
        autoCompleteText.onItemClickListener =
            AdapterView.OnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
                var item:String =  parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Item: ${item}", Toast.LENGTH_SHORT).show()
                //val items = arrayOf("restaurant","store", "gym")

                if(item.equals("25m")){
                    meters = 25.0
                    ACTUAL_ZOOM = 20f
                }else if(item.equals("30m")){
                    meters = 30.0
                    ACTUAL_ZOOM = 19f
                }else if(item.equals("50m")){
                    meters = 50.0
                    ACTUAL_ZOOM = 19f
                }

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

        mMap.setOnMarkerClickListener(this)

        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener { point ->
            verifyMarker()
            val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                .title("New")
            puntero = marker
            markers.add(marker)

            //createMarker(point.latitude, point.longitude, "HOME",1)
            updateMapPointer()
            mMap.addMarker(marker)


            println(point.latitude.toString() + "---" + point.longitude)
        })


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

    private fun updateMap(){

        mMap.clear()
        for (marker:MarkerOptions in markers){
            mMap.addMarker(marker)
        }

        drawCircle(LatLng(userLat, userLong), meters)
    }

    private fun updateMapPointer(){

        mMap.clear()
        for (marker:MarkerOptions in markers){
            mMap.addMarker(marker)
        }

        //drawCircle(LatLng(userLat, userLong), meters)
    }

    fun createMarker(lati:Double, longi:Double, name: String, type:Int) {
        //val coordinates = LatLng(25.7299374,-100.2096866)
        val coordinates = LatLng(lati,longi)
        val marker:MarkerOptions
        if(type == 1){
            marker = MarkerOptions().position(coordinates).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.home_icon))
            markers.add(marker)
        }else if(type == 2){
            marker = MarkerOptions().position(coordinates).title(name)
            markers.add(marker)
        }



        //val pointer = mMap.addMarker(marker)
        //pointer?.tag = 0

    }

    private fun verifyMarker(){
        for(marker:MarkerOptions in markers){
            if(marker.title.equals("New")){
                val indexMarker = markers.indexOf(marker)
                markers.removeAt(indexMarker)
            }
        }
    }

    fun getDeviceLocation()
    {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(!band)
        {
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

                            if(bandera == true){
                                realLatitude = userLat
                                realLongitude = userLong
                                bandera = false
                            }


                            var distanceInMeters = currentLocation.distanceTo(home)
                            Log.d(TAG, "Distance in meters ->  ${distanceInMeters}")

                            //addLine(currentLocation,home)

                            //Location.distanceBetween(userLat, userLong, 25.7299374, -100.2096866, arr1)
                            //Log.d(TAG, "Distance -> ${arr1[0]} ")




                            createMarker(userLat, userLong, "HOME",1)

                            for (marker:MarkerOptions in markers){
                                mMap.addMarker(marker)
                            }

                            moveCamera(LatLng(userLat, userLong), CLOSE_ZOOM, "My location")
                            drawCircle(LatLng(userLat, userLong),25.0)



                        }else{
                            Log.d(TAG, "onComplete: current location is null")
                            Toast.makeText(this@Ubicacion, "Unable to get current location", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }catch (e: SecurityException){
                Log.d(TAG, "getUserLocation: Security Exception ${e.message}")
            }
        }else{
            userLat = staticLatitud
            userLong = staticLongitude

            markers.removeAll(markers)

            createMarker(userLat, userLong, "HOME",1)
            updateMap()

            moveCamera(LatLng(userLat, userLong), ACTUAL_ZOOM, "My location")
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

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "onMarkerClick: Clicking")
        // Retrieve the data from the marker.
        val clickCount = marker.tag as? Int

        // Check if a click count was set, then display the click count.
        clickCount?.let {
            val newClickCount = it + 1
            marker.tag = newClickCount
            Toast.makeText(this, "${marker.title} has been clicked $newClickCount times.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onMarkerClick: ${marker.title} has been clicked $newClickCount times.")
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }



}