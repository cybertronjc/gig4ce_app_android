package com.gigforce.app.modules.preferences.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.profile.models.AddressModel
import kotlinx.android.synthetic.main.around_current_address_fragment.*

class AroundCurrentAddressFragment : BaseFragment() {

    companion object {
        fun newInstance() = AroundCurrentAddressFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private var preferredDistanceActive: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.around_current_address_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeAll()
        listener()
    }

    private fun listener() {
        arround_current_add_seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value =
                    (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekbardependent.text = progress.toString() + " " + getString(R.string.km)
                seekbardependent.setX(seekBar.getX() + value + seekBar.getThumbOffset() / 2)
                //textView.setY(100); just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        workFromHomeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                around_current_add_cl.isVisible = isChecked
        }

        back_arrow_iv.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initializeAll() {
        workFromHomeSwitch.isChecked = viewModel.getCurrentAddress()?.preferredDistanceActive!!
        preferredDistanceActive = viewModel.getCurrentAddress()?.preferredDistanceActive!!
        arround_current_add_seekbar.progress = 0

        val progress = viewModel.getCurrentAddress()?.preferred_distance!!
        arround_current_add_seekbar.progress = progress
       //TODO : Extract String Resource
        val value =

            (progress * (arround_current_add_seekbar.getWidth() - 2 * arround_current_add_seekbar.getThumbOffset())) / arround_current_add_seekbar.getMax()
        seekbardependent.text = progress.toString() +" "  +"Km"
        seekbardependent.setX(arround_current_add_seekbar.getX() + value + arround_current_add_seekbar.getThumbOffset() / 2)


//        arround_current_add_seekbar.setOtherView(seekbardependent,false,"Km")
        populateAddress(viewModel.getCurrentAddress()!!)
        setVisibilityAroundCurrAdd()
    }



    private fun setVisibilityAroundCurrAdd() {
        if (viewModel.getCurrentAddress()?.preferredDistanceActive!!)
            around_current_add_cl.visibility = View.VISIBLE
        else
            around_current_add_cl.visibility = View.GONE

    }

    private fun populateAddress(address: AddressModel) {
        line1view.text = address.firstLine
        line2view.text = address.secondLine
        areaview.text = address.area
        cityview.text = address.city
        stateview.text = address.state
        pincodeview.text = address.pincode

    }

    override fun onBackPressed(): Boolean {
        if (arround_current_add_seekbar.progress == 0) {
            showToast(getString(R.string.prefered_distance_zero))
            viewModel.setCurrentAddressPrferredDistanceData(
                arround_current_add_seekbar.progress,
                false
            )
        } else {
            preferredDistanceActive = workFromHomeSwitch.isChecked
            viewModel.setCurrentAddressPrferredDistanceData(
                arround_current_add_seekbar.progress,
                preferredDistanceActive
            )
        }
        return super.onBackPressed()
    }

}