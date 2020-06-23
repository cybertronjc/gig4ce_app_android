package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import kotlinx.android.synthetic.main.help_expanded_page.*

class HelpExpandedPage: WalletBaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.help_expanded_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListernere()
    }

    private fun initialize() {

    }

    private fun setListernere() {
        toggle_terminology.setOnClickListener {
            if (terminologies.visibility == View.VISIBLE) {
                terminologies.visibility = View.GONE
                bt_expand_terminology.icon = resources.getDrawable(R.drawable.ic_baseline_forward_24)
            }
            else {
                terminologies.visibility = View.VISIBLE
                bt_expand_terminology.icon = resources.getDrawable(R.drawable.ic_baseline_up_24)
            }
        }
    }
}
