package com.rcmdev.decisionmaker.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rcmdev.decisionmaker.GLOBAL_ROULETTE
import com.rcmdev.decisionmaker.GLOBAL_ROULETTE_LIST
import com.rcmdev.decisionmaker.PREFERENCES_FILE
import com.rcmdev.decisionmaker.models.Roulette
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MyRoulettesViewModel(application: Application) : AndroidViewModel(application) {

    private var jsonString:String? = null
    private val sharedPref = application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    private val _rouletteList = MutableLiveData<ArrayList<Roulette>>()
    val rouletteList: LiveData<ArrayList<Roulette>>
        get() = _rouletteList

    var editRoulette: Roulette? = null

    init{
        jsonString = sharedPref.getString(GLOBAL_ROULETTE_LIST, null)

        val type: Type = object : TypeToken<ArrayList<Roulette>>() {}.type
        val list: ArrayList<Roulette> = if(jsonString != null) {
                        GsonBuilder().create().fromJson(jsonString, type)
                    }else{ ArrayList() }

        _rouletteList.postValue(list)
    }

    private fun loadRoulettes(){ _rouletteList.postValue(_rouletteList.value) }

    fun saveRoulette(roulette: Roulette){
        if(_rouletteList.value == null){ _rouletteList.value = ArrayList() }

        if(editRoulette != roulette){
            if(editRoulette != null){ deleteRoulette(editRoulette!!) }

            _rouletteList.value!!.add(0, roulette)
            _rouletteList.value = _rouletteList.value
            loadRoulettes()
        }
    }

    private fun deleteRoulette(roulette: Roulette){
        _rouletteList.value!!.removeAt(_rouletteList.value!!.indexOf(roulette))
    }

    fun saveMainRoulette(roulette: Roulette){
        jsonString = GsonBuilder().create().toJson(roulette)
        sharedPref.edit().putString(GLOBAL_ROULETTE, jsonString).apply()
    }

    override fun onCleared() {
        jsonString = GsonBuilder().create().toJson(_rouletteList.value)
        sharedPref.edit().putString(GLOBAL_ROULETTE_LIST, jsonString).apply()
    }
}