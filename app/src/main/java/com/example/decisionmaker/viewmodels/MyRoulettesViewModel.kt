package com.example.decisionmaker.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.decisionmaker.GLOBAL_ROULETTE
import com.example.decisionmaker.GLOBAL_ROULETTE_LIST
import com.example.decisionmaker.PREFERENCES_FILE
import com.example.decisionmaker.models.Roulette
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

private const val TAG = "MyRoulettesViewModel"

class MyRoulettesViewModel(application: Application) : AndroidViewModel(application) {

    private var jsonString:String? = null
    private val sharedPref = application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    //LiveData objects
    private val _rouletteList = MutableLiveData<ArrayList<Roulette>>()
    val rouletteList: LiveData<ArrayList<Roulette>>
        get() = _rouletteList

    var editRoulette: Roulette? = null

    init{
        //Retrieve the Roulette List form SharedPreferences
        jsonString = sharedPref.getString(GLOBAL_ROULETTE_LIST, null)
        Log.d(TAG, "init: jsonString is $jsonString")

        val type: Type = object : TypeToken<ArrayList<Roulette>>() {}.type
        val list: ArrayList<Roulette> = if(jsonString != null) {
                        GsonBuilder().create().fromJson(jsonString, type)
                    }else{ArrayList()}
        Log.d(TAG, "init: list is $list")

        _rouletteList.postValue(list)
    }

    private fun loadRoulettes(){
        Log.d(TAG, "loadRoulettes: starts")
        Log.d(TAG, "loadRoulettes: list is ${_rouletteList.value}")
        _rouletteList.postValue(_rouletteList.value)
    }

    //Function to insert or update a roulette when the add or edit buttons are tapped
    fun saveRoulette(roulette: Roulette){
        Log.d(TAG, "saveRoulette: starts")

        if(_rouletteList.value == null){
            _rouletteList.value = ArrayList()
        }

        /*  Check if the roulette has changed.
            If edited, delete the old roulette and add the new one. If adding new roulette, there's nothing to delete.
            This is just in case the Roulette name changes, the options list changes is managed by OptionsAdapter automatically. */
        if(editRoulette != roulette){
            if(editRoulette != null){
                deleteRoulette(editRoulette!!)
            }
            //Insert new roulette
            _rouletteList.value!!.add(0, roulette)
            _rouletteList.value = _rouletteList.value
            loadRoulettes()
        }
    }

    //Function to delete a Roulette
    private fun deleteRoulette(roulette: Roulette){
        Log.d(TAG, "deleteRoulette: starts")
        _rouletteList.value!!.removeAt(_rouletteList.value!!.indexOf(roulette))
    }

    //Function to save main roulette in SharedPreferences
    fun saveMainRoulette(roulette: Roulette){
        Log.d(TAG, "saveMainRoulette: starts")
        //Convert the Roulette to JSON string
        jsonString = GsonBuilder().create().toJson(roulette)
        //Save the string in SharedPreferences
        sharedPref.edit().putString(GLOBAL_ROULETTE, jsonString).apply()
    }

    //Save the Roulettes List in SharedPreferences when the ViewModel is destroyed
    override fun onCleared() {
        Log.d(TAG, "onCleared: starts")
        //Convert the RouletteList to JSON string
        jsonString = GsonBuilder().create().toJson(_rouletteList.value)
        //Save the string in SharedPreferences
        sharedPref.edit().putString(GLOBAL_ROULETTE_LIST, jsonString).apply()
    }
}