package com.yazid.walkapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.location.LocationRequest
import android.location.LocationRequest.QUALITY_HIGH_ACCURACY
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telecom.Call
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.Response
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.yazid.walkapp.View_model.myViewmodel
import com.yazid.walkapp.ui.theme.WalkappTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
    fun getRoute(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyClp46OnfM7iTVhkcqm0fqaGsc6VVsmmfY"
        val urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"
        Log.e("newroad",urlString)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                val inputStream: InputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                bufferedReader.close()
                val jsonResponse = stringBuilder.toString()
                val route = getRouteFromJson(jsonResponse)
                viewmodel.set_new_des(route)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRouteFromJson(jsonResponse: String): List<LatLng> {
        val pointsList = mutableListOf<LatLng>()
        val jsonObject = JSONObject(jsonResponse)
        val routes = jsonObject.getJSONArray("routes")
        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val steps = legs.getJSONObject(0).getJSONArray("steps")

        for (i in 0 until steps.length()) {
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            val decodedPoints = PolyUtil.decode(points)
            pointsList.addAll(decodedPoints.map { LatLng(it.latitude, it.longitude) })
        }

        return pointsList
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get permission
        requestPermission()
        viewmodel.getDataFromFireBase_vm()
        GetLocation()



         setContent {
             var showOptions by remember { mutableStateOf(false) }

             //false data to avoid null
             val initlist:MutableList<LatLng> = mutableListOf()
             initlist.add(LatLng(30.000,30.000))
             initlist.add(LatLng(40.000,40.000))

             val road_= remember{
                mutableStateOf<List<LatLng>>(initlist)
            }

             val parkingStore_= remember{
                 mutableStateOf<List<LatLng>>(initlist)
             }

             val DesMarkvisibility = remember {
                 mutableStateOf(false)
             }
             val DesState = rememberMarkerState(
                 position = LatLng(0.0, 0.0)
             )

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
                    viewmodel.ParkingPoints.observe(this, Observer {
                        parkingStore_.value=it
                    })
                    viewmodel.road.observe(this, Observer {
                        road_.value=it


                    })

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                    ) {

                        Row (
                            modifier = Modifier
                                .height(100.dp)
                                .background(androidx.compose.ui.graphics.Color.Black)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center


                        ){
                            IconButton(onClick = {

                                cameraPositionState.position=CameraPosition.fromLatLngZoom(markerState.position, 10f)

                            },
                                ) {
                                Icon(painter = painterResource(
                                    id = R.drawable.reshot_icon_location_map_marker_6uybmp8j95),
                                    contentDescription = "s",
                                    tint = androidx.compose.ui.graphics.Color.White)
                            }

                            IconButton(onClick = {

                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/mahmoudyazid/")))

                            }  ,


                                ) {
                                Icon(painter = painterResource(id = R.drawable.profile_picture_svgrepo_com),
                                    contentDescription ="s",
                                    tint = androidx.compose.ui.graphics.Color.White)
                            }
                        }


                        GoogleMap(
                            modifier = Modifier.fillMaxHeight(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = {
                                DesState.position= it
                                DesMarkvisibility.value=true
                                showOptions=true
                                getRoute(markerState.position,DesState.position)

                            },
                            properties = MapProperties(
                                mapType = MapType.TERRAIN
                            )

                        ){

                            val vectorDrawable = ContextCompat.getDrawable(LocalContext.current, R.drawable.reshot_icon_location_map_marker_6uybmp8j95)

                            val bitmap = vectorDrawable?.toBitmap()

                            val bitmapDescriptor = bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }

                            val vectorDrawable2 = ContextCompat.getDrawable(LocalContext.current, R.drawable.icons8_google_maps)

                            val bitmap2 = vectorDrawable2?.toBitmap()

                            val bitmapDescriptor2 = bitmap2?.let { BitmapDescriptorFactory.fromBitmap(it) }


                            // parking area
                            val vectorDrawable3 = ContextCompat.getDrawable(LocalContext.current, R.drawable.parking_sign_2526)

                            val bitmap3 = vectorDrawable3?.toBitmap()

                            val bitmapDescriptor3 = bitmap3?.let { BitmapDescriptorFactory.fromBitmap(it) }


                            Marker(
                                state = markerState,
                                title = "Marker1",
                                snippet = "Marker in Singapore",
                                icon = bitmapDescriptor
                            )

                            Marker(
                                state = DesState,
                                title = "Marker1",
                                snippet = "Marker in Singapore",
                                icon = bitmapDescriptor2,
                                visible = DesMarkvisibility.value,

                                )
                            if (showOptions) {
                                OptionsDialog(
                                    markerPosition = DesState.position,
                                    onDismiss = { showOptions = false },
                                    onDisappear = {
                                        DesMarkvisibility.value=false

                                    },
                                    viewmodel

                                )
                            }

                            Polyline(
                                points = road_.value
                                ,color = androidx.compose.ui.graphics.Color.Blue,
                                visible =  DesMarkvisibility.value

                            )

                            parkingStore_.value.forEach {
                                Marker(
                                    state = rememberMarkerState(
                                        position =it

                                    )

                                    ,
                                    title = "Marker1",
                                    snippet = "Marker in Singapore",
                                    icon = bitmapDescriptor3,
                                    visible =true,



                                    )
                            }



                        }
                    }









                }
            }
        }


    }


}
@Composable
fun OptionsDialog(markerPosition: LatLng, onDismiss: () -> Unit,onDisappear: () -> Unit , viewModelInst:myViewmodel) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
        ) {
            Text(text = "Options")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Handle option 1 click
                    // For example, open a dialog or perform an action
                    viewModelInst.AddPointToMap_vm(

                        markerPosition
                    )
                }
            ) {
                Text(text = "set free place to park")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // Handle option 2 click
                    // For example, open a dialog or perform an action
                    onDismiss()
                }
            ) {
                Text(text = "go to there")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // Handle option 2 click
                    // For example, open a dialog or perform an action
                    onDisappear()
                    onDismiss()
                }
            ) {
                Text(text = "close")
            }
        }
    }

}