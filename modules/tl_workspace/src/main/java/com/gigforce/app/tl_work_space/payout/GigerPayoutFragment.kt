package com.gigforce.app.tl_work_space.payout

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.GigerPayoutFragmentBinding
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class GigerPayoutFragment : BaseFragment2<GigerPayoutFragmentBinding>(
    fragmentName = "GigerPayoutFragment",
    layoutId = R.layout.giger_payout_fragment,
    statusBarColor = R.color.status_bar_pink
) {

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: GigerPayoutViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: GigerPayoutFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        if (viewCreatedForTheFirstTime) {
            initView()
            observeViewStates()
            observeViewEffects()
        }
    }

    private fun initView() = viewBinding.apply{
        this.appBar.apply {
            setAppBarTitle(getToolBarTitleReceivedFromPreviousScreen() ?: "Retention")
            setBackButtonListener {

                if (isSearchCurrentlyShown) {
                    hideSoftKeyboard()
                } else {
                    findNavController().navigateUp()
                }
            }

            changeBackButtonDrawable()
            lifecycleScope.launchWhenCreated {

                search_item.getTextChangeAsStateFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->

                        Log.d("Search ", "Searhcingg...$searchString")
                        viewModel.setEvent(
                            GigerPayoutFragmentViewEvents.FilterApplied.SearchFilterApplied(
                                searchString
                            )
                        )
                    }
            }
        }

        this.payoutMainLayout.recyclerViewPayout.apply {
            Log.d("recyclerview", "$this")
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            setDiffUtilCallback(GigerPayoutListDiffUtil())
            setHasFixedSize(true)
        }

        this.swipeRefreshLayout.setOnRefreshListener {
            viewModel.setEvent(GigerPayoutFragmentViewEvents.RefreshGigerPayoutDataClicked)
        }
    }

    private fun observeViewStates() {

    }

    private fun observeViewEffects() {

    }

}