package com.gigforce.user_preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PreferredLocationAdapter : RecyclerView.Adapter<PreferredLocationAdapter.PreferencesViewHolder>() {

    companion object{
        const val LOCATION = 1
    }

    inner class PreferencesViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val title = itemView.findViewById<TextView>(R.id.title)


        fun bindView(data : String, position: Int){
            title.text = data
        }
    }

    var data = ArrayList<String>()
    var clickListener : ItemClickListener?=null

    fun setItemClickListener(itemClickListener : ItemClickListener){
        this.clickListener = itemClickListener
    }

    interface ItemClickListener{
        fun onItemClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferencesViewHolder {
        return PreferencesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.preferred_location_item,null))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PreferencesViewHolder, position: Int) {
        holder.bindView(data.get(position),position)
    }
}