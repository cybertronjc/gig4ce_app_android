package com.gigforce.app.modules.chatmodule.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.TextDrawable
import kotlinx.android.synthetic.main.chat_contact_item.*
import kotlinx.android.synthetic.main.fragment_fake_contact_screen.*

class FakeGigContactScreenFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_fake_contact_screen, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back_arrow.setOnClickListener {
            activity?.onBackPressed()
        }

        tv_nameValue.text = "Help Contact"
        tv_dateValue.text = "040 - 48217315"


        val drawable = TextDrawable.builder().buildRound(
            "H",
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
        )

        contactImage.setImageDrawable(drawable)
        contactItemRoot.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "04048217315", null))
            startActivity(intent)

        }
    }
}