package com.gigforce.lead_management.ui.giger_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.core.extensions.gone
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentShowCheckListDocsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowCheckListDocsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "DropSelectionBottomSheetDialogFragment"
        const val INTENT_IMAGES_TO_SHOW = "images_to_show"
        const val INTENT_TOP_TITLE = "top_title"

        fun launch(
            imagesToShow: ArrayList<String>,
            topText: String,
            childFragmentManager : FragmentManager
        ){
            ShowCheckListDocsBottomSheet().apply {
                arguments = bundleOf(
                    INTENT_IMAGES_TO_SHOW to imagesToShow,
                    INTENT_TOP_TITLE to topText
                )
            }.show(childFragmentManager,TAG)
        }
    }

    private lateinit var viewBinding: FragmentShowCheckListDocsBottomSheetBinding
    var topTitle = ""
    var frontImage = ""
    var backImage = ""
    private lateinit var imagesToShowInViewPager: ArrayList<String>
    lateinit var adapter: CheckListViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            imagesToShowInViewPager = it.getStringArrayList(ShowCheckListDocsBottomSheet.INTENT_IMAGES_TO_SHOW) ?: return@let
            topTitle = it.getString(ShowCheckListDocsBottomSheet.INTENT_TOP_TITLE) ?: return@let
        }

        savedInstanceState?.let {
            imagesToShowInViewPager = it.getStringArrayList(ShowCheckListDocsBottomSheet.INTENT_IMAGES_TO_SHOW) ?: return@let
            topTitle = it.getString(ShowCheckListDocsBottomSheet.INTENT_TOP_TITLE) ?: return@let
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentShowCheckListDocsBottomSheetBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
        listeners()
    }

    private fun setViews() = viewBinding.apply{
        adapter = CheckListViewPagerAdapter()
        adapter.setItem(imagesToShowInViewPager)
        viewPager2.adapter = adapter
        if (imagesToShowInViewPager.size == 1) {
            tabLayout.gone()
        }
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
        }.attach()

        topText.setText(topTitle)
    }

    private fun listeners()  {
        viewBinding.okayBtn.setOnClickListener {
            this.dismiss()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(
            ShowCheckListDocsBottomSheet.INTENT_IMAGES_TO_SHOW,
            imagesToShowInViewPager
        )
        outState.putString(
            ShowCheckListDocsBottomSheet.INTENT_TOP_TITLE,
            topTitle
        )
    }




}