package com.gigforce.profile.onboarding.fragments.preferredJobLocation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.gigforce.profile.R
import com.gigforce.profile.models.CityWithImage
import java.util.*

class PreferredLocationMajorCitiesAdapter(
        context: Context,
        textViewResourceId: Int,
        private val cities: List<CityWithImage>,
        private val requestManager: RequestManager
) : ArrayAdapter<CityWithImage>(context, textViewResourceId, cities) {


    override fun getCount(): Int {
        return cities.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        v = inflater.inflate(R.layout.recycler_item_major_city, null)

        var cityNameTv: TextView = v.findViewById(R.id.city_name_tv)
        var cityImageIV: ImageView = v.findViewById(R.id.city_image_iv)


        val city = cities[position]
        requestManager.load(city.image).into(cityImageIV)
        cityNameTv.text = city.name
        return v
    }
}