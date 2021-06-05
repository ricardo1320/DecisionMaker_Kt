package com.example.decisionmaker

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.decisionmaker.views.OnRouletteViewListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.fragment_container_view
import kotlinx.android.synthetic.main.activity_my_roulettes.*
import kotlinx.android.synthetic.main.content_my_roulettes.*
import java.lang.Exception
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.random.Random

//Tag for LOG
private const val TAG = "MainActivity"

//Constants for saving values in Shared Preferences
const val GLOBAL_ROULETTE = "GlobalRoulette"
const val GLOBAL_ROULETTE_LIST = "GlobalRouletteList"
const val PREFERENCES_FILE = "PreferencesFile"

class MainActivity : AppCompatActivity(), OnRouletteViewListener, View.OnClickListener, AddEditFragment.OnSaveClicked {
    var mp:MediaPlayer? = null
    var sd = 0

    private lateinit var mainRoulette: Roulette

    companion object{
        const val MAIN_ACT_ROULETTE_ROTATION: String = "ROULETTE_ROTATION"
        const val MAIN_ACT_TEXTVIEW_RESULT: String = "TEXTVIEW_RESULT"
        const val OPTIONS_ACT_ROULETTE_LIST:String = "ROULETTE_LIST"
        const val OPTIONS_ACT_ROULETTE_TITLE:String = "ROULETTE_TITLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Get roulette from SharedPreferences or the default roulette
        mainRoulette = getRoulette()

        //Set the values from roulette object
        roulette.setRouletteOptionList(mainRoulette.options)
        textView_title.text = mainRoulette.name

        roulette.onRouletteViewListener = this

        button_spin.setOnClickListener(this)
        search_button.setOnClickListener(this)
        share_button.setOnClickListener(this)

        mp = MediaPlayer.create(this@MainActivity, R.raw.pop2)
        sd = (mp?.duration!!/1.5).toInt()
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: starts")

        //Clean screen, when coming back from MyRoulettesActivity
        textView_result.text = resources.getString(R.string.EMPTY_STRING)
        search_button.visibility = View.INVISIBLE
        share_button.visibility = View.INVISIBLE
        roulette.setRouletteRotation(0f)
    }

