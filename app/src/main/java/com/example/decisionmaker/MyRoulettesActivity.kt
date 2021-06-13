package com.example.decisionmaker

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.decisionmaker.adapters.RoulettesAdapter
import com.example.decisionmaker.databinding.ActivityMyRoulettesBinding
import com.example.decisionmaker.models.Roulette
import com.example.decisionmaker.viewmodels.MyRoulettesViewModel

private const val TAG = "MyRoulettesActivity"

class MyRoulettesActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked, RoulettesAdapter.OnRouletteClickListener {

    //Variable for view binding
    private lateinit var binding: ActivityMyRoulettesBinding

    //Variable to hold the adapter
    private val roulettesAdapter by lazy { RoulettesAdapter(null, this) }

    //View Model
    private val viewModel: MyRoulettesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyRoulettesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set the view correctly
        if( (supportFragmentManager.findFragmentById(R.id.fragment_container_view)) != null ){
            showEditFragment()
        }else{
            removeEditFragment(null)
        }

        //Subscribe to the View Model and observe LiveData
        viewModel.rouletteList.observe(this, {rouletteList -> roulettesAdapter.swapList(rouletteList)})

        //Initialise the adapter and associated it with the RecyclerView
        binding.rouletteList.layoutManager = LinearLayoutManager(this)
        binding.rouletteList.adapter = roulettesAdapter

        //Implementing deleting by swapping
        //Create an IteTouchHelper object and attach it to the recycler view
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    //Implement this to allow sorting tasks by dragging them up and down in the list
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    Log.d(TAG, "onSwiped: starts")
                    if (direction == ItemTouchHelper.LEFT) {
                        roulettesAdapter.removeAt(viewHolder.adapterPosition)
                    }
                }
            }
        )
        itemTouchHelper.attachToRecyclerView(binding.rouletteList)

    }

    //Menu overridden methods
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_myroulettesact, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuRoulettes_add -> {
                Log.d(TAG, "onOptionsItemSelected: Add New Roulette")
                rouletteEditRequest(null)
                viewModel.editRoulette = null
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showEditFragment(){
        binding.fragmentContainerView.visibility = View.VISIBLE
        binding.rouletteList.visibility = View.GONE
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun removeEditFragment(fragment: Fragment? = null){
        if(fragment != null){
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        binding.fragmentContainerView.visibility = View.GONE
        binding.rouletteList.visibility = View.VISIBLE
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun rouletteEditRequest(roulette: Roulette?){
        Log.d(TAG, "rouletteEditRequest: starts")

        //Create a new fragment to edit the Roulette
        val newFragment = AddEditFragment.newInstance(roulette)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container_view, newFragment).commit()

        showEditFragment()
        Log.d(TAG, "Exiting rouletteEditRequest")
    }

    //Callback function to remove AddEditFragment, from this Activity, when a Roulette is saved
    override fun onSaveClicked(roulette: Roulette){
        viewModel.saveRoulette(roulette)
        removeEditFragment(supportFragmentManager.findFragmentById(R.id.fragment_container_view))
    }

    //Callback function to edit a Roulette
    override fun onEditClick(roulette: Roulette) {
        rouletteEditRequest(roulette)
        viewModel.editRoulette = roulette
    }

    //Callback function to select a Roulette
    override fun onRouletteClick(roulette: Roulette) {
        //Save in SharedPreferences via ViewModel and finish activity
        Log.d(TAG, "onRouletteClick: roulette is $roulette")
        viewModel.saveMainRoulette(roulette)
        finish()
    }

    /**
     * Detect back key pressed, if fragment on screen then remove it.
     */
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        if(fragment == null){
            super.onBackPressed()
        }else{
            removeEditFragment(fragment)
        }
    }

}