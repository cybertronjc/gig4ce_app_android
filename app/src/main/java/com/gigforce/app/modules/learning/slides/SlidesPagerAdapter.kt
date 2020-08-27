package com.gigforce.app.modules.learning.slides

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.gigforce.app.modules.learning.slides.types.SingleImageFragment

class SlidesPagerAdapter constructor(
        fm: FragmentManager,
        private val imageUriList : List<Int>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return SingleImageFragment.getInstance(imageUriList[position])
    }

    override fun getCount(): Int = imageUriList.size
}