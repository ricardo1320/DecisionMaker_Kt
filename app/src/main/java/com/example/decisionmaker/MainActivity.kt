package com.example.decisionmaker

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.decisionmaker.views.OnRouletteViewListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.random.Random

//Tag for LOG
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnRouletteViewListener {
    var mp:MediaPlayer? = null
    var sd = 0

    companion object{
        const val ROULETTE_OPTIONS_REQUEST_CODE = 10
    }

    private var rouletteOptions:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rouletteOptions.add("Tacos")
        rouletteOptions.add("Pizza")
        rouletteOptions.add("Sushi")
        rouletteOptions.add("Tortas")
        rouletteOptions.add("Hot-dogs")

        roulette.setRouletteOptionList(rouletteOptions)
        roulette.onRouletteViewListener = this


        //Click listener for button_spin (spin the roulette)
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_spin -> {
                    textView_result.text = resources.getString(R.string.EMPTY_STRING)
                    roulette.spin(7000, 2.7f*(0.9f + Random.nextFloat()))
                }
            }
        }

        button_spin.setOnClickListener(listener)
        mp = MediaPlayer.create(this@MainActivity, R.raw.pop2)
        sd = (mp?.duration!!/2).toInt()
    }

    //onResume -> clear the result textView, when returning from OptionsActivity
    override fun onResume() {
        textView_result.text = resources.getString(R.string.EMPTY_STRING)
        super.onResume()
    }


    //Menu overridden methods
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_modify -> {
                //Launch next activity
                val intent = Intent(this, OptionsActivity::class.java)
                intent.putExtra(OptionsActivity.OPTIONS_ACT_ROULETTE_LIST, rouletteOptions)
                startActivityForResult(intent, ROULETTE_OPTIONS_REQUEST_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Get Activity Result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            ROULETTE_OPTIONS_REQUEST_CODE -> { //Check for OptionsActivity result
                if(resultCode == OptionsActivity.OPTIONS_ACT_ROULETTE_UPD_OK){ //Roulette option list updated correctly
                    if(data != null){ //Overwrite list
                        rouletteOptions = data.getStringArrayListExtra(OptionsActivity.OPTIONS_ACT_ROULETTE_LIST)!!
                        roulette.setRouletteOptionList(rouletteOptions)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Roulette spin animation End
     * @param idx is the picked numeric index in the choice array,
     * @param choice is the option picked
     */
    override fun OnRouletteSpinCompleted(idx: Int, choice: String) {
        val sp = MediaPlayer.create(this, R.raw.success)
        sp.start()
        textView_result.text = choice.toUpperCase(Locale.ROOT)
    }

    override fun OnRouletteSpinEvent(speed: Float) {
        Log.d("ROULETTE SPIN EVT", speed.toString())
        if(abs(speed) > 0.075f) {
            textView_result.text = resources.getString(R.string.EMPTY_STRING)
            val t = (min(9000f, 7000f*abs(speed))).toLong()
            roulette.spin(t, 1.25f*speed)
        }
    }

    val handler = Handler(Looper.myLooper()!!)

    /*val runT = object:Runnable {
        override fun run() {

            handler.postDelayed(this, 60)
        }
    }
    handler.postDelayed(runT, 1000)*/

    var lastOptionChangeTime = System.currentTimeMillis()

    override fun OnRouletteOptionChanged() {
        var t = System.currentTimeMillis()
        val dt = t-lastOptionChangeTime
        if(dt < sd){
            return
        }
        lastOptionChangeTime = t

            mp?.seekTo(0)
            mp?.start()


    }
}