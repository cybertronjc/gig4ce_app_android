package com.gigforce.user_preferences.location

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.user_preferences.R
import com.gigforce.common_ui.viewmodels.userpreferences.SharedPreferenceViewModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.around_current_address_fragment.*
import java.lang.Exception

class AroundCurrentAddressFragment : Fragment(), IOnBackPressedOverride {

    companion object {
        fun newInstance() = AroundCurrentAddressFragment()
    }

    private val viewModel: SharedPreferenceViewModel by viewModels()
    private var preferredDistanceActive: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.around_current_address_fragment, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
        initializeAll()
    }
    var SEEKBAR_REFRESH : Long= 500
    private fun listener() {
        Handler().postDelayed({
            try {
                arround_current_add_seekbar.progress = 0

                val progress = viewModel.getCurrentAddress()?.preferred_distance!!
                arround_current_add_seekbar.progress = progress
            }catch (e:Exception){

            }
        }, SEEKBAR_REFRESH)

        arround_current_add_seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                if(progress>0 && value>0) {
                    seekbardependent.visible()
                    seekbardependent.text = progress.toString() + " " + getString(R.string.km_pref)
                    seekbardependent.setX(seekBar.getX() + value + seekBar.getThumbOffset() / 2)
                }
                else{
                    seekbardependent.gone()
                }
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

//        val progress = viewModel.getCurrentAddress()?.preferred_distance!!
//        arround_current_add_seekbar.progress = progress

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
            showToast(getString(R.string.prefered_distance_zero_pref))
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
        return false
    }

}