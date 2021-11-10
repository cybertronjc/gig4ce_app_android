package com.gigforce.lead_management.ui.joining_list_2.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningStatus
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.analytics.LeadManagementAnalyticsEvents
import com.gigforce.lead_management.databinding.RecyclerRowJoiningItemBinding
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class Joining2RecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener, View.OnLongClickListener {

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    private lateinit var viewBinding: RecyclerRowJoiningItemBinding
    private var viewData: JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData? = null

    lateinit var foregroundView: View
    lateinit var backgroundView: View
    var selectEnableComponent: Boolean = false

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        viewBinding.callGigerBtn.setOnClickListener(this)
      //  viewBinding.root.setOnLongClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowJoiningItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        foregroundView = viewBinding.viewForeground
//        backgroundView = viewBinding.viewBackground
    }

    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val gigerAttendanceData =
                it as JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData
            viewData = gigerAttendanceData
            if (gigerAttendanceData.isVisible){
                viewBinding.root.visible()
            }else{
                viewBinding.root.gone()
            }

            viewBinding.userNameTv.text = gigerAttendanceData.gigerName.capitalize()
            viewBinding.callGigerBtn.isVisible =
                gigerAttendanceData.gigerMobileNo.isNotBlank()

            setUserImage(
                "",
                gigerAttendanceData.profilePicture.toString()
            )
            setSelectionDate(gigerAttendanceData.createdAt, gigerAttendanceData.updatedAt)
            setJoiningStatus(gigerAttendanceData.status)
            setSelectEnableCard(gigerAttendanceData.selectEnable, gigerAttendanceData.selected)
        }
    }

     fun setSelectEnableCard(selectEnable: Boolean, isSelected: Boolean) {

        if (selectEnable){
            if (isSelected){
                Log.d("selectEnable", "$selectEnable , $isSelected")
                viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_selected_tick))
//                this.selectEnable = true
                viewData?.selected = true
                viewBinding.selectJoiningBtn.visible()
                viewBinding.callGigerBtn.gone()
            }else {
                Log.d("selectEnableNot", "$selectEnable , $isSelected")
                viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_unselect_tick))
