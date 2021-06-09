package com.example.decisionmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.decisionmaker.databinding.OptionBinding

//Class for the custom adapter
class OptionsAdapter(private var listOptions: ArrayList<String>) : RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder>() {

    //View holder
    inner class OptionsViewHolder(val binding: OptionBinding) : RecyclerView.ViewHolder(binding.root)

    //Inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        //Create a viewBinding, which defines the UI of the list item
        val binding = OptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionsViewHolder(binding)
    }

    //Replace the contents of a view. Populate data into the item through holder
    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        // Get element from the list at this position and replace the contents of the view with that element
        holder.binding.textViewOption.text = listOptions[position]

        //Listener for delete option button. Remove from the list and the recycler view
        holder.binding.buttonDeleteOption.setOnClickListener {
            listOptions.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun getOptions() : ArrayList<String>{
        return listOptions
    }

    fun addOption(choice:String){
        listOptions.add(choice)
        notifyDataSetChanged()
    }

    fun clear(){
        listOptions.clear()
        notifyDataSetChanged()
    }

    //Returns the total count of items in the list
    override fun getItemCount(): Int {
        return listOptions.size
    }

    //Load new data and refresh the recycler view
    fun loadNewData(newList: ArrayList<String>){
        listOptions = newList
        notifyDataSetChanged()
    }

}