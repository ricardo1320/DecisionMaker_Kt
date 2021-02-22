package com.example.decisionmaker

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.decisionmaker.views.OnRouletteViewListener
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_main.*

//Tag for LOG
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnRouletteViewListener {

    companion object{
        const val ROULETTE_OPTIONS_REQUEST_CODE = 10
    }

    private var rouletteOptions:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roulette.setRouletteOptionList(rouletteOptions)
        roulette.onRouletteViewListener = this

        //Click listener for button_spin (spin the roulette)
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_spin -> { roulette.spin(6000)  }
            }
        }

        button_spin.setOnClickListener(listener)
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
        Toast.makeText(this, choice + " win", Toast.LENGTH_SHORT).show()
    }

}