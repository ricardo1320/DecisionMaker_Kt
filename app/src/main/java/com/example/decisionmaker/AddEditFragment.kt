package com.example.decisionmaker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "AddEditFragment"

// the fragment initialization parameters
private const val ARG_ROULETTE = "roulette"

//Adapter variable
private lateinit var optionsAdapter: OptionsAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment(), View.OnClickListener {
    //Name and type of parameters
    private var roulette: Roulette? = null

    private var listener: OnSaveClicked? = null

    //View Model
    private val viewModel: MyRoulettesViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)

        //Get the roulette from the arguments
        roulette = arguments?.getParcelable(ARG_ROULETTE)
        Log.d(TAG, "onCreate: roulette is $roulette")

        //Let the fragment manipulate the menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: starts")
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    //If editing a roulette -> populate the widgets with the details. If not -> add a new roulette
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var optionList = ArrayList<String>()
        val roulette = roulette

        //Editing a roulette
        if(roulette != null){
            Log.d(TAG, "onViewCreated: Roulette details found, editing roulette -> $roulette")
            editText_RouletteTitle.setText(roulette.name)
            optionList = roulette.options
        }else{
            //Adding a new roulette
            Log.d(TAG, "onViewCreated: No arguments, adding new roulette")
        }

        options_recyclerView.layoutManager = LinearLayoutManager(context)
        optionsAdapter = OptionsAdapter(optionList)
        options_recyclerView.adapter = optionsAdapter

        editText_addOption.setOnKeyListener(object:View.OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    if(editText_addOption.text.isNotBlank()){
                        optionsAdapter.addOption(editText_addOption.text.trim().toString())
                        editText_addOption.text.clear()
                        editText_addOption.requestFocus()
                        return true
                    }
                }
                return false
            }
        })

        button_addOption.setOnClickListener(this)
        button_clearTitle.setOnClickListener(this)
        floatButton_ready.setOnClickListener(this)
    }

    //Clear the menu
    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called")
        menu.clear()
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_addOption -> {
                if(editText_addOption.text.isNotBlank()){
                    optionsAdapter.addOption(editText_addOption.text.trim().toString())
                    editText_addOption.text.clear()
                }
            }
            R.id.button_clearTitle -> {
                editText_RouletteTitle.text.clear()
            }
            R.id.floatButton_ready -> {
                //Go back to Activity and update the Roulette List, if the option list has at least 2 elements
                checkMinOptions()
            }
        }
    }

    //Function to save the Roulette
    private fun saveRoulette(){
        //Create a newTask object with the details to be saved, then call the viewModel's saveTask function to save it.
        val newRoulette = rouletteFromUI()
        Log.d(TAG, "saveRoulette: roulette $roulette, newRoulette $newRoulette")

        //Different behaviour depending the Parent Activity
        if((activity?.javaClass?.simpleName) == MainActivity::class.java.simpleName){
            //Convert the Roulette to JSON string
            val jsonString = GsonBuilder().create().toJson(newRoulette)
            //Save the string in SharedPreferences
            val sharedPref = requireContext().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            sharedPref.edit().putString(GLOBAL_ROULETTE, jsonString).apply()
        }else if((activity?.javaClass?.simpleName) == MyRoulettesActivity::class.java.simpleName){
            if(newRoulette != roulette){
                //If edited, delete the old roulette and add the new one. If adding new roulette, deleteRoulette won't do anything
                //This is just in case the Roulette name changes, the options list changes is managed by OptionsAdapter automatically
                viewModel.deleteRoulette(roulette)
                roulette = viewModel.saveRoulette(newRoulette)
            }
        }
    }

    //Build a Roulette object from the UI widgets
    private fun rouletteFromUI(): Roulette {
        //Get options list
        val optionList = optionsAdapter.getOptions()
        return Roulette(editText_RouletteTitle.text.toString(), optionList)
    }

    //Check options are at least two (2)
    private fun checkMinOptions(){
        if(optionsAdapter.itemCount < 2)
            Toast.makeText(activity, resources.getString(R.string.MIN_OPTIONS_CONSTRAINT), Toast.LENGTH_LONG).show()
        else{
            if(editText_RouletteTitle.text.isBlank())
                editText_RouletteTitle.setText(resources.getString(R.string.ROULETTE_DEFAULT_TITLE))
            //TODO save in database
            saveRoulette()  //Save in the list of the view model
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)
        //Make sure the activity/fragment implements its callbacks
        if(context is OnSaveClicked){
            listener = context
        }else{
            throw RuntimeException("$context must implement OnSavedClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        //Reset the active callbacks interface, because we're no longer attached
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnSaveClicked{
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param roulette The roulette to be edited, or null to add a new roulette.
         * @return A new instance of fragment AddEditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(roulette: Roulette?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROULETTE, roulette)
                }
            }
    }
}