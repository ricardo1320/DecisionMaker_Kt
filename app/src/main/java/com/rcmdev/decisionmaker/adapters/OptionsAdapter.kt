package com.rcmdev.decisionmaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rcmdev.decisionmaker.databinding.OptionBinding

class OptionsAdapter(private var listOptions: ArrayList<String>) : RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder>() {

    inner class OptionsViewHolder(val binding: OptionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        val binding = OptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        holder.binding.textViewOption.text = listOptions[position]
        holder.binding.buttonDeleteOption.setOnClickListener {
            listOptions.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun getOptions() : ArrayList<String>{ return listOptions }

    fun addOption(choice:String){
        listOptions.add(0, choice)
        notifyDataSetChanged()
    }

    fun clear(){
        listOptions.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int { return listOptions.size }

}