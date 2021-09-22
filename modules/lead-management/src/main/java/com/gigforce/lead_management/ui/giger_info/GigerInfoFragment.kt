package com.gigforce.lead_management.ui.giger_info


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigerInfo
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.GigerInfoFragmentBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_below_giger_functionality.*
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
    var gigerPhone = ""

    override fun viewCreated(viewBinding: GigerInfoFragmentBinding, savedInstanceState: Bundle?) {
        initToolbar(viewBinding)
        initListeners()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.getGigerJoiningInfo()
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

    private fun showLoadingInfo() = viewBinding.apply{
        checklistRv.collection = emptyList()
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
        //update ui
        gigerInfo?.let {
            toolbar.showTitle(it.gigerName)
            overlayCardLayout.companyName.text = it.businessName
            overlayCardLayout.jobProfileTitle.text = it.jobProfileTitle
            overlayCardLayout.locationText.text = it.businessLocation
            overlayCardLayout.joiningDate.text = it.joiningDate
            overlayCardLayout.selectionDate.text = it.selectionDate
            gigerPhone = it.gigerPhone.toString()

            context?.let { it1 ->
                GlideApp.with(it1)
                    .load(it.businessLogo)
                    .placeholder(getCircularProgressDrawable(it1))
                    .into(overlayCardLayout.profileImageOverlay.companyImg)
            }

            context?.let { it1 ->
                GlideApp.with(it1)
                    .load(it.gigerProfilePicture)
                    .placeholder(getCircularProgressDrawable(it1))
                    .into(overlayCardLayout.profileImageOverlay.gigerImg)
            }

            applicationStatusLayout.statusText.text = it.status
            applicationStatusLayout.statusIconImg.setImageDrawable(
                if (it.status == "Application Pending") resources.getDrawable(R.drawable.ic_pending_icon) else resources.getDrawable(R.drawable.ic_blue_tick)
            )

            val checkListItemData = arrayListOf<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>()
            it.checkList.forEachIndexed { index, checkListItem ->
                val itemData = ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData(checkListItem.name, checkListItem.status, checkListItem.optional)
                checkListItemData.add(itemData)
            }
            stopShimmer(
                gigerinfoShimmerContainer as LinearLayout,
                R.id.shimmer_controller
            )
            gigerinfoShimmerContainer.gone()
            checklistRv.collection = checkListItemData
        }
    }

    private fun showErrorLoadingInfo(error: String) = viewBinding.apply{
        checklistRv.collection = emptyList()
        stopShimmer(
            gigerinfoShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigerinfoShimmerContainer.gone()
    }

    private fun initListeners() = viewBinding.apply {
        bottomButtonLayout.dropGigerBtn.setOnClickListener {
            //drop functionality

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

//        topLayout.backImageButton.setOnClickListener {
//            //back functionality
//        }
    }

    private fun initToolbar(viewBinding: GigerInfoFragmentBinding) = viewBinding.toolbar.apply {
        this.showTitle("Giger name")
        this.hideActionMenu()
        this.hideSubTitle()
        this.changeBackButtonDrawable()
        this.setBackButtonListener(View.OnClickListener {
            //back functionality
            activity?.onBackPressed()
        })
    }


}