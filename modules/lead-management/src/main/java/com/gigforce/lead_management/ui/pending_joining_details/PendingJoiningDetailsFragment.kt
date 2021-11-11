package com.gigforce.lead_management.ui.pending_joining_details

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigerInfo
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentPendingJoiningDetailsBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.lead_management.ui.giger_info.GigerInfoState
import com.gigforce.lead_management.ui.giger_info.GigerInfoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PendingJoiningDetailsFragment : BaseFragment2<FragmentPendingJoiningDetailsBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_pending_joining_details,
    statusBarColor = R.color.lipstick_2
) {

    companion object{
        const val TAG = "PendingJoiningDetailsFragment"
        const val INTENT_EXTRA_JOINING_ID = "joining_id"
    }

    @Inject
    lateinit var navigation : INavigation

    private var gigerPhone = ""
    private lateinit var joiningId : String
    private val viewModel : GigerInfoViewModel by viewModels()

    override fun viewCreated(
        viewBinding: FragmentPendingJoiningDetailsBinding,
        savedInstanceState: Bundle?
    ) {

        getDataFrom(
            arguments,
            savedInstanceState
        )
        initViewModel()
        getJoiningDetails()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            joiningId = it.getString(INTENT_EXTRA_JOINING_ID) ?: return@let
        }

        savedInstanceState?.let {
            joiningId = it.getString(INTENT_EXTRA_JOINING_ID) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_JOINING_ID, joiningId)
    }

    private fun getJoiningDetails() {
        viewModel.getGigerJoiningInfo(joiningId)
    }

    private fun initViewModel() {

        viewModel
            .viewState
            .observe(viewLifecycleOwner,{

                when (it) {
                    is GigerInfoState.ErrorLoadingData -> showJoiningDetailsError(it.error)
                    is GigerInfoState.GigerInfoLoaded -> showJoiningDetails(it.gigerInfo)
                    GigerInfoState.LoadingDataFromServer -> showDetailsLoading()
                }
            })
    }

    private fun showJoiningDetailsError(
        error: String
    ) = viewBinding.apply{
        checklistLayout.removeAllViews()
        stopShimmer(
            gigerinfoShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigerinfoShimmerContainer.gone()
        mainScrollView.visible()


        MaterialAlertDialogBuilder(
            requireContext()
        ).setTitle("Unable to load info")
            .setMessage(error)
            .setPositiveButton("Okay"){_,_ ->}
            .show()
    }

    private fun showJoiningDetails(
        gigerInfo: GigerInfo
    ) = viewBinding.apply {

        stopShimmer(
            gigerinfoShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigerinfoShimmerContainer.gone()
        mainScrollView.visible()


       val totalStepsInCheckList =  gigerInfo.checkList.size
       val totalCompletedStepsInCheckList = gigerInfo.checkList.filter { it.status != "Pending" }
       checklistText.text = "${getString(R.string.application_checklist_lead)} ($totalCompletedStepsInCheckList/$totalStepsInCheckList)}"

        gigerInfo.let {
            overlayCardLayout.jobProfileTextview.text = ": "+it.jobProfileTitle ?: ""
            overlayCardLayout.businessNameTV.text = it.businessName
            val reportingLocText = if (!it.reportingLocation.isNullOrBlank() && it.reportingLocation != "null") it.reportingLocation + ", " else ""
            val businessLocText = if (!it.businessLocation.isNullOrBlank() && it.businessLocation != "null") it.businessLocation else ""
            overlayCardLayout.locationText.text = ": "+reportingLocText + businessLocText ?: ""

            gigerPhone = it.gigerPhone.toString()

            context?.let { it1 ->
                GlideApp.with(it1)
                    .load(it.businessLogo)
                    .placeholder(getCircularProgressDrawable(it1))
                    .into(overlayCardLayout.jobProfileImage)
            }
            overlayCardLayout.selectionDate.text = ": "+getFormattedDate(it.selectionDate)

            val checkListItemData = arrayListOf<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>()
            if (it.checkList == null){
                checklistText.gone()
                checklistLayout.gone()
            } else {
                it.checkList?.let {
                    it.forEachIndexed { index, checkListItem ->
                        val itemData = ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData(
                            checkListItem.name,
                            checkListItem.status,
                            checkListItem.optional,
                            checkListItem.frontImage,
                            checkListItem.backImage,
                            checkListItem.type,
                            checkListItem.dependency
                        )

                        checkListItemData.add(itemData)
                    }
                    if (checkListItemData.size > 0){
                        checklistLayout.visible()
                        inflateCheckListInCheckListContainer(
                            checkListItemData,
                            gigerInfo.jobProfileId
                            )
                    } else {
                        checklistText.gone()
                        checklistLayout.gone()
                    }

                }
            }
            stopShimmer(
                gigerinfoShimmerContainer as LinearLayout,
                R.id.shimmer_controller
            )
            gigerinfoShimmerContainer.gone()

        }
    }

    private fun inflateCheckListInCheckListContainer(
        checkListItemData: ArrayList<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>,
        jobProfileId: String
    ) = viewBinding.checklistLayout.apply{
        removeAllViews()

        checkListItemData.forEach {

            val view = PendingJoiningCheckListItemComponent(
                requireContext(),
                null,
                navigation = navigation,
                jobProfileId = jobProfileId
            )

            addView(view)
            view.bind(it)
        }
    }

    fun getFormattedDateFromYYMMDD(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd")
        val output = SimpleDateFormat("dd/MMM/yy", Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }

    fun getFormattedDate(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val output = SimpleDateFormat("dd/MMM/yy",Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }

    private fun showDetailsLoading() {

    }
}