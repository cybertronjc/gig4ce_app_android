package com.gigforce.lead_management.ui.joining_list_2.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobLocation
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningStatus
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.RecyclerRowJoiningItemBinding
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
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
), IViewHolder, View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation
    private lateinit var viewBinding: RecyclerRowJoiningItemBinding
    private var viewData: JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData? = null

    lateinit var foregroundView: View
    lateinit var backgroundView: View
    var selectEnable = false

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        viewBinding.callGigerBtn.setOnClickListener(this)
//        viewBinding.root.setOnLongClickListener(this)
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

            viewBinding.userNameTv.text = gigerAttendanceData.gigerName
            viewBinding.callGigerBtn.isVisible =
                gigerAttendanceData.gigerMobileNo.isNotBlank()

            setUserImage(
                "",
                gigerAttendanceData.profilePicture.toString()
            )
            setSelectionDate(gigerAttendanceData.createdAt, gigerAttendanceData.updatedAt)
            setJoiningStatus(gigerAttendanceData.status)
        }
    }

    private fun setJoiningStatus(
        status: String,
    ) {

        val joiningStatus = JoiningStatus.fromValue(status)
//        viewBinding.userAttendanceStatusTextview.isVisible = joiningStatus != JoiningStatus.JOINED
        viewBinding.userAttendanceStatusTextview.text = "Application " + context.getString(joiningStatus.getStatusFormattedStringRes())
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
            viewBinding.selectedOn.text = "Selected on " + getFormattedDate(createdAt.toString())
        } else {
            viewBinding.selectedOn.text = "Selected on " + getFormattedDate(updatedAt.toString())
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

        if (v?.id == R.id.call_giger_btn) {

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
                    LeadManagementConstants.INTENT_EXTRA_JOINING_ID to currentViewData._id
                )
                )

        }
    }

    fun getFormattedDate(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val output = SimpleDateFormat("dd/MM/yyyy")

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }

}
