package com.example.decisionmaker

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_main.*

//Tag for LOG
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    companion object{
        const val ROULETTE_OPTIONS_REQUEST_CODE = 10
    }

    //Initialize variables for the values of animation (spinning the roulette)
    var startPoint: Float = 0.0f
    var endPoint: Float = Random.nextFloat()*720.0f + 720.0f

    var rouletteOptions:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Click listener for button_spin (spin the roulette)
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_spin -> {
                    spinRoulette(startPoint, endPoint)
                    startPoint = endPoint % 360 //Reset angle to be in range 0..360 degrees, for avoiding overflow or precision error due to large values
                    endPoint = startPoint + Random.nextFloat()*720.0f + 720.0f
                }
            }
        }

        button_spin.setOnClickListener(listener)
    }

    //Function for animating the roulette
    private fun spinRoulette(start: Float, end: Float){
        val animator: ObjectAnimator = ObjectAnimator.ofFloat(roulette, "rotation",start, end)
        animator.setDuration(2000)
        //Log the values: start and end point of the animation
        Log.d("MainActivity", "values: ${animator.values[0]}")
        animator.start()
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

}