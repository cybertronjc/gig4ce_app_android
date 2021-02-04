package com.gigforce.user_profile.components

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.ListAdapter
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.gigforce.user_profile.R
import com.tokenautocomplete.TokenCompleteTextView

open class ChipCollectionView(context: Context?, attrs: AttributeSet?) :
    TokenCompleteTextView<String?>(context, attrs) {
    @DrawableRes
    var drawableEnd: Int? = null
    override fun getViewForObject(person: String?): View {
        val l = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = l.inflate(R.layout.token_view, parent as ViewGroup, false) as TextView
        view.text = person
        return view
    }

    override fun defaultObject(completionText: String): String? {
        return completionText
    }


    fun <T> setAdapter_(adapter: T) where T : ListAdapter, T : Filterable? {
        super.setAdapter(adapter)
    }

    override fun shouldIgnoreToken(token: String?): Boolean {
        return objects.contains(token)
    }


}