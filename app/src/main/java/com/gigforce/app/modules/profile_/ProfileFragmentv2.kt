package com.gigforce.app.modules.profile_

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.android.synthetic.main.fragment_profile_v2.*

class ProfileFragmentv2 : BaseFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_profile_v2, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAppBar()
        initViews()
    }

    private fun initAppBar() {
        val act = activity as AppCompatActivity
        act.setSupportActionBar(toolbar)
        act.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        act.supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setColorFilter(
                resources.getColor(R.color.black),
                PorterDuff.Mode.SRC_ATOP
        );
        toolbar.setTitleTextColor(resources.getColor(R.color.black))
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.black))
        toolbar_layout.setCollapsedTitleTextColor(resources.getColor(R.color.black))
        var isShow = true
        var scrollRange = -1
        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                toolbar_layout.title = "Profile"
                iv_share_profile_v2.gone()
                isShow = true
            } else if (isShow) {
                iv_share_profile_v2.visible()
                toolbar_layout.title =
                        " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }


        })


    }

    private fun initViews() {
        initAddressCard()

    }

    private fun initAddressCard() {
        add_address_profile_v2.topLabel = R.string.current_address
        add_address_profile_v2.topIcon = R.drawable.ic_address_illustration
        add_address_profile_v2.contentIllustration = R.drawable.ic_address_illustration
        add_address_profile_v2.contentHeading = R.string.add_contact_address
        add_address_profile_v2.rightActionText = R.string.add_now
//        add_address_profile_v2.contentText = R.string.large_text
        add_address_profile_v2.setRightClickAction {
            this@ProfileFragmentv2.navigate(R.id.fragment_add_lang_profile_v2)
        }
//        PushDownAnim.setPushDownAnimTo(card_language_profile_v2.tv_add_now_profile_v2)
//            .setOnClickListener(
//                View.OnClickListener {
//                    navigate(R.id.fragment_add_lang_profile_v2)
//                })

    }
}