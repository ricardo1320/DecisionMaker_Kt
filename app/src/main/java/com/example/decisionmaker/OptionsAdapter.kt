package com.example.decisionmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Class for the custom adapter
class OptionsAdapter(private var listOptions: ArrayList<String>) : RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder>() {

    //Provide a reference to the type of views that you are using
    class OptionsViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textViewOption: TextView = view.findViewById(R.id.textView_option)
        val buttonDeleteOption: ImageButton = view.findViewById(R.id.button_delete_option)
    }

    //Inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        //Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.option, parent, false)
        return OptionsViewHolder(view)
    }

    //Replace the contents of a view. Populate data into the item through holder
    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        // Get element from the list at this position and replace the contents of the view with that element
        holder.textViewOption.text = listOptions[position]

        //Listener for delete option button. Remove from the list and the recycler view
        holder.buttonDeleteOption.setOnClickListener {
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