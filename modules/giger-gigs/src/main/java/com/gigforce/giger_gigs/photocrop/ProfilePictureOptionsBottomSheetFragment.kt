package com.gigforce.giger_gigs.photocrop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.giger_gigs.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ProfilePictureOptionsBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance() =
            ProfilePictureOptionsBottomSheetFragment()
    }

    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.profile_photo_bottom_sheet, container, false)
        return layout
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ProfileOptionBS", "view created")

    }

}