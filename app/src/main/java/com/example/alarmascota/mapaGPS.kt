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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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
import org.json.JSONException

class mapaGPS : AppCompatActivity(), OnMapReadyCallback {

    companion object{
        var DogLatitud:Double = 0.0
        var DogLongitud:Double = 0.0
    }
    var Dog = Location("")
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
            if(marker.title.equals("DOG")){
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

                        verifyMarker()
                        updateMap()
                        createMarker(mapaGPS.DogLatitud, mapaGPS.DogLongitud, "DOG", 2)
                        updateMapDog()


                        Dog.latitude = mapaGPS.DogLatitud
                        Dog.longitude = mapaGPS.DogLongitud

                        var distanceInMeters = currentPos.distanceTo(Dog)
                        Log.d(TAG, "Distance in meters ->  ${distanceInMeters}")
                        binding.Distancia.setText(getString(R.string.txtDistancia, distanceInMeters.toString()))
                        addLine(currentPos,Dog)



                        if(distanceInMeters <= Ubicacion.meters){
                            binding.tvStatus.setText(getString(R.string.txtStatus, "Dentro del rango"))
                            binding.tvStatus.setTextColor(Color.GREEN)

                        }else{
                            binding.tvStatus.setText(getString(R.string.txtStatus, "fUERA del rango"))
                            binding.tvStatus.setTextColor(Color.RED)
                        }
                        getAddressName()

                        val coordinates = LatLng(mapaGPS.DogLatitud,mapaGPS.DogLongitud)
                        moveCamera(coordinates,19f  ,"Dog")
                        //Toast.makeText(this@mapaGPS, "Latitud -> ${both.get(0)}", Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this@mapaGPS, "Longitud -> ${both.get(1)}", Toast.LENGTH_SHORT).show()

                    }
                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }


    private fun getAddressName(){
        var queue = Volley.newRequestQueue(this)
        var lati:Double
        var longi:Double
        var precios:Double = 0.0
        var cont:Int = 0
        var promedio:Double = 0.0
        var distance:Double = 99999.0
        var distanceInMeters:Float

        var address:String = ""
        var actualName:String = ""

        lateinit var actualLocation: Location



        Log.d(TAG, "UserLat -> ${Dog.latitude} ")
        Log.d(TAG, "UserLat -> ${Dog.longitude} ")
        var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?fields=price_level&location=${Dog.latitude}%2C${Dog.longitude}&radius=400&type=any&key=AIzaSyDV6aFItX960hrbAaI229-8iDa3xTZ-RXU"
        //Log.d(TAG, "https://maps.googleapis.com/maps/api/place/nearbysearch/json?fields=price_level&location=${userLat}%2C${userLong}&radius=2500&type=restaurant&key=AIzaSyDV6aFItX960hrbAaI229-8iDa3xTZ-RXU")
        var myJsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,url,null,
            {
                    response ->  try{
                var myJsonArray = response.getJSONArray("results")
                for(i in 0 until myJsonArray.length()){
                    var myJSONObject = myJsonArray.getJSONObject(i)
                    /*
                    val registro = LayoutInflater.from(this).inflate(R.layout.table_row_np,null,false)
                    val colName = registro.findViewById<View>(R.id.columnaNombre) as TextView
                    val colPrice = registro.findViewById<View>(R.id.columnaEmail) as TextView
                    val colLatitude = registro.findViewById<View>(R.id.colEditar)
                    val colBorrar = registro.findViewById<View>(R.id.colBorrar)
                    */


                    var name = myJSONObject.getString("name")
                    Log.d(TAG, "Nombre:  ${name}" )



                    //colPrice.text=myJSONObject.getString("price_level")


                    var geometry = myJSONObject.getJSONObject("geometry")
                    var location = geometry.getJSONObject("location")

                    lati = location.getString("lat").toDouble()
                    longi = location.getString("lng").toDouble()
                    Log.d(TAG, "Latitude: ${location.getString("lat")}")
                    Log.d(TAG, "Latitude: ${location.getString("lng")}")



                    //createMarker(lati, longi, name)

                    val loc2 = Location("")
                    loc2.latitude = lati
                    loc2.longitude = longi

                    distanceInMeters = Dog.distanceTo(loc2)
                    if(distanceInMeters < distance ){
                        distance = distanceInMeters.toDouble()
                        actualName = name
                        address = myJSONObject.getString("vicinity")

                        actualLocation = loc2

                    }

                    //addLine(currentLocation, loc2)





                    //colEditar.id=myJSONObject.getString("id").toInt()
                    //colBorrar.id=myJSONObject.getString("id").toInt()



                }

                //addLine(currentLocation, actualLocation)
                //createMarker(actualLocation.latitude, actualLocation.longitude, "Closest one")

                Log.d(TAG, "cargaTabla: ENOR DISTANCIA -> $distance")
                Log.d(TAG, "cargaTabla: PLACE NAME -> $actualName")
                Log.d(TAG, "cargaTabla: Address -> $address")

                binding.txtAddress.setText(address)

            }catch (e: JSONException){
                //e.printStackTrace()
                Log.d(TAG, "getAddressName: ${e.message}")
            }
            }, {
                    error ->
                Log.d(TAG, "Error: ${error}")

            })
        queue.add(myJsonObjectRequest)
    }

}