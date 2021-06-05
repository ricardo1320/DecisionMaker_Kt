package com.example.decisionmaker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "RoulettesAdapter"

class RoulettesAdapter(private var listRoulettes: ArrayList<Roulette>?, private val listener: OnRouletteClickListener): RecyclerView.Adapter<RoulettesAdapter.RoulettesViewHolder>() {

    //Interface for the click listeners, the activity or fragment must implement these functions
    interface OnRouletteClickListener{
        fun onEditClick(roulette: Roulette)
        fun onRouletteClick(roulette: Roulette)
    }

    //Provide a reference to the type of views that you are using
    class RoulettesViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val rouletteName: TextView = view.findViewById(R.id.rli_name)
        val buttonEditRoulette: ImageButton = view.findViewById(R.id.rli_edit)
    }

    //Inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoulettesViewHolder {
        //Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roulette_list_items, parent, false)
        return RoulettesViewHolder(view)
    }

    //Replace the contents of a view. Populate data into the item through holder
    override fun onBindViewHolder(holder: RoulettesViewHolder, position: Int) {
        // Get element from the list at this position and replace the contents of the view with that element
        if(listRoulettes != null){
            holder.rouletteName.text = listRoulettes!![position].name
        }

        //Listener for edit button
        holder.buttonEditRoulette.setOnClickListener {
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