    //Menu overridden methods
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_modify -> {
                //Launch AddEditFragment
                if(roulette.isAnimationRunning()){
                    Toast.makeText(this, resources.getString(R.string.UNTIL_SPIN_COMPLETED), Toast.LENGTH_SHORT).show()
                    return false
                }
                rouletteEditRequest(mainRoulette)
                true
            }
            R.id.menumain_showRoulettes -> {
                //Launch next activity
                if(roulette.isAnimationRunning()){
                    Toast.makeText(this, resources.getString(R.string.UNTIL_SPIN_COMPLETED), Toast.LENGTH_SHORT).show()
                    return false
                }
                startActivity(Intent(this, MyRoulettesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Manage screen orientation changes
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(OPTIONS_ACT_ROULETTE_LIST, roulette.getRouletteOptionList())
        outState.putFloat(MAIN_ACT_ROULETTE_ROTATION, roulette.getRouletteRotation())
        outState.putString(MAIN_ACT_TEXTVIEW_RESULT, textView_result.text.toString())
        outState.putString(OPTIONS_ACT_ROULETTE_TITLE, textView_title.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        roulette.setRouletteRotation(savedInstanceState.getFloat(MAIN_ACT_ROULETTE_ROTATION))
        roulette.setRouletteOptionList(savedInstanceState.getStringArrayList(OPTIONS_ACT_ROULETTE_LIST)!!)
        textView_result.text = savedInstanceState.getString(MAIN_ACT_TEXTVIEW_RESULT)
        textView_title.text = savedInstanceState.getString(OPTIONS_ACT_ROULETTE_TITLE)
        if (textView_result.text.isNotEmpty()) {
            search_button.visibility = View.VISIBLE
            share_button.visibility = View.VISIBLE
        }

        if( (supportFragmentManager.findFragmentById(R.id.fragment_container_view)) != null){
            showEditFragment()
        }
    }

    //On resume, re-read and re-set the global roulette from SharedPreferences
    override fun onResume() {
        Log.d(TAG, "onResume: starts")
        super.onResume()

        mainRoulette = getRoulette()
        Log.d(TAG, "onResume: roulette is $mainRoulette")
        //Set the values from roulette object
        roulette.setRouletteOptionList(mainRoulette.options)
        textView_title.text = mainRoulette.name
    }

    //Function to get Global Roulette from SharedPreferences
    private fun getRoulette(): Roulette {
        val sharedPref = application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString(GLOBAL_ROULETTE, null)

        return if (jsonString != null) {
            GsonBuilder().create().fromJson(jsonString, Roulette::class.java)
        } else {
            getDefaultRoulette()
        }
    }

    //Function to build default roulette (just the first time the app is installed)
    private fun getDefaultRoulette(): Roulette{
        //Build options list
        val rouletteOptions:ArrayList<String> = ArrayList()
        rouletteOptions.add("Tacos")
        rouletteOptions.add("Pizza")
        rouletteOptions.add("Sushi")
        rouletteOptions.add("Burger")
        rouletteOptions.add("Hot-dogs")

        //Return Roulette object
        return Roulette(resources.getString(R.string.ROULETTE_INIT_TITLE), rouletteOptions)
    }

    private fun rouletteEditRequest(roulette: Roulette){
        Log.d(TAG, "rouletteEditRequest: starts")

        //Create a new fragment to edit the Roulette
        val newFragment = AddEditFragment.newInstance(roulette)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container_view, newFragment).commit()

        showEditFragment()
        Log.d(TAG, "Exiting rouletteEditRequest")
    }

    private fun showEditFragment(){
        fragment_container_view.visibility = View.VISIBLE
        roulette.visibility = View.GONE
        button_spin.visibility = View.GONE
        textView_result.visibility = View.GONE
        textView_title.visibility = View.GONE
        share_button.visibility = View.GONE
        search_button.visibility = View.GONE
    }

    private fun removeEditFragment(fragment: Fragment?){
        if(fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        fragment_container_view.visibility = View.GONE
        roulette.visibility = View.VISIBLE
        button_spin.visibility = View.VISIBLE
        textView_title.visibility = View.VISIBLE
        textView_result.visibility = View.VISIBLE
        share_button.visibility = View.INVISIBLE
        search_button.visibility = View.INVISIBLE
        roulette.setRouletteRotation(0f)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_spin -> {
                textView_result.text = resources.getString(R.string.EMPTY_STRING)
                search_button.visibility = View.INVISIBLE
                share_button.visibility = View.INVISIBLE
                roulette.spin(7000, 2.7f*(0.9f + Random.nextFloat()))
            }
            R.id.share_button->{
                try {
                    /*val bitmap = Bitmap.createBitmap(roulette.width, roulette.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    roulette.draw(canvas)
                    var uri:Uri? = null
                    val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share.png")
                    val outStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                    outStream.close()
                    uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
                    val sendIntent = Intent(android.content.Intent.ACTION_SEND)
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    sendIntent.setType("image/png")
                    startActivity(sendIntent)*/
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Decision Maker [${Utils.getDate()}]\n${textView_title.text}: ${textView_result.text} win!")
                    sendIntent.type = "text/plain"
                    startActivity(Intent.createChooser(sendIntent, null))

                }catch (e:Exception){
                    Log.e(TAG, ".onClick: share_button error is ${e.printStackTrace()}")
                }
            }
            R.id.search_button->{
                val gsIntentUri = Uri.parse(String.format(resources.getString(R.string.URL_GOOGLE), URLEncoder.encode(textView_result.text.toString(), "UTF-8")))
                val googleSearchIntent = Intent(Intent.ACTION_VIEW, gsIntentUri)
                startActivity(googleSearchIntent)
            }
        }
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
        search_button.visibility = View.VISIBLE
        share_button.visibility = View.VISIBLE
    }

    override fun OnRouletteSpinEvent(speed: Float) {
        Log.d(TAG, ".OnRouletteSpinEvent: speed is $speed")
        if(roulette.getRouletteOptionsCount() > 1 && abs(speed) > 0.09f) {
            textView_result.text = resources.getString(R.string.EMPTY_STRING)
            search_button.visibility = View.INVISIBLE
            share_button.visibility = View.INVISIBLE
            val t = (min(9000f, 7000f*abs(speed))).toLong()
            roulette.spin(t, 1.1f*speed)
        }
    }

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

    //Callback function from AddEditFragment
    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts")
        mainRoulette = getRoulette()
        //Set the values from roulette object
        roulette.setRouletteOptionList(mainRoulette.options)
        textView_title.text = mainRoulette.name

        textView_result.text = resources.getString(R.string.EMPTY_STRING)

        removeEditFragment(supportFragmentManager.findFragmentById(R.id.fragment_container_view))
    }

    /**
     * Detect back key pressed and disable it, when Fragment on screen.
     * (avoid problems)
     */
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if(fragment == null){
            super.onBackPressed()
        }else{
            Log.d(TAG, "Back pressed with fragment on screen")
        }
    }

}