package com.gigforce.giger_gigs.travelling_info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.core.IValueChangeListener
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.transformIntoDatePicker
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.R
import com.jaeger.library.StatusBarUtil
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.travelling_detail_info_fragment.*
import java.util.*

@AndroidEntryPoint
class TravellingDetailInfoFragment : Fragment() {

    private val travellingDetailInfoViewModel: TravellingDetailInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.travelling_detail_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAll()
        listeners()
        observers()
    }

    private fun listeners() {
        appBar2.backImageButton.setOnClickListener{
            activity?.onBackPressed()
        }
        textView4.setOnClickListener {
            it.transformIntoDatePicker(context = requireContext(), format = "yyyy-MM-dd", valueChangeListener =
                object : IValueChangeListener {
                    override fun valueChangeListener(date: String,dateObj: Date) {
                        //"dd MMM, yyyy"
                        textView2.text = DateHelper.getDateInddMMMYYYY(dateObj)
                        travellingDetailInfoList.clear()
                        recyclerGenericAdapter?.notifyDataSetChanged()
                        travellingDetailInfoViewModel.getAllTravellingInfo(date, date)
                    }
                })
        }
    }

    private fun initializeAll() {
        initializTravellingDetail()
        changeBackButtonDrawable()
        val fromDate = DateHelper.getDateInYYYYMMDD(Date())
        val toDate = DateHelper.getDateInYYYYMMDD(Date())
        textView2.text = DateHelper.getDateInddMMMYYYY(Date())

        travellingDetailInfoViewModel.getAllTravellingInfo(fromDate, toDate)
    }
    fun changeBackButtonDrawable() {
        appBar2.backImageButton.setImageDrawable(resources.getDrawable(com.gigforce.common_ui.R.drawable.ic_chevron))
    }
    private fun observers() {
        travellingDetailInfoViewModel.travellingInfoLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Lce.Loading -> {
                    progress_bar.visible()
                }
                is Lce.Content -> {
                    progress_bar.gone()
                    if(it.content.status == true) {
                        it.content.data?.let { travellingRes->
                            setData(travellingRes)
                        }?:run{
                            progress_bar.gone()
                            travelling_data_cl.gone()
                            no_travelling_data.visible()
                            error_text.text = "No Gigs Assigned !"
                        }
                    }else{
                        progress_bar.gone()
                        travelling_data_cl.gone()
                        no_travelling_data.visible()
                        error_text.text = "No Gigs Assigned !"
                    }
                }
                is Lce.Error -> {
                    progress_bar.gone()
                    travelling_data_cl.gone()
                    no_travelling_data.visible()
                    context?.let { context1 ->
                        if(!it.error.equals("not_found")){
                            ToastHandler.getToastInstance(
                                context1,
                                it.error.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }

    private fun setData(content: TravellingResponseDM) {
        content.details.let {
            if (it.isNotEmpty()) {
                travelling_data_cl.visible()
                no_travelling_data.gone()
                textView5.text = "${content.totalDistance} km"
//                textView2.text = content.date
                travellingDetailInfoList.clear()
                travellingDetailInfoList.addAll(it)
//                travellingDetailInfoList.add(TravellingDetailInfoModel())
//                travellingDetailInfoList.add(TravellingDetailInfoModel())
                recyclerGenericAdapter?.notifyDataSetChanged()
            }else{
                travelling_data_cl.gone()
                no_travelling_data.visible()
                error_text.text = "Not check-in done yet!"
            }
        }
    }

    var travellingDetailInfoList =
        ArrayList<TravellingDetailInfoModel>()
    var recyclerGenericAdapter: RecyclerGenericAdapter<TravellingDetailInfoModel>? = null
    private fun initializTravellingDetail() {
        recyclerGenericAdapter =
            RecyclerGenericAdapter<TravellingDetailInfoModel>(
                activity?.applicationContext,
                { view, position, item -> },
                { obj, viewHolder, position ->

                    val heading = viewHolder.getView(R.id.textView7) as TextView
                    val countCheckin =
                        getFormatCheckinText(travellingDetailInfoList.size - position)
                    heading.text = "${countCheckin} Toll Details"
                    val checkin_time = viewHolder.getView(R.id.textView9) as TextView
                    val checkout_time = viewHolder.getView(R.id.textView10) as TextView
                    val checkin_address = viewHolder.getView(R.id.checkin_address) as TextView
                    val checkout_address = viewHolder.getView(R.id.checkout_address) as TextView
                    obj.checkin_time?.let {
                        checkin_time.text = DateHelper.getTimeFromString(it) ?: ""
                    }
                    obj.checkout_time?.let {
                        checkout_time.text = DateHelper.getTimeFromString(it) ?: ""
                    }
                    checkin_address.text = obj.checkin_location
                    checkout_address.text = obj.checkout_location
                    if(obj.checkin_latitude.isNullOrBlank() || obj.checkin_longitude.isNullOrBlank()){
                        viewHolder.getView(R.id.checkin_latlong).gone()

                    }else{
                        viewHolder.getView(R.id.checkin_latlong).visible()
                        viewHolder.getView(R.id.checkin_latlong).setOnClickListener{
                            openGoogleMap(obj.checkin_latitude,obj.checkin_longitude, "Checkin location")
                        }
                    }

                    if(obj.checkout_latitude.isNullOrBlank() || obj.checkout_longitude.isNullOrBlank()){
                        viewHolder.getView(R.id.checkout_latlong).gone()
                    }else{
                        viewHolder.getView(R.id.checkout_latlong).visible()
                        viewHolder.getView(R.id.checkout_latlong).setOnClickListener{
                            openGoogleMap(obj.checkout_latitude,obj.checkout_longitude, "Checkout location")
                        }
                    }
                })
        recyclerGenericAdapter?.list = travellingDetailInfoList
        recyclerGenericAdapter?.setLayout(R.layout.travelling_detail_info_card_item)
        travelling_detail_item_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        travelling_detail_item_rv.adapter = recyclerGenericAdapter
    }

    private fun getFormatCheckinText(i: Int): String {
        when (i) {
            1 -> return "1st"
            2 -> return "2nd"
            3 -> return "3rd"
            else -> return "${i}th"
        }
    }

    fun openGoogleMap(latitude:String,longitude:String,label:String){
        val gmmIntentUri = Uri.parse("geo:${latitude},${longitude}?q=${latitude},${longitude}($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}