//                this.selectEnable = false
                viewData?.selected = false
                viewBinding.selectJoiningBtn.visible()
                viewBinding.callGigerBtn.gone()
            }

        } else {
            viewData?.selected = false
            viewBinding.selectJoiningBtn.gone()
            viewBinding.callGigerBtn.visible()
        }
    }

    private fun setJoiningStatus(
        status: String,
    ) {

        val joiningStatus = JoiningStatus.fromValue(status)
//        viewBinding.userAttendanceStatusTextview.isVisible = joiningStatus != JoiningStatus.JOINED
        viewBinding.userAttendanceStatusTextview.text = resources.getString(R.string.application_lead, resources.getString(joiningStatus.getStatusFormattedStringRes()))
        viewBinding.statusDot.setImageDrawable(
            if (status == "Pending") resources.getDrawable(R.drawable.ic_status_dot) else resources.getDrawable(R.drawable.ic_blue_dot)
        )
        viewBinding.userAttendanceStatusTextview.setTextColor(
            if (status == "Pending") resources.getColor(R.color.pink_dot) else resources.getColor(R.color.blue_dot)
        )

    }

    private fun setSelectionDate(
        createdAt: String?,
        updatedAt: String?
    ) {
        if (updatedAt.isNullOrBlank()) {
            viewBinding.selectedOn.text = "Selected " + formatTimeAgo(createdAt.toString())
        } else {
            viewBinding.selectedOn.text = "Selected " + formatTimeAgo(updatedAt.toString())
        }
    }

    private fun setUserImage(
        profilePicThumbnail: String?,
        gigerImage: String
    ) {

        if(!profilePicThumbnail.isNullOrBlank()){
            val userPathInFirebase = if (profilePicThumbnail.startsWith("profile_pics/"))
                profilePicThumbnail
            else
                "profile_pics/${profilePicThumbnail}"

            viewBinding.userImageIv.loadImageIfUrlElseTryFirebaseStorage(
                userPathInFirebase,
                R.drawable.ic_avatar_male,
                R.drawable.ic_avatar_male
            )

        } else if(!gigerImage.isBlank()) {

            if (gigerImage.isEmpty() || gigerImage == "avatar.jpg") {
                viewBinding.userImageIv.loadImage(R.drawable.ic_avatar_male)
                return
            }

            val userPathInFirebase = if (gigerImage.startsWith("profile_pics/"))
                gigerImage
            else
                "profile_pics/${gigerImage}"

            viewBinding.userImageIv.loadImageIfUrlElseTryFirebaseStorage(
                userPathInFirebase,
                R.drawable.ic_avatar_male,
                R.drawable.ic_avatar_male
            )
        } else {
            viewBinding.userImageIv.loadImage(R.drawable.ic_avatar_male)

        }
    }

    override fun onClick(v: View?) {
        val currentViewData = viewData ?: return
        Log.d("drop", "data ${currentViewData.selected}")
        if (currentViewData.selectEnable){
            if (viewData?.selected == true){
                viewData?.selected = false
                viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_unselect_tick))
                viewData?.viewModel?.dropSelection(viewData?._id.toString(), false)
            }else {
                viewData?.selected = true
                viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_selected_tick))
                viewData?.viewModel?.dropSelection(viewData?._id.toString(), true)
            }
        }else {
            if (v?.id == R.id.call_giger_btn) {

                eventTracker.pushEvent(
                    TrackingEventArgs(
                        LeadManagementAnalyticsEvents.Onboarded_Giger_Calling,
                        mapOf(
                            "Phone_number_called" to currentViewData.gigerMobileNo
                        )
                    )
                )


                val intent =
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.fromParts("tel", currentViewData.gigerMobileNo, null)
                    )
                context.startActivity(intent)
            } else {

                //navigate to joining details screen
                navigation.navigateTo(
                    LeadManagementNavDestinations.FRAGMENT_GIGER_INFO,
                    bundleOf(
                        LeadManagementConstants.INTENT_EXTRA_JOINING_ID to currentViewData._id,
                        LeadManagementConstants.INTENT_EXTRA_IS_ACTIVE to viewData?.isActive
                    )
                )
            }
        }
    }

    fun getFormattedDate(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val output = SimpleDateFormat("dd/MMM/yyyy")

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }

    fun formatTimeAgo(date1: String): String {  // Note : date1 must be in   "yyyy-MM-dd hh:mm:ss"   format
        var conversionTime =""
        try{
            val format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

            val sdf = SimpleDateFormat(format)
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));

            val datetime= Calendar.getInstance()
            var date2= sdf.format(datetime.time).toString()


            val dateObj1 = sdf.parse(date1)
            val dateObj2 = sdf.parse(date2)
            val diff = dateObj2.time - dateObj1.time

            val diffDays = diff / (24 * 60 * 60 * 1000)
            val diffhours = diff / (60 * 60 * 1000)
            val diffmin = diff / (60 * 1000)
            val diffsec = diff  / 1000
            if(diffDays in 1..7){
                conversionTime+=diffDays.toString()+context.getString(R.string.days_ago)
            }else if (diffDays > 7){
                conversionTime+= "on "+getFormattedDate(date1)
            } else if(diffhours>1){
                conversionTime+=(diffhours-diffDays*24).toString()+context.getString(R.string.hours_ago)
            }else if(diffmin>1){
                conversionTime+=(diffmin-diffhours*60).toString()+context.getString(R.string.mins_ago)
            }else if(diffsec>1){
                conversionTime+=(diffsec-diffmin*60).toString()+context.getString(R.string.sec_ago)
            }else {
                conversionTime+=context.getString(R.string.moments_ago)
            }
        }catch (ex:java.lang.Exception){
            Log.d("formatTimeAgo",ex.toString())
        }

        return conversionTime
    }

    override fun onLongClick(p0: View?): Boolean {
        if (viewData?.selected == true){
            viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_unselect_tick))
            return false
        }else {
            viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_selected_tick))
            viewData?.selected = true
            viewBinding.selectJoiningBtn.visible()
            viewBinding.callGigerBtn.gone()
            viewData?.viewModel?.dropSelection(viewData?._id.toString(), true)
        }
        return true
    }

}
