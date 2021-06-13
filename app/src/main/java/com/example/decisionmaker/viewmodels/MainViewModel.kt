package com.example.decisionmaker.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.decisionmaker.GLOBAL_ROULETTE
import com.example.decisionmaker.PREFERENCES_FILE
import com.example.decisionmaker.R
import com.example.decisionmaker.models.Roulette
import com.google.gson.GsonBuilder

private const val TAG = "MainViewModel"

class MainViewModel(application: Application): AndroidViewModel(application) {

    //Variable for Roulette object
    lateinit var roulette: Roulette
        private set

    lateinit var oldRoulette: Roulette
        private set

    private var jsonString:String? = null
    private val sharedPref = application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    var isNewRouletteSelected: Boolean = false

    //LiveData objects
    private val _rouletteTitle = MutableLiveData<String>()
    val rouletteTitle: LiveData<String>
        get() = _rouletteTitle

    private val _optionsList = MutableLiveData<ArrayList<String>>()
    val optionsList: LiveData<ArrayList<String>>
        get() = _optionsList

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    private val _rotation = MutableLiveData<Float>()
    val rotation: LiveData<Float>
        get() = _rotation

    //Observe Shared Preferences changes
    private val shPListener = SharedPreferences.OnSharedPreferenceChangeListener{_, key ->
        when(key){
            GLOBAL_ROULETTE -> {
                readRoulette()
                _result.value = getApplication<Application>().resources.getString(R.string.EMPTY_STRING)
                _rotation.value = 0f
                isNewRouletteSelected = true
            }
        }
    }

    init {
        Log.d(TAG, "MainViewModel: starts")
        //Register the SharedPreferences listener (Shared Preferences observer)
        sharedPref.registerOnSharedPreferenceChangeListener(shPListener)
        readRoulette()
    }

    private fun readRoulette(){
        Log.d(TAG, "readRoulette: starts")
        jsonString = sharedPref.getString(GLOBAL_ROULETTE, null)

        roulette = if(jsonString != null){
            GsonBuilder().create().fromJson(jsonString, Roulette::class.java)
        }else {
            getDefaultRoulette()
        }

        //Update LiveData
        _rouletteTitle.value = roulette.name
        _optionsList.value = roulette.options
    }

    fun writeRoulette(roulette: Roulette){
        Log.d(TAG, "writeRoulette: starts")
        jsonString = GsonBuilder().create().toJson(roulette)
        sharedPref.edit().putString(GLOBAL_ROULETTE, jsonString).apply()
    }

    //Function to build default roulette (just the first time the app is installed)
    private fun getDefaultRoulette(): Roulette {
        //Build options list
        val rouletteOptions:ArrayList<String> = ArrayList()
        rouletteOptions.add("Tacos")
        rouletteOptions.add("Pizza")
        rouletteOptions.add("Sushi")
        rouletteOptions.add("Burger")
        rouletteOptions.add("Hot-dogs")

        //Return Roulette object
        return Roulette(getApplication<Application>().resources.getString(R.string.ROULETTE_INIT_TITLE), rouletteOptions)
    }

    fun setResult(result: String){
        _result.value = result
    }

    fun setRotation(rotation: Float){
        _rotation.value = rotation
    }

    fun deepCopy(roulette: Roulette){
        jsonString = GsonBuilder().create().toJson(roulette)
        oldRoulette = GsonBuilder().create().fromJson(jsonString, Roulette::class.java)
    }

    fun swapRoulette(){
        roulette = oldRoulette
        //Update LiveData
        _rouletteTitle.value = roulette.name
        _optionsList.value = roulette.options
    }

    //Unsubscribe the observers, to avoid memory leaks
    override fun onCleared() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(shPListener)
    }
}