package com.rcmdev.decisionmaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rcmdev.decisionmaker.models.Roulette
import com.rcmdev.decisionmaker.databinding.RouletteListItemsBinding

class RoulettesAdapter(private var listRoulettes: ArrayList<Roulette>?, private val listener: OnRouletteClickListener): RecyclerView.Adapter<RoulettesAdapter.RoulettesViewHolder>() {

    interface OnRouletteClickListener{
        fun onEditClick(roulette: Roulette)
        fun onRouletteClick(roulette: Roulette)
    }

    inner class RoulettesViewHolder(val binding: RouletteListItemsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoulettesViewHolder {
        val binding = RouletteListItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoulettesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoulettesViewHolder, position: Int) {
        if(listRoulettes != null){ holder.binding.rliName.text = listRoulettes!![position].name }

        holder.binding.rliEdit.setOnClickListener { listener.onEditClick(listRoulettes!![position]) }

        holder.itemView.setOnClickListener { listener.onRouletteClick(listRoulettes!![position]) }
    }

    override fun getItemCount(): Int {
        return if(listRoulettes != null){
            listRoulettes!!.size
        }else{
            1
        }
    }

    fun removeAt(position: Int){
        listRoulettes!!.removeAt(position)
        notifyDataSetChanged()
    }

    fun swapList(newListRoulettes: ArrayList<Roulette>){
        listRoulettes = newListRoulettes
        notifyDataSetChanged()
    }

}