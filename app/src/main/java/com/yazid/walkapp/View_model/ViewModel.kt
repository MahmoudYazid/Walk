package com.yazid.walkapp.View_model

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationResult

class myViewmodel:ViewModel(){
    val location: MutableLiveData<Location> = MutableLiveData()


}