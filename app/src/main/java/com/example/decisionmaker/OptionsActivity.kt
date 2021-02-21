package com.example.decisionmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_options.*

//Tag for LOG
private const val TAG = "OptionsActivity"

class OptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        //List to store the options
        var listOptions = ArrayList<String>()

        //Make scrollable the text view
        textView_Options.movementMethod = ScrollingMovementMethod()

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
                    for (option in listOptions){
                        Log.d(TAG,"Elemento de la lista: $option")
                    }
                    Snackbar.make(view,"Regresar a MainActivity y actualizar la ruleta", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
            }
        }

        button_addOption.setOnClickListener(listener)
        floatButton_ready.setOnClickListener(listener)
    }
}