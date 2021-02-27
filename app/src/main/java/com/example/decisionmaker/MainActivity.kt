package com.example.decisionmaker

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
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
        const val MAIN_ACT_ROULETTE_ROTATION: String = "ROULETTE_ROTATION"
        const val MAIN_ACT_TEXTVIEW_RESULT: String = "TEXTVIEW_RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val rouletteOptions:ArrayList<String> = ArrayList()
        rouletteOptions.add("Tacos")
        rouletteOptions.add("Pizza")
        rouletteOptions.add("Sushi")
        rouletteOptions.add("Tortas")
        rouletteOptions.add("Hot-dogs")
        roulette.setRouletteOptionList(rouletteOptions)
        textView_title.text ="¿Qué comemos?"

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
        sd = (mp?.duration!!/1.5).toInt()

        //Listener to clearFocus on editText_title, after user modifies the Title
        /*textView_title.setOnEditorActionListener { view, actionId, event ->
            when(actionId){
                EditorInfo.IME_ACTION_DONE -> {
                    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                    textView_title.clearFocus()
                    true
                }
                else -> false
            }
        }*/
    }

    //Menu overridden methods
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem = menu?.findItem(R.id.menu_modify)
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_edit, theme)
        val styledArray = obtainStyledAttributes(R.style.Theme_DecisionMaker, intArrayOf(R.attr.backgroundTint))
        val color = styledArray.getColor(0, Color.CYAN)
        styledArray.recycle()
        icon?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        menuItem?.icon = icon
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_modify -> {
                //Launch next activity
                if(roulette.isAnimationRunning()){
                    Toast.makeText(this, "Wait until roulette spin completed", Toast.LENGTH_SHORT).show()
                    return false
                }
                val intent = Intent(this, OptionsActivity::class.java)
                intent.putExtra(OptionsActivity.OPTIONS_ACT_ROULETTE_LIST, roulette.getRouletteOptionList())
                intent.putExtra(OptionsActivity.OPTIONS_ACT_ROULETTE_TITLE, textView_title.text.toString())
                startActivityForResult(intent, ROULETTE_OPTIONS_REQUEST_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Manage screen orientation changes
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(OptionsActivity.OPTIONS_ACT_ROULETTE_LIST, roulette.getRouletteOptionList())
        outState.putFloat(MAIN_ACT_ROULETTE_ROTATION, roulette.getRouletteRotation())
        outState.putString(MAIN_ACT_TEXTVIEW_RESULT, textView_result.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        roulette.setRouletteRotation(savedInstanceState.getFloat(MAIN_ACT_ROULETTE_ROTATION))
        roulette.setRouletteOptionList(savedInstanceState.getStringArrayList(OptionsActivity.OPTIONS_ACT_ROULETTE_LIST)!!)
        textView_result.text = savedInstanceState.getString(MAIN_ACT_TEXTVIEW_RESULT)
    }

    /**
     * Get Activity Result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            ROULETTE_OPTIONS_REQUEST_CODE -> {                                  //Check for OptionsActivity result
                if(resultCode == OptionsActivity.OPTIONS_ACT_ROULETTE_UPD_OK){  //Roulette option list updated correctly
                    roulette.setRouletteOptionList(data?.getStringArrayListExtra(OptionsActivity.OPTIONS_ACT_ROULETTE_LIST)!!)
                    textView_title.text = data.getStringExtra(OptionsActivity.OPTIONS_ACT_ROULETTE_TITLE)
                    textView_result.text = resources.getString(R.string.EMPTY_STRING)
                    roulette.setRouletteRotation(0f)
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
        if(roulette.getRouletteOptionsCount() > 1 && abs(speed) > 0.09f) {
            textView_result.text = resources.getString(R.string.EMPTY_STRING)
            val t = (min(9000f, 7000f*abs(speed))).toLong()
            roulette.spin(t, 1.1f*speed)
        }
    }

    val handler = Handler(Looper.myLooper()!!)


    var lastOptionChangeTime = System.currentTimeMillis()

    override fun OnRouletteOptionChanged() {
        val t = System.currentTimeMillis()
        val dt = t-lastOptionChangeTime
        if(dt < sd){
            return
        }
        lastOptionChangeTime = t

        mp?.seekTo(0)
        mp?.start()
    }
}