package com.rcmdev.decisionmaker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rcmdev.decisionmaker.databinding.ActivityMainBinding
import com.rcmdev.decisionmaker.models.Roulette
import com.rcmdev.decisionmaker.viewmodels.MainViewModel
import com.rcmdev.decisionmaker.views.OnRouletteViewListener
import java.net.URLEncoder
import java.util.*
import kotlin.math.*
import kotlin.random.Random

const val GLOBAL_ROULETTE = "GlobalRoulette"
const val GLOBAL_ROULETTE_LIST = "GlobalRouletteList"
const val PREFERENCES_FILE = "PreferencesFile"
const val SETTINGS_SOUND = "sound"
const val SETTINGS_SHAKE = "shake"
const val SETTINGS_COLOR = "color_scheme"

class MainActivity : AppCompatActivity(), OnRouletteViewListener, View.OnClickListener, AddEditFragment.OnSaveClicked {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null
    private var mp:MediaPlayer? = null
    private var sd = 0
    private var backPressedFragment: Boolean = false
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeViewModel()

        binding.roulette.onRouletteViewListener = this
        binding.buttonSpin.setOnClickListener(this)
        binding.searchButton.setOnClickListener(this)
        binding.shareButton.setOnClickListener(this)

        mp = MediaPlayer.create(this@MainActivity, R.raw.pop2)
        sd = (mp?.duration!!/1.5).toInt()

        initSensor()
    }

    /**
     * Subscribe to view model's observable objects.
     */
    private fun subscribeViewModel(){
        viewModel.rouletteTitle.observe(this, {rouletteTitle ->
            binding.textViewTitle.text = rouletteTitle
        })
        viewModel.optionsList.observe(this, {optionsList ->
            binding.roulette.setRouletteOptionList(optionsList, viewModel.colorScheme.toString())
        })
        viewModel.result.observe(this, {result ->
            binding.textViewResult.text = result
        })
        viewModel.rotation.observe(this, {rotation ->
            binding.roulette.setRouletteRotation(rotation)
        })
    }

    /**
     * Initialize variables related to the Sensor Event Listener.
     * To detect when the phone shakes, and spin the roulette.
     */
    private fun initSensor(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector()
        shakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener{
            override fun onShake(speed: Float) {
                if(viewModel.isShakeOn) {
                    spinRequested(speed)
                }
            }
        })
    }

    /**
     * OnRestart -> Clean screen when coming back from MyRoulettesActivity
     * with a new roulette selected
     */
    override fun onRestart() {
        super.onRestart()
        if(viewModel.isNewRouletteSelected){
            viewModel.isNewRouletteSelected = false
            binding.shareButton.visibility = View.INVISIBLE
            binding.searchButton.visibility = View.INVISIBLE
        }
    }

    /**
     * OnResume -> register Sensor Event Listener
     */
    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * OnPause -> unregister Sensor Event Listener
     */
    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(shakeDetector)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_modify -> {
                if(!checkConditions()) return false
                rouletteEditRequest(viewModel.roulette)
                true
            }
            R.id.menumain_showRoulettes -> {
                if(!checkConditions()) return false
                startActivity(Intent(this, MyRoulettesActivity::class.java))
                true
            }
            R.id.menu_settings -> {
                if(!checkConditions()) return false
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_rate -> {
                if(!checkConditions()) return false
                rateApp()
                true
            }
            R.id.menu_share -> {
                if(!checkConditions()) return false
                shareApp()
                true
            }
            R.id.menu_feedback -> {
                if(!checkConditions()) return false
                sendFeedback()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkConditions(): Boolean{
        return if(binding.roulette.isAnimationRunning()){
                    if(this::toast.isInitialized) toast.cancel()
                    toast = Toast.makeText(this, resources.getString(R.string.UNTIL_SPIN_COMPLETED), Toast.LENGTH_SHORT)
                    toast.show()
                    false
                }else true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(binding.roulette.isAnimationRunning()){
            binding.roulette.stopSpinning()
            viewModel.setRotation(0f)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        if (binding.textViewResult.text.isNotEmpty()) {
            binding.searchButton.visibility = View.VISIBLE
            binding.shareButton.visibility = View.VISIBLE
        }
        if( (supportFragmentManager.findFragmentById(R.id.fragment_container_view)) != null){
            showEditFragment()
        }
    }

    private fun rouletteEditRequest(roulette: Roulette){
        val newFragment = AddEditFragment.newInstance(roulette)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container_view, newFragment).commit()
        viewModel.deepCopy(roulette)
        showEditFragment()
    }

    private fun showEditFragment(){
        binding.fragmentContainerView.visibility = View.VISIBLE
        binding.roulette.visibility = View.GONE
        binding.buttonSpin.visibility = View.GONE
        binding.textViewResult.visibility = View.GONE
        binding.textViewTitle.visibility = View.GONE
        binding.shareButton.visibility = View.GONE
        binding.searchButton.visibility = View.GONE
    }

    private fun removeEditFragment(fragment: Fragment?){
        if(fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        binding.fragmentContainerView.visibility = View.GONE
        binding.roulette.visibility = View.VISIBLE
        binding.buttonSpin.visibility = View.VISIBLE
        binding.textViewTitle.visibility = View.VISIBLE
        binding.textViewResult.visibility = View.VISIBLE

        if(binding.textViewResult.text.isEmpty())
            backPressedFragment = false

        if(backPressedFragment){
            backPressedFragment = false
            binding.shareButton.visibility = View.VISIBLE
            binding.searchButton.visibility = View.VISIBLE
        }else {
            binding.shareButton.visibility = View.INVISIBLE
            binding.searchButton.visibility = View.INVISIBLE
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_spin -> {
                spinRequested(2.7f)
            }
            R.id.share_button->{
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Decision Maker [${Utils.getDate()}]\n${binding.textViewTitle.text}: ${binding.textViewResult.text} win!")
                    sendIntent.type = "text/plain"
                    startActivity(Intent.createChooser(sendIntent, null))

                }catch (e:Exception){
                    Toast.makeText(this, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.search_button->{
                val gsIntentUri = Uri.parse(String.format(resources.getString(R.string.URL_GOOGLE), URLEncoder.encode(binding.textViewResult.text.toString(), "UTF-8")))
                val googleSearchIntent = Intent(Intent.ACTION_VIEW, gsIntentUri)
                startActivity(googleSearchIntent)
            }
        }
    }

    private fun spinRequested(speed: Float){
        viewModel.setResult(resources.getString(R.string.EMPTY_STRING))
        binding.searchButton.visibility = View.INVISIBLE
        binding.shareButton.visibility = View.INVISIBLE
        binding.roulette.spin(7000, speed*(0.9f + Random.nextFloat()))
    }

    /**
     * Roulette spin animation End
     * @param idx is the picked numeric index in the choice array,
     * @param choice is the option picked
     */
    override fun onRouletteSpinCompleted(idx: Int, choice: String) {
        if(viewModel.isSoundOn) {
            val sp = MediaPlayer.create(this, R.raw.success)
            sp.start()
        }
        viewModel.setResult(choice.uppercase(Locale.ROOT))
        viewModel.setRotation(binding.roulette.getRouletteRotation())
        binding.searchButton.visibility = View.VISIBLE
        binding.shareButton.visibility = View.VISIBLE
    }

    override fun onRouletteSpinEvent(speed: Float) {
        if(binding.roulette.getRouletteOptionsCount() > 1 && abs(speed) > 0.09f) {
            viewModel.setResult(resources.getString(R.string.EMPTY_STRING))
            binding.searchButton.visibility = View.INVISIBLE
            binding.shareButton.visibility = View.INVISIBLE
            val t = (min(9000f, 7000f*abs(speed))).toLong()
            binding.roulette.spin(t, 1.1f*speed)
        }
    }

    private var lastOptionChangeTime = System.currentTimeMillis()
    override fun onRouletteOptionChanged() {
        if(viewModel.isSoundOn) {
            val t = System.currentTimeMillis()
            val dt = t - lastOptionChangeTime
            if (dt < sd) { return }
            lastOptionChangeTime = t

            mp?.seekTo(0)
            mp?.start()
        }
    }

    override fun onSaveClicked(roulette: Roulette) {
        viewModel.writeRoulette(roulette)
        viewModel.setResult(resources.getString(R.string.EMPTY_STRING))
        viewModel.setRotation(0f)
        removeEditFragment(supportFragmentManager.findFragmentById(R.id.fragment_container_view))
    }

    /**
     * Detect back key pressed.
     * When Fragment on screen, remove it without saving any changes in the Roulette.
     */
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if(fragment == null){
            super.onBackPressed()
        }else{
            if(viewModel.roulette != viewModel.oldRoulette){
                viewModel.swapRoulette()
            }
            backPressedFragment = true
            removeEditFragment(fragment)
        }
    }

    private fun rateApp(){
        try{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        }catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun shareApp(){
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, check out this App! https://play.google.com/store/apps/details?id=$packageName")
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share app via"))
    }

    private fun sendFeedback(){
        val emailArray:Array<String> = arrayOf("asdflolrichi@gmail.com")
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, emailArray)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Decision Maker App Feedback")
        startActivity(Intent.createChooser(intent, null))
    }

}