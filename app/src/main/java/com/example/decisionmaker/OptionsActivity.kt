package com.example.decisionmaker

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_options.*


class OptionsActivity : AppCompatActivity() {

    companion object{
        const val OPTIONS_ACT_ROULETTE_LIST:String = "ROULETTE_LIST"
        const val OPTIONS_ACT_ROULETTE_UPD_OK:Int = 1
    }

    //Adapter variable
    private lateinit var myAdapter: OptionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        //Adapter
        myAdapter = OptionsAdapter(intent.extras!!.getStringArrayList(OPTIONS_ACT_ROULETTE_LIST)!!)
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration (this, DividerItemDecoration.VERTICAL))

        //Listener
        val listener = View.OnClickListener { view ->
            when(view.id){
                R.id.button_addOption -> {
                    if(editText_addOption.text.isNotEmpty()){
                        myAdapter.addOption(editText_addOption.text.toString())
                        editText_addOption.text.clear()
                    }
                }
                R.id.floatButton_ready -> {
                    //Go back to MainActivity and update the Roulette, if the list has at least 2 options
                    checkMinOptions()
                }
            }
        }

        editText_addOption.setOnKeyListener(object:View.OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    if(editText_addOption.text.isNotEmpty()){
                        myAdapter.addOption(editText_addOption.text.toString())
                        editText_addOption.text.clear()
                        editText_addOption.requestFocus()
                        return true
                    }
                }
                return false
            }
        })

        button_addOption.setOnClickListener(listener)
        floatButton_ready.setOnClickListener(listener)
    }

    //Menu overridden methods
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_optionsactivity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_clear -> {
                //Clear options -> List and Edit Text
                editText_addOption.text.clear()
                myAdapter.clear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Check options are at least two (2)
    private fun checkMinOptions(){
        if(myAdapter.itemCount < 2)
            Toast.makeText(this, "At least two options!", Toast.LENGTH_LONG).show()
        else
            setResultAndReturnToActivity()
    }

    /**
     * For better UX, detect back key pressed
     * and save entered values
     */
    override fun onBackPressed() {
        checkMinOptions()
        super.onBackPressed()
    }

    /**
     * Set listOptions as the activity result,
     * then finish the activity
     */
    private fun setResultAndReturnToActivity(){
        val result = Intent()
        result.putExtra(OPTIONS_ACT_ROULETTE_LIST, myAdapter.getOptions())
        setResult(OPTIONS_ACT_ROULETTE_UPD_OK, result)
        finish()
    }

}


