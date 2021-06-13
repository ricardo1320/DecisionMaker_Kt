package com.example.decisionmaker.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.decisionmaker.models.Roulette
import com.example.decisionmaker.databinding.RouletteListItemsBinding

private const val TAG = "RoulettesAdapter"

class RoulettesAdapter(private var listRoulettes: ArrayList<Roulette>?, private val listener: OnRouletteClickListener): RecyclerView.Adapter<RoulettesAdapter.RoulettesViewHolder>() {

    //Interface for the click listeners, the activity or fragment must implement these functions
    interface OnRouletteClickListener{
        fun onEditClick(roulette: Roulette)
        fun onRouletteClick(roulette: Roulette)
    }

    //View holder
    inner class RoulettesViewHolder(val binding: RouletteListItemsBinding) : RecyclerView.ViewHolder(binding.root)

    //Inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoulettesViewHolder {
        //Create a viewBinding, which defines the UI of the list item
        val binding = RouletteListItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoulettesViewHolder(binding)
    }

    //Replace the contents of a view. Populate data into the item through holder
    override fun onBindViewHolder(holder: RoulettesViewHolder, position: Int) {
        // Get element from the list at this position and replace the contents of the view with that element
        if(listRoulettes != null){
            holder.binding.rliName.text = listRoulettes!![position].name
        }

        //Listener for edit button
        holder.binding.rliEdit.setOnClickListener {
            Log.d(TAG, "edit button tapped. Roulette Name: ${listRoulettes!![position]}, Position: $position")
            listener.onEditClick(listRoulettes!![position])
        }

        //Listener for onClickRoulette
        holder.itemView.setOnClickListener {
            Log.d(TAG, "onClick: roulette is ${listRoulettes!![position]}")
            listener.onRouletteClick(listRoulettes!![position])
        }
    }

    //Returns the total count of items in the list
    override fun getItemCount(): Int {
        return if(listRoulettes != null){
            listRoulettes!!.size
        }else{
            1
        }
    }

    //Function to remove an item
    fun removeAt(position: Int){
        listRoulettes!!.removeAt(position)
        notifyItemRemoved(position)
    }

    fun swapList(newListRoulettes: ArrayList<Roulette>){
        listRoulettes = newListRoulettes
        notifyDataSetChanged()
    }

}