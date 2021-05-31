package com.gigforce.app.core.base.viewsfromviews

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter

//import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter

class ViewsFromViewsImpl(var activity: Activity) : ViewsFromViewsInterface {

    //view from holders
    override fun getTextView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): TextView {
        return view.getView(id) as TextView
    }

    override fun getEditText(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): EditText {
        return view.getView(id) as EditText
    }
    override fun getImageView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): ImageView {
        return view.getView(id) as ImageView
    }
    override fun getRecyclerView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): RecyclerView {
        return view.getView(id) as RecyclerView
    }

    override  fun getView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): View {
        return view.getView(id)
    }

    // view from views
    override fun getTextView(view: View, id: Int): TextView {
        return view.findViewById(id) as TextView
    }


    override fun getImageView(view: View, id: Int): ImageView {
        return view.findViewById(id) as ImageView
    }


    override fun getView(view: View, id: Int): View {
        return view.findViewById(id)
    }

    // set colors size
    override fun setTextViewColor(textView: TextView, color: Int) {
        textView.setTextColor(ContextCompat.getColor(activity.applicationContext, color))
    }

    override fun setTextViewSize(textView: TextView, size: Float) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    override fun setViewBackgroundColor(view: View, color: Int) {
        view.setBackgroundColor(ContextCompat.getColor(activity.applicationContext, color))
    }
}