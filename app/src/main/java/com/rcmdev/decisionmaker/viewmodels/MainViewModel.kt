package com.rcmdev.decisionmaker.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rcmdev.decisionmaker.*
import com.rcmdev.decisionmaker.models.Roulette
import com.google.gson.GsonBuilder

class MainViewModel(application: Application): AndroidViewModel(application) {

    var isSoundOn: Boolean = false
        private set
    var isShakeOn: Boolean = false
        private set
    var colorScheme: String? = null
        private set

    lateinit var roulette: Roulette
        private set

    lateinit var oldRoulette: Roulette
        private set

    private var jsonString:String? = null
    private val sharedPref = application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    var isNewRouletteSelected: Boolean = false

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

    private val shPListener = SharedPreferences.OnSharedPreferenceChangeListener{_, key ->
        when(key){
            GLOBAL_ROULETTE -> {
                readRoulette()
                _result.value = ""
                _rotation.value = 0f
                isNewRouletteSelected = true
            }
            SETTINGS_SOUND -> { readSound() }
            SETTINGS_SHAKE -> { readShake() }
            SETTINGS_COLOR -> { readColor() }
        }
    }

    init {
        sharedPref.registerOnSharedPreferenceChangeListener(shPListener)
        readSettings()
        readRoulette()
    }

    private fun readSettings(){
        readSound()
        readShake()
        readColor()
    }

    private fun readSound(){ isSoundOn = sharedPref.getBoolean(SETTINGS_SOUND, false) }

    private fun readShake(){ isShakeOn = sharedPref.getBoolean(SETTINGS_SHAKE, false) }

    private fun readColor(){ colorScheme = sharedPref.getString(SETTINGS_COLOR, "red") }

    private fun readRoulette(){
        jsonString = sharedPref.getString(GLOBAL_ROULETTE, null)

        roulette = if(jsonString != null){
            GsonBuilder().create().fromJson(jsonString, Roulette::class.java)
        }else {
            getDefaultRoulette()
        }

        _rouletteTitle.value = roulette.name
        _optionsList.value = roulette.options
    }

    fun writeRoulette(roulette: Roulette){
        jsonString = GsonBuilder().create().toJson(roulette)
        sharedPref.edit().putString(GLOBAL_ROULETTE, jsonString).apply()
    }

    private fun getDefaultRoulette(): Roulette {
        val rouletteOptions:ArrayList<String> = ArrayList()
        rouletteOptions.add("Tacos")
        rouletteOptions.add("Pizza")
        rouletteOptions.add("Sushi")
        rouletteOptions.add("Burger")
        rouletteOptions.add("Hot-dogs")
        return Roulette(getApplication<Application>().resources.getString(R.string.ROULETTE_INIT_TITLE), rouletteOptions)
    }

    fun setResult(result: String){ _result.value = result }

    fun setRotation(rotation: Float){ _rotation.value = rotation }

    fun deepCopy(roulette: Roulette){
        jsonString = GsonBuilder().create().toJson(roulette)
        oldRoulette = GsonBuilder().create().fromJson(jsonString, Roulette::class.java)
    }

    fun swapRoulette(){
        roulette = oldRoulette
        _rouletteTitle.value = roulette.name
        _optionsList.value = roulette.options
    }

    override fun onCleared() { sharedPref.unregisterOnSharedPreferenceChangeListener(shPListener) }
}