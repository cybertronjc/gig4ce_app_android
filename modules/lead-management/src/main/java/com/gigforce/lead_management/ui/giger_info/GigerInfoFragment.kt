package com.gigforce.lead_management.ui.giger_info


import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigerInfo
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.inflate
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.GlideApp
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.GigerInfoFragmentBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.drop_selection.DropSelectionBottomSheetDialogFragment
import com.gigforce.lead_management.ui.giger_info.views.AppCheckListRecyclerComponent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_below_giger_functionality.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GigerInfoFragment : BaseFragment2<GigerInfoFragmentBinding>(
    fragmentName = "GigerInfoFragment",
    layoutId = R.layout.giger_info_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = GigerInfoFragment()
        private const val TAG = "GigerInfoFragment"
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: GigerInfoViewModel by viewModels()
    private val sharedViewModel : LeadManagementSharedViewModel by activityViewModels()

    var gigerPhone = ""
    private lateinit var joiningId: String

    override fun viewCreated(viewBinding: GigerInfoFragmentBinding, savedInstanceState: Bundle?) {
        getDataFrom(arguments,savedInstanceState)
        initToolbar(viewBinding)
        initListeners()
        initViewModel()
        initSharedViewModel()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID) ?: return@let

        }

        savedInstanceState?.let {
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID) ?: return@let

        }
    }
    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID, joiningId)
    }


    private fun initViewModel() {
        viewModel.getGigerJoiningInfo(joiningId)
        //observe data
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when(state) {

                is GigerInfoState.ErrorLoadingData -> showErrorLoadingInfo(
                    state.error
                )
                is GigerInfoState.GigerInfoLoaded -> showGigerInfo(state.gigerInfo)

                GigerInfoState.LoadingDataFromServer -> showLoadingInfo()
            }
        })
    }

    private fun initSharedViewModel() {
        sharedViewModel.viewState
            .observe(viewLifecycleOwner,{
                when (it) {
                    LeadManagementSharedViewModelState.OneOrMoreSelectionsDropped -> viewModel.getGigerJoiningInfo(joiningId)
                }
            })
    }

    private fun showLoadingInfo() = viewBinding.apply{
        checklistLayout.removeAllViews()
        mainScrollView.gone()
        gigerinfoShimmerContainer.visible()

        startShimmer(
            this.gigerinfoShimmerContainer as LinearLayout,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }

    private fun showGigerInfo(gigerInfo: GigerInfo) = viewBinding.apply {
        stopShimmer(
            gigerinfoShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigerinfoShimmerContainer.gone()
        mainScrollView.visible()


        gigerInfo.let {
            toolbar.showTitle(it.gigerName)
            toolbar.setTitleTypeface(Typeface.BOLD)
            overlayCardLayout.companyName.text = ": "+it.businessName ?: ""
            overlayCardLayout.jobProfileTitle.text = it.jobProfileTitle ?: ""
            overlayCardLayout.locationText.text = ": "+it.reportingLocation + ", " + it.businessLocation ?: ""

            gigerPhone = it.gigerPhone.toString()
            toolbar.showSubtitle(gigerPhone)
            setGigerProfilePicture(it.gigerProfilePicture.toString())

            context?.let { it1 ->
                GlideApp.with(it1)
                    .load(it.businessLogo)
                    .placeholder(getCircularProgressDrawable(it1))
                    .into(overlayCardLayout.profileImageOverlay.companyImg)
            }

            applicationStatusLayout.statusText.text = getString(R.string.application,it.status)
            if (it.status == "Pending"){
                applicationStatusLayout.statusIconImg.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_pending_icon,null))
                applicationStatusLayout.root.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.status_background_pink,null))
            } else {
                applicationStatusLayout.statusIconImg.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_blue_tick,null))
                applicationStatusLayout.root.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.status_background_blue,null))
            }

            overlayCardLayout.selectionDate.text = ": "+getFormattedDate(it.selectionDate)
            overlayCardLayout.joiningDate.text = ": "+getFormattedDateFromYYMMDD(it.joiningDate) ?: ""

            val checkListItemData = arrayListOf<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>()
            if (it.checkList == null){
                checklistText.gone()
                checklistLayout.gone()
            } else {
                 it.checkList?.let {
                it.forEachIndexed { index, checkListItem ->
                    val itemData = ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData(checkListItem.name, checkListItem.status, checkListItem.optional)
                    checkListItemData.add(itemData)
                }
                if (checkListItemData.size > 0){
                    checklistLayout.visible()
                    inflateCheckListInCheckListContainer(checkListItemData)
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
        checkListItemData: ArrayList<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>
    ) = viewBinding.checklistLayout.apply{
        removeAllViews()

        checkListItemData.forEach {

            val view = AppCheckListRecyclerComponent(requireContext(),null)
            addView(view)
            view.bind(it)
        }
    }

    private fun showErrorLoadingInfo(error: String) = viewBinding.apply{
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

    private fun initListeners() = viewBinding.apply {
        bottomButtonLayout.dropGigerBtn.setOnClickListener {
            //drop functionality
            DropSelectionBottomSheetDialogFragment.launch(
                arrayListOf(joiningId),
                childFragmentManager
            )
        }
        bottomButtonLayout.callLayout.setOnClickListener {
            //call functionality
            val intent =
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.fromParts("tel", gigerPhone, null)
                )
            context?.startActivity(intent)

        }

    }

    private fun initToolbar(viewBinding: GigerInfoFragmentBinding) = viewBinding.toolbar.apply {
        this.showTitle("Giger name")
        this.hideActionMenu()
        this.changeBackButtonDrawable()
        this.setBackButtonListener(View.OnClickListener {
            //back functionality
            activity?.onBackPressed()
        })
    }


    fun setGigerProfilePicture(path: String) {
        if (!path.isBlank()) {

            if (path.isEmpty() || path == "avatar.jpg") {
                viewBinding.overlayCardLayout.profileImageOverlay.gigerImg.setImageDrawable(
                    ResourcesCompat.getDrawable(resources,R.drawable.ic_avatar_male,null)
                )
                return
            }

            val userPathInFirebase = if (path.startsWith("profile_pics/"))
                path
            else
                "profile_pics/${path}"

            viewBinding.overlayCardLayout.profileImageOverlay.gigerImg.loadImageIfUrlElseTryFirebaseStorage(
                userPathInFirebase,
                R.drawable.ic_avatar_male,
                R.drawable.ic_avatar_male
            )
        } else {
                viewBinding.overlayCardLayout.profileImageOverlay.gigerImg.loadImage(R.drawable.ic_avatar_male)
        }
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

    fun getFormattedDateFromYYMMDD(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd")
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

}