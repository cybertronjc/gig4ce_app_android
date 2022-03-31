package com.gigforce.giger_gigs.listItems

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.gigforce.common_ui.CommonIntentExtras
import com.gigforce.common_ui.navigation.LeadManagementConstants
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.RecyclerRowGigerAttendanceBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceGigerAttendanceRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowGigerAttendanceBinding
    private var viewData: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData? = null

    lateinit var foregroundView : View
    lateinit var backgroundView : View

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var navigation : INavigation

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.callGigerBtn.setOnClickListener(this)
        viewBinding.root.setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowGigerAttendanceBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        foregroundView = viewBinding.viewForeground
        backgroundView = viewBinding.viewBackground
    }

    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val gigerAttendanceData =
                it as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            viewData = gigerAttendanceData

            viewBinding.userNameTv.text = gigerAttendanceData.gigerName
            viewBinding.callGigerBtn.isVisible = gigerAttendanceData.gigerPhoneNumber.isNotBlank()

            setUserImage(gigerAttendanceData.gigerImage)
            setOfficeOnView(gigerAttendanceData.gigerOffice)
            setUserAttendanceStatus(gigerAttendanceData.attendanceStatus, gigerAttendanceData.gigStatus)
        }
    }

    private fun setUserAttendanceStatus(
        attendanceStatus: String,
        gigStatus: String
    ) {
        viewBinding.userAttendanceStatusTextview.text = gigStatus
        if ("Present".equals(attendanceStatus, true)) {
            viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.status_present_chip_background)
        } else {
            viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.status_absent_chip_background)
        }
    }

    private fun setOfficeOnView(
        office: String
    ) {
        if (office.isEmpty()) {
            viewBinding.userPhoneNumber.text = "Office : N/A"
        } else {
            viewBinding.userPhoneNumber.text = office
        }
    }

    private fun setUserImage(gigerImage: String) {
        if (gigerImage.isEmpty() || gigerImage == "avatar.jpg") {
            viewBinding.userImageIv.loadImage(R.drawable.ic_user_2)
            return
        }

        val userPathInFirebase = if (gigerImage.startsWith("profile_pics/"))
            gigerImage
        else
            "profile_pics/${gigerImage}"

        viewBinding.userImageIv.loadImageIfUrlElseTryFirebaseStorage(
            userPathInFirebase,
            R.drawable.ic_user_2,
            R.drawable.ic_user_2
        )
    }

    override fun onClick(v: View?) {
        val currentViewData = viewData ?: return

        if(v?.id == R.id.call_giger_btn){

            eventTracker.pushEvent(
                TrackingEventArgs(
                    "tl_call_giger", null
                )
            )
            val intent =
                Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", currentViewData.gigerPhoneNumber, null))
            context.startActivity(intent)
        } else{
          val gigId = currentViewData.gigId
          //navigate to joining details screen
          navigation.navigateTo(
                LeadManagementNavDestinations.FRAGMENT_GIGER_INFO,
                bundleOf(
                    LeadManagementConstants.INTENT_EXTRA_GIG_ID to currentViewData.gigId,
                    CommonIntentExtras.INTENT_USER_ID to viewData?.gigerId
                )
          )
        }
    }

    fun getGigDataOrThrow() : AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData {
        return viewData?: throw NullPointerException("view data is null")
    }
}
