package com.gigforce.app.core.base.viewsfromviews

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.core.base.genericadapter.PFRecyclerViewAdapter

//import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter

interface ViewsFromViewsInterface {
    //from holder
    fun getTextView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): TextView
    fun getEditText(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): EditText
    fun getImageView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): ImageView
    fun getRecyclerView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): RecyclerView
    fun getView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): View

    //from views
    fun getTextView(view: View, id: Int): TextView
    fun getImageView(view: View, id: Int): ImageView
    fun getView(view: View, id: Int): View


    //set color and size
    fun setTextViewColor(textView: TextView, color: Int)
    fun setTextViewSize(textView: TextView, size: Float)
    fun setViewBackgroundColor(view: View, color: Int)
}
