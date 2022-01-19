//package com.gigforce.lead_management.ui.changing_tl
//
//import androidx.lifecycle.ViewModelProvider
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.gigforce.giger_gigs.R
//import com.gigforce.lead_management.R
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class GigerAttendanceDetailFragment : Fragment() {
//
//    companion object {
//        fun newInstance() = GigerAttendanceDetailFragment()
//    }
//
//    private lateinit var viewModel: GigerAttendanceDetailViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.giger_attendance_detail_fragment, container, false)
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(GigerAttendanceDetailViewModel::class.java)
//        // TODO: Use the ViewModel
//    }
//
//}