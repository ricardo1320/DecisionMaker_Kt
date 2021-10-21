package com.rcmdev.decisionmaker

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rcmdev.decisionmaker.adapters.RoulettesAdapter
import com.rcmdev.decisionmaker.databinding.ActivityMyRoulettesBinding
import com.rcmdev.decisionmaker.models.Roulette
import com.rcmdev.decisionmaker.viewmodels.MyRoulettesViewModel
import com.rcmdev.decisionmaker.views.SwipeToDeleteCallback

private const val MAX_ROULETTES = 10

class MyRoulettesActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked, RoulettesAdapter.OnRouletteClickListener {

    private lateinit var binding: ActivityMyRoulettesBinding
    private val roulettesAdapter by lazy { RoulettesAdapter(null, this) }
    private val viewModel: MyRoulettesViewModel by viewModels()
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyRoulettesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if( (supportFragmentManager.findFragmentById(R.id.fragment_container_view)) != null ){
            showEditFragment()
        }else{
            removeEditFragment(null)
        }

        viewModel.rouletteList.observe(this, {rouletteList -> roulettesAdapter.swapList(rouletteList)})
        binding.rouletteList.layoutManager = LinearLayoutManager(this)
        binding.rouletteList.adapter = roulettesAdapter

        val swipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                roulettesAdapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rouletteList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_myroulettesact, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuRoulettes_add -> {
                if(roulettesAdapter.itemCount < MAX_ROULETTES){
                    rouletteEditRequest(null)
                    viewModel.editRoulette = null
                }else{
                    if(this::toast.isInitialized) toast.cancel()
                    toast = Toast.makeText(this, resources.getString(R.string.MAX_ROULETTES_CONSTRAINT), Toast.LENGTH_LONG)
                    toast.show()
                }
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
        val newFragment = AddEditFragment.newInstance(roulette)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container_view, newFragment).commit()
        showEditFragment()
    }

    override fun onSaveClicked(roulette: Roulette){
        viewModel.saveRoulette(roulette)
        removeEditFragment(supportFragmentManager.findFragmentById(R.id.fragment_container_view))
    }

    override fun onEditClick(roulette: Roulette) {
        rouletteEditRequest(roulette)
        viewModel.editRoulette = roulette
    }

    override fun onRouletteClick(roulette: Roulette) {
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