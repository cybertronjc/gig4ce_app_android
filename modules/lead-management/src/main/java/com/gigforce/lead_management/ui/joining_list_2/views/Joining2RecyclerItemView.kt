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
        viewBinding.root.setOnLongClickListener(this)
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
//            setOfficeOnView(gigerAttendanceData.joiningStatusText)
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

        //viewBinding.userAttendanceStatusTextview.text = status
//        when (joiningStatus) {
//            JoiningStatus.SIGN_UP_PENDING -> {
//                viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.joining_status_bck_orange)
//            }
//            JoiningStatus.APPLICATION_PENDING -> {
//                viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.joining_status_bck_orange)
//            }
//            JoiningStatus.JOINING_PENDING -> {
//                viewBinding.userAttendanceStatusTextview.setBackgroundResource(R.drawable.joining_status_bck_green)
//            }
//            JoiningStatus.JOINED -> {
//            }
//        }
    }

    private fun setOfficeOnView(
        office: String
    ) {
        if (office.isEmpty()) {
            viewBinding.userPhoneNumber.text = context.getString(R.string.office_na_lead)
        } else {
            viewBinding.userPhoneNumber.text = office
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
                R.drawable.ic_user_2,
                R.drawable.ic_user_2
            )

        } else if(!gigerImage.isBlank()) {

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
        } else {
            viewBinding.userImageIv.loadImage(R.drawable.ic_user_2)

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
//            if (currentViewData.status.isEmpty())
//                return
//
//            if (selectEnable){
//                viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_selected_tick))
//                viewData?.selected = true
//            }
            //navigate to joining details screen
            navigation.navigateTo(
                LeadManagementNavDestinations.FRAGMENT_GIGER_INFO,
                bundleOf(
                    LeadManagementConstants.INTENT_EXTRA_JOINING_ID to currentViewData._id
                )
                )
//            when (JoiningStatus.fromValue(currentViewData.status)) {
//                JoiningStatus.SIGN_UP_PENDING, JoiningStatus.JOINED -> {
//                }
//                JoiningStatus.APPLICATION_PENDING -> {
//                    //navigate to applications screen
//                    navigation.navigateTo(
//                        LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_TO_ACTIVATE,
//                        bundleOf(
//                            LeadManagementConstants.INTENT_EXTRA_JOINING_ID to currentViewData.joiningId,
//                            LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO to prepareCurrentUserInfo(
//                                currentViewData
//                            ),
//                            LeadManagementConstants.INTENT_EXTRA_USER_ID to currentViewData.userUid,
//                        )
//                    )
//                }
//                JoiningStatus.JOINING_PENDING -> {
//                    if (currentViewData._id.isEmpty()) {
//                        return
//                    }
//
//                    navigation.navigateTo(
//                        LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_LOCATION,
//                        bundleOf(
//                            LeadManagementConstants.INTENT_EXTRA_JOINING_ID to currentViewData._id,
//                            LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO to prepareCurrentUserInfo(
//                                currentViewData
//                            ),
//                            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to prepareAssigngigModel(
//                                currentViewData
//                            ),
//                            LeadManagementConstants.INTENT_EXTRA_USER_ID to currentViewData.gigerId
//                        )
//                    )
//                }
//            }
        }
    }

//    private fun prepareAssigngigModel(
//        currentViewData: JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData
//    ): AssignGigRequest {
//
//        return AssignGigRequest(
//            joiningId = currentViewData.joiningId,
//            jobProfileId = currentViewData.jobProfileId,
//            jobProfileName = currentViewData.jobProfileName,
//            userName = currentViewData.userName,
//            userUid = currentViewData.userUid!!,
//            enrollingTlUid = "",
//            assignGigsFrom = "",
//            cityId = "",
//            cityName = "",
//            location = JobLocation(
//                id = "",
//                type = "",
//                name = null
//            ),
//            shift = listOf(),
//            gigForceTeamLeaders = listOf(),
//            businessTeamLeaders = listOf()
//        )
//    }

//    private fun prepareCurrentUserInfo(
//        currentViewData: JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData
//    ): GigerProfileCardDVM {
//        return GigerProfileCardDVM(
//            name = currentViewData.userName,
//            gigerImg = currentViewData.userProfilePicture,
//            number = currentViewData.userProfilePhoneNumber,
//            jobProfileName = currentViewData.jobProfileName,
//            jobProfileLogo = currentViewData.jobProfileIcon,
//            tradeName = currentViewData.tradeName
//        )
//    }

    fun getDBImageUrl(imagePath: String): String? {
        if (imagePath.isNotBlank()) {
            try {
                var modifiedString = imagePath
                if (!imagePath.startsWith("/"))
                    modifiedString = "/$imagePath"
                return "gs://gigforce-staging.appspot.com" + modifiedString
            } catch (egetDBImageUrl: Exception) {
                return null
            }
        }
        return null
    }

    override fun onLongClick(p0: View?): Boolean {
        viewBinding.selectJoiningBtn.visible()
        viewData?.selected = true
        viewBinding.selectJoiningBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_selected_tick))
        Toast.makeText(context, "Long click", Toast.LENGTH_SHORT).show()

        return true
    }
//
//    fun getGigDataOrThrow(): JoiningListRecyclerItemData.JoiningListRecyclerJoiningItemData {
//        return viewData ?: throw NullPointerException("view data is null")
//    }
}
