package com.example.decisionmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_options.*

//Tag for LOG
private const val TAG = "OptionsActivity"

class OptionsActivity : AppCompatActivity() {

    companion object{
        const val OPTIONS_ACT_ROULETTE_LIST:String = "ROULETTE_LIST"
        const val OPTIONS_ACT_ROULETTE_UPD_OK:Int = 1
    }

    //List to store the options
    var listOptions = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        //Make scrollable the text view
        textView_Options.movementMethod = ScrollingMovementMethod()

        if(intent.extras != null){
            listOptions = intent.extras!!.getStringArrayList(OPTIONS_ACT_ROULETTE_LIST)!!
            for(choice in listOptions){
                textView_Options.append(choice)
                textView_Options.append("\n")
            }
        }

        //Listener
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_addOption -> {
                    textView_Options.append(editText_addOption.text)
                    textView_Options.append("\n")
                    listOptions.add(editText_addOption.text.toString())
                    editText_addOption.text.clear()
                }
                R.id.floatButton_ready -> {
                    if(editText_addOption.text.isNotEmpty()){
                        listOptions.add(editText_addOption.text.toString())
                    }
                    for (option in listOptions){
                        Log.d(TAG,"Elemento de la lista: $option")
                    }
                    Snackbar.make(view,"Regresar a MainActivity y actualizar la ruleta", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    setResultAndReturnToActivity()
                }
            }
        }

        button_addOption.setOnClickListener(listener)
        floatButton_ready.setOnClickListener(listener)
    }

    /**
     * For better UX, detect back key pressed
     * and save entered values
     */
    override fun onBackPressed() {
        setResultAndReturnToActivity()
        super.onBackPressed()
    }

    /**
     * Set listOptions as the activity result,
     * then finish the activity
     */
    private fun setResultAndReturnToActivity(){
        val result = Intent()
        result.putExtra(OPTIONS_ACT_ROULETTE_LIST, listOptions)
        setResult(OPTIONS_ACT_ROULETTE_UPD_OK, result)
        finish()
    }
}