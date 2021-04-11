package com.gigforce.profile.onboarding

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.gigforce.profile.R


/**
 * A simple [Fragment] subclass.
 * Use the [NameGenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NameGenderFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.name_gender_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val unwrappedDrawable: Drawable? =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_search)
        unwrappedDrawable?.let {
            val wrappedDrawable: Drawable = DrawableCompat.wrap(unwrappedDrawable)
            DrawableCompat.setTint(wrappedDrawable, Color.RED)
        }
    }

}