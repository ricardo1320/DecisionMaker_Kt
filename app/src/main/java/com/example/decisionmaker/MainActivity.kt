package com.example.decisionmaker

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
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.example.decisionmaker.databinding.ActivityMainBinding
import com.example.decisionmaker.models.Roulette
import com.example.decisionmaker.viewmodels.MainViewModel
import com.example.decisionmaker.views.OnRouletteViewListener
import java.lang.Exception
import java.net.URLEncoder
import java.util.*
import kotlin.math.*
import kotlin.random.Random

private const val TAG = "MainActivity"

//Constants for saving values in Shared Preferences
const val GLOBAL_ROULETTE = "GlobalRoulette"
const val GLOBAL_ROULETTE_LIST = "GlobalRouletteList"
const val PREFERENCES_FILE = "PreferencesFile"

class MainActivity : AppCompatActivity(), OnRouletteViewListener, View.OnClickListener, AddEditFragment.OnSaveClicked {
    private var mp:MediaPlayer? = null
    private var sd = 0
    private var backPressedFragment: Boolean = false

    //Variable for view binding
    private lateinit var binding: ActivityMainBinding

    //View Model
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: starts")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Subscribe to ViewModel
        subscribeViewModel()

        binding.roulette.onRouletteViewListener = this
        binding.buttonSpin.setOnClickListener(this)
        binding.searchButton.setOnClickListener(this)
        binding.shareButton.setOnClickListener(this)

        mp = MediaPlayer.create(this@MainActivity, R.raw.pop2)
        sd = (mp?.duration!!/1.5).toInt()
    }

    private fun subscribeViewModel(){
        viewModel.rouletteTitle.observe(this, {rouletteTitle ->
            binding.textViewTitle.text = rouletteTitle
        })
        viewModel.optionsList.observe(this, {optionsList ->
            binding.roulette.setRouletteOptionList(optionsList)
        })
        viewModel.result.observe(this, {result ->
            binding.textViewResult.text = result
        })
        viewModel.rotation.observe(this, {rotation ->
            binding.roulette.setRouletteRotation(rotation)
        })
    }

    //Clean screen when coming back from MyRoulettesActivity with a new Roulette selected
    override fun onRestart() {
        Log.d(TAG, "onRestart: starts")
        super.onRestart()

        if(viewModel.isNewRouletteSelected){
            viewModel.isNewRouletteSelected = false
            binding.shareButton.visibility = View.INVISIBLE
            binding.searchButton.visibility = View.INVISIBLE
        }
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
                if(binding.roulette.isAnimationRunning()){
                    Toast.makeText(this, resources.getString(R.string.UNTIL_SPIN_COMPLETED), Toast.LENGTH_SHORT).show()
                    return false
                }
                rouletteEditRequest(viewModel.roulette)
                true
            }
            R.id.menumain_showRoulettes -> {
                //Launch next activity
                if(binding.roulette.isAnimationRunning()){
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
        //When orientation changes or when the user exit app (without closing it), stop animation. Restart roulette.
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
        Log.d(TAG, "rouletteEditRequest: starts")
        //Create a new fragment to edit the Roulette
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
                viewModel.setResult(resources.getString(R.string.EMPTY_STRING))
                binding.searchButton.visibility = View.INVISIBLE
                binding.shareButton.visibility = View.INVISIBLE
                binding.roulette.spin(7000, 2.7f*(0.9f + Random.nextFloat()))
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
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Decision Maker [${Utils.getDate()}]\n${binding.textViewTitle.text}: ${binding.textViewResult.text} win!")
                    sendIntent.type = "text/plain"
                    startActivity(Intent.createChooser(sendIntent, null))

                }catch (e:Exception){
                    Log.e(TAG, ".onClick: share_button error is ${e.printStackTrace()}")
                }
            }
            R.id.search_button->{
                val gsIntentUri = Uri.parse(String.format(resources.getString(R.string.URL_GOOGLE), URLEncoder.encode(binding.textViewResult.text.toString(), "UTF-8")))
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
        viewModel.setResult(choice.uppercase(Locale.ROOT))
        viewModel.setRotation(binding.roulette.getRouletteRotation())
        binding.searchButton.visibility = View.VISIBLE
        binding.shareButton.visibility = View.VISIBLE
    }

    override fun OnRouletteSpinEvent(speed: Float) {
        Log.d(TAG, ".OnRouletteSpinEvent: speed is $speed")
        if(binding.roulette.getRouletteOptionsCount() > 1 && abs(speed) > 0.09f) {
            viewModel.setResult(resources.getString(R.string.EMPTY_STRING))
            binding.searchButton.visibility = View.INVISIBLE
            binding.shareButton.visibility = View.INVISIBLE
            val t = (min(9000f, 7000f*abs(speed))).toLong()
            binding.roulette.spin(t, 1.1f*speed)
        }
    }

    private var lastOptionChangeTime = System.currentTimeMillis()
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
    override fun onSaveClicked(roulette: Roulette) {
        Log.d(TAG, "onSaveClicked: starts")

        //Set in Shared preferences
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
            Log.d(TAG, "Back pressed with fragment on screen")
            if(viewModel.roulette != viewModel.oldRoulette){
                viewModel.swapRoulette()
            }
            backPressedFragment = true
            removeEditFragment(fragment)
        }
    }

}