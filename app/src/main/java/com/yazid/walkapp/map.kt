package com.yazid.walkapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.location.LocationRequest
import android.location.LocationRequest.QUALITY_HIGH_ACCURACY
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.yazid.walkapp.View_model.myViewmodel
import com.yazid.walkapp.ui.theme.WalkappTheme

class map : ComponentActivity() {
    val viewmodel:myViewmodel by viewModels()
    @SuppressLint("MissingPermission")
    fun GetLocation(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 2 // in milliseconds
            fastestInterval = 2 // in milliseconds
            priority = LocationRequest.QUALITY_HIGH_ACCURACY
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation = locationResult.lastLocation
                viewmodel.location.value =lastLocation
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,callback, Looper.getMainLooper())

    }


    val requestPermCallback = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it){
            GetLocation()
        }
        else{

        }
    }
    fun requestPermission() {

        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,                 android.Manifest.permission.ACCESS_FINE_LOCATION) -> {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("we need permission")
                builder.setMessage("we need permission for gps")
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    requestPermCallback.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

                }

                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                   dialog?.dismiss()
                }


                builder.show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermCallback.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get permission
        requestPermission()

        GetLocation()



         setContent {

             val markerState = rememberMarkerState(
                 position = LatLng(30.000, 30.000)
             )
             val cameraPositionState = rememberCameraPositionState {
                 position = CameraPosition.fromLatLngZoom(markerState.position, 10f)
             }
             WalkappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    viewmodel.location.observe(this, Observer {
                        markerState.position= LatLng(it.latitude,it.longitude)


                    })


                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ){
                        Marker(
                            state = markerState,
                            title = "Marker1",
                            snippet = "Marker in Singapore",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }





                }
            }
        }
    }


}

