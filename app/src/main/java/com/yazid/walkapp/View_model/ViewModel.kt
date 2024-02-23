package com.yazid.walkapp.View_model

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yazid.walkapp.View_model.model.firebaseSt
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class myViewmodel:ViewModel(){
    val location: MutableLiveData<Location> = MutableLiveData()
    val road: MutableLiveData<List<LatLng>> = MutableLiveData()
    val initlist:MutableList<LatLng> = mutableListOf(
        LatLng(30.000,30.000),
        LatLng(30.000,30.000)
    )

    val ParkingPoints: MutableLiveData<List<LatLng>> = MutableLiveData(initlist)
    suspend fun set_new_des(list:List<LatLng>){
        withContext(Dispatchers.Main){
            road.value=list

        }


    }

    fun getDataFromFireBase_vm(){
        viewModelScope.launch(){
            val tepList:MutableList<LatLng> = mutableListOf()
            Firebase.firestore.collection("places_fb").addSnapshotListener{
                snap,e->
                snap?.forEach {
                    val newitem= LatLng(
                        it.get("lat").toString().toDouble(),
                        it.get("long").toString().toDouble()

                    )
                    tepList.add(newitem)
                    Log.e("NewArr",tepList.toString())

                }





            }
            withContext(Dispatchers.Main){

                ParkingPoints.value=tepList
            }
        }


    }

    fun AddPointToMap_vm(data:LatLng){
        viewModelScope.launch(){
            val newdata = HashMap<String, Any>()
            newdata["lat"] = data.latitude .toString()
            newdata["long"] = data.longitude.toString()

            Firebase.firestore.collection("places_fb").add(
                newdata

            )

        }


    }
}