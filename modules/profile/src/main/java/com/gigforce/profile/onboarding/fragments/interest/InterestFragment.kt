package com.gigforce.profile.onboarding.fragments.interest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import kotlinx.android.synthetic.main.interest_fragment.*

class InterestFragment : Fragment() {

    companion object {
        fun newInstance() = InterestFragment()
    }

    private lateinit var viewModel: InterestViewModel
    private var allInterestList = ArrayList<InterestDM>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.interest_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InterestViewModel::class.java)
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Driving"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Delivery Executive"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Sales"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Technician"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Helper"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Security"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Tele Calling"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Supervisor"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Cleaner"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Farmers"))
        context?.let {
            all_interests_rv.layoutManager = GridLayoutManager(
                activity, 3,
                GridLayoutManager.VERTICAL, false
            )
            all_interests_rv.adapter = AllInterestAdapter(
                it,
                allInterestList,
                object : AllInterestAdapter.OnDeliveryExecutiveClickListener {
                    override fun onclick() {
                        interest_cl.gone()
                        delivery_executive_detail_cl.visible()
                    }

                })


        }
    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_default_border
                )
            )
        }
    }

    private fun setSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.selected_image_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.selected_text_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_selection_border
                )
            )
        }

    }

}