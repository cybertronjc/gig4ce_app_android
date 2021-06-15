package com.gigforce.giger_gigs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.databinding.FragmentGigerUnderManagersAttendanceBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndViewModelData
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModel
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModelState
import com.gigforce.giger_gigs.viewModels.SharedGigerAttendanceUnderManagerViewModel
import com.jaeger.library.StatusBarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDate

class GigersAttendanceUnderManagerFragment : Fragment() {

    private val sharedGigViewModel: SharedGigerAttendanceUnderManagerViewModel by activityViewModels()
    private val viewModel: GigerAttendanceUnderManagerViewModel by viewModels()
    private lateinit var viewBinding: FragmentGigerUnderManagersAttendanceBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentGigerUnderManagersAttendanceBinding.inflate(
                inflater,
                container,
                false
        )
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
        getAttendanceFor(LocalDate.now())
    }

    private fun initView() {
        viewBinding.gigersUnderManagerMainLayout.attendanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.toolbar.setBackButtonListener{
            if (viewBinding.toolbar.isSearchCurrentlyShown) {
                hideSoftKeyboard()
            } else {
                activity?.onBackPressed()
            }
        }

        lifecycleScope.launch {

            viewBinding.toolbar.showTitle("Gigers Attendance")
            viewBinding.toolbar.showSearchOption("Search Attendance")
            viewBinding.toolbar.getSearchTextChangeAsFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->
                        Log.d("Search ","Searhcingg...$searchString")

                       viewModel.searchAttendance(searchString)
                    }
        }
    }

    private fun getAttendanceFor(date: LocalDate) {
        viewModel.fetchUsersAttendanceDate(date)
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(
                resources,
                R.color.lipstick_two,
                null
        ))
    }

    private fun initViewModel() {

        viewModel.gigerAttendanceUnderManagerViewState
                .observe(viewLifecycleOwner, {

                    when (it) {
                        is GigerAttendanceUnderManagerViewModelState.AttendanceDataLoaded -> showStatusAndAttendanceOnView(
                                it.attendanceStatuses,
                                it.attendanceItemData
                        )
                        is GigerAttendanceUnderManagerViewModelState.ErrorInLoadingDataFromServer -> errorInLoadingAttendanceFromServer(
                                it.error,
                                it.shouldShowErrorButton
                        )
                        GigerAttendanceUnderManagerViewModelState.LoadingDataFromServer -> showDataLoadingFromServer()
                        GigerAttendanceUnderManagerViewModelState.NoAttendanceFound -> noAttendanceFound()
                    }
                })
    }

    private fun showStatusAndAttendanceOnView(
            attendanceStatuses: List<AttendanceStatusAndCountItemData>,
            attendanceItemData: List<AttendanceRecyclerItemData>
    ) = viewBinding.apply {

        this.gigersUnderManagerMainError.gone()
        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()

            stopShimmer(
                    this.statusShimmerContainer as LinearLayout,
                    R.id.chip_like_shimmer_controller
            )
            this.statusesRecyclerview.collection = attendanceStatuses.map {
                AttendanceStatusAndViewModelData(
                        it,
                        viewModel
                )
            }

            stopShimmer(
                    this.attendanceShimmerContainer as LinearLayout,
                    R.id.shimmer_controller
            )
            this.attendanceRecyclerView.collection = attendanceItemData
        }
    }


    private fun errorInLoadingAttendanceFromServer(
            error: String,
            shouldShowRetryButton: Boolean
    ) = viewBinding.apply {

        stopShimmer(
                this.gigersUnderManagerMainLayout.attendanceShimmerContainer as LinearLayout,
                R.id.shimmer_controller
        )
        stopShimmer(
                this.gigersUnderManagerMainLayout.statusShimmerContainer as LinearLayout,
                R.id.chip_like_shimmer_controller
        )

        this.gigersUnderManagerMainLayout.root.gone()
        this.gigersUnderManagerMainError.visible()
        this.gigersUnderManagerMainError.text = error
    }


    private fun noAttendanceFound() = viewBinding.apply {

        this.gigersUnderManagerMainLayout.root.gone()
        this.gigersUnderManagerMainError.visible()
        this.gigersUnderManagerMainError.text = "No Attendance Found"
    }


    private fun showDataLoadingFromServer() = viewBinding.apply {

        this.gigersUnderManagerMainError.gone()
        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()

            this.statusesRecyclerview.collection = emptyList()
            startShimmer(
                    this.statusShimmerContainer as LinearLayout,
                    ShimmerDataModel(
                            cardRes = com.gigforce.common_ui.R.layout.shimmer_chip_like_layout,
                            minHeight = R.dimen.size_60,
                            minWidth = R.dimen.size_90,
                            marginRight = R.dimen.size_8,
                            marginTop = R.dimen.size16,
                            marginLeft = R.dimen.size_1,
                            orientation = LinearLayout.HORIZONTAL
                    ),
                    R.id.chip_like_shimmer_controller
            )

            this.attendanceRecyclerView.collection = emptyList()
            startShimmer(
                    this.attendanceShimmerContainer as LinearLayout,
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
    }

    private fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()?.getWindowToken(), 0)
    }

    companion object {
        const val TAG = "role"
    }
}