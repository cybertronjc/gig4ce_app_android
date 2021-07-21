package com.gigforce.lead_management.ui.joining_list.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningStatus
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.RecyclerRowJoiningItemBinding
import com.gigforce.lead_management.models.JoiningListRecyclerItemData


class JoiningRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowJoiningItemBinding
    private var viewData: JoiningListRecyclerItemData.JoiningListRecyclerJoiningItemData? = null

    lateinit var foregroundView: View
    lateinit var backgroundView: View

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        viewBinding.callGigerBtn.setOnClickListener(this)
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
        backgroundView = viewBinding.viewBackground
    }

    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val gigerAttendanceData =
                it as JoiningListRecyclerItemData.JoiningListRecyclerJoiningItemData
            viewData = gigerAttendanceData

            viewBinding.userNameTv.text = gigerAttendanceData.userName
            viewBinding.callGigerBtn.isVisible =
                gigerAttendanceData.userProfilePhoneNumber.isNotBlank()

            setUserImage(
                gigerAttendanceData.userProfilePictureThumbnail,
                gigerAttendanceData.userProfilePicture
            )
            setOfficeOnView(gigerAttendanceData.joiningStatusText)
            setJoiningStatus(gigerAttendanceData.status)
        }
    }

    private fun setJoiningStatus(
        status: String,
    ) {

        val joiningStatus = JoiningStatus.fromValue(status)
        viewBinding.userAttendanceStatusTextview.isVisible = joiningStatus != JoiningStatus.JOINED
        viewBinding.userAttendanceStatusTextview.text = joiningStatus.getStatusFormattedString()

        when (joiningStatus) {
            JoiningStatus.SIGN_UP_PENDING -> {
                viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.joining_status_bck_orange)
            }
            JoiningStatus.APPLICATION_PENDING -> {
                viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.joining_status_bck_orange)
            }
            JoiningStatus.JOINING_PENDING -> {
                viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.joining_status_bck_green)
            }
            JoiningStatus.JOINED -> {
            }
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

    private fun setUserImage(
        profilePicThumbnail: String?,
        gigerImage: String
    ) {
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

        val intent =
            Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", currentViewData.userProfilePhoneNumber, null)
            )
        context.startActivity(intent)
    }

    fun getGigDataOrThrow(): JoiningListRecyclerItemData.JoiningListRecyclerJoiningItemData {
        return viewData ?: throw NullPointerException("view data is null")
    }
}
