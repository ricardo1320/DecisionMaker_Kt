package com.example.decisionmaker

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    fun saveRoulette(roulette: Roulette): Roulette{
        Log.d(TAG, "saveRoulette: starts")

        if(_rouletteList.value == null){
            _rouletteList.value = ArrayList()
        }

        //Insert new roulette
        _rouletteList.value!!.add(roulette)
        _rouletteList.value = _rouletteList.value

        loadRoulettes()
        return roulette
    }

    //Function to delete a Roulette
    fun deleteRoulette(roulette: Roulette?){
        Log.d(TAG, "deleteRoulette: starts")
        //If roulette is NULL, then don't delete anything
        if(roulette != null){
            _rouletteList.value!!.removeAt(_rouletteList.value!!.indexOf(roulette))
        }
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