package com.gigforce.app.modules.photoCrop.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.AddSkillBottomSheetFragment
import com.gigforce.app.modules.profile.models.Education
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.profile_photo_bottom_sheet.view.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class ProfilePictureOptionsBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance() =
            ProfilePictureOptionsBottomSheetFragment()
    }

    lateinit var layout: View
    private lateinit var mListener:BottomSheetListener

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
        Log.d("ProfileOptionBS","view created")

        layout.selectProfilePicture.setOnClickListener {
            Log.d("ProfileOptionBS","select Profiel clicked")
            mListener.onButtonClicked(R.id.selectProfilePicture)
            dismiss()
//            this.findNavController().navigate(R.id.photoCrop)
        }

        layout.removeProfilePicture.setOnClickListener {
            Log.d("ProfileOptionBS","remove Profiel clicked")
            mListener.onButtonClicked(R.id.removeProfilePicture)
            dismiss()
//            this.findNavController().navigate(R.id.photoCrop)
        }

    }

    public interface BottomSheetListener{
        fun onButtonClicked(id:Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener=context as BottomSheetListener
        } catch (e:Exception){
            throw ClassCastException(context.toString()+" must implement Bottom Sheet Listener")
        }
    }
}