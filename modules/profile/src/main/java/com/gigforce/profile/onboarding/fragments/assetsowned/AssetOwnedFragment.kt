package com.gigforce.profile.onboarding.fragments.assetsowned

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.asset_owned_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class AssetOwnedFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment(),OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) = AssetOwnedFragment(formCompletionListener)
    }

    @Inject lateinit var eventTracker: IEventTracker
    private lateinit var viewModel: AssetOwnedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.asset_owned_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssetOwnedViewModel::class.java)
        listeners()
        setSelected(icon_iv_6, smart_phone, imageTextCardcl_6)
        owned_smart_phone = true
    }
    lateinit  var twoWheeler: HashMap<String, Boolean>
    lateinit  var threeWheeler: HashMap<String, Boolean>
    lateinit  var other: HashMap<String, Boolean>
    lateinit  var itItems: HashMap<String, Boolean>

    var owned_bicycle = false
    var owned_electric_bike = false
    var owned_motor_bike = false

    var owned_e_rickshaw = false
    var owned_auto_rickshaw = false

    var owned_car = false
    var owned_commercial_vehicle = false

    var owned_laptop = false
    var owned_smart_phone = false
    var owned_pc = false
    private fun listeners() {
        imageTextCardcl.setOnClickListener {

            if (owned_bicycle) {
                resetSelected(icon_iv, bicycle, imageTextCardcl)
                owned_bicycle = false
                twoWheeler.put("Bicycle", false)
            } else {
                setSelected(icon_iv, bicycle, imageTextCardcl)
                owned_bicycle = true
                twoWheeler.put("Bicycle", true)
            }
            validateForm()
        }
        imageTextCardcl_.setOnClickListener {
            if (owned_electric_bike) {
                resetSelected(icon_iv1, electric_bike, imageTextCardcl_)
                owned_electric_bike = false
                twoWheeler.put("Electric Bike", false)
            } else {
                setSelected(icon_iv1, electric_bike, imageTextCardcl_)
                owned_electric_bike = true
                twoWheeler.put("Electric Bike", true)
            }
            validateForm()
        }

        imageTextCardcl_x.setOnClickListener {
            if (owned_motor_bike) {
                resetSelected(icon_iv_x, motor_bike, imageTextCardcl_x)
                owned_motor_bike = false
                twoWheeler.put("Motor Bike", false)
            } else {
                setSelected(icon_iv_x, motor_bike, imageTextCardcl_x)
                owned_motor_bike = true
                twoWheeler.put("Motor Bike", true)
            }
            validateForm()
        }

        imageTextCardcl_1.setOnClickListener {
            if (owned_e_rickshaw) {
                resetSelected(icon_iv_1, e_rickshaw, imageTextCardcl_1)
                owned_e_rickshaw = false
                threeWheeler.put("E - Rickshaw", true)
            } else {
                setSelected(icon_iv_1, e_rickshaw, imageTextCardcl_1)
                owned_e_rickshaw = true
                threeWheeler.put("E - Rickshaw", false)
            }
            validateForm()
        }

        imageTextCardcl_2.setOnClickListener {
            if (owned_auto_rickshaw) {
                resetSelected(icon_iv_2, auto_rickshaw, imageTextCardcl_2)
                owned_auto_rickshaw = false
                threeWheeler.put("Auto Rickshaw", false)
            } else {
                setSelected(icon_iv_2, auto_rickshaw, imageTextCardcl_2)
                owned_auto_rickshaw = true
                threeWheeler.put("Auto Rickshaw", true)
            }
            validateForm()
        }

        imageTextCardcl_3.setOnClickListener {
            if (owned_car) {
                resetSelected(icon_iv_3, car, imageTextCardcl_3)
                owned_car = false
                other.put("Car", false)
            } else {
                setSelected(icon_iv_3, car, imageTextCardcl_3)
                owned_car = true
                other.put("Car", true)
            }
            validateForm()
        }
        imageTextCardcl_4.setOnClickListener {
            if (owned_commercial_vehicle) {
                resetSelected(icon_iv_4, commercial_vehicle, imageTextCardcl_4)
                owned_commercial_vehicle = false
                other.put("Commercial Vehicle", false)
            } else {
                setSelected(icon_iv_4, commercial_vehicle, imageTextCardcl_4)
                owned_commercial_vehicle = true
                other.put("Commercial Vehicle", true)
            }
            validateForm()
        }

        imageTextCardcl5.setOnClickListener {
            if (owned_laptop) {
                resetSelected(icon_iv_5, laptop, imageTextCardcl5)
                owned_laptop = false
                itItems.put("Laptop", false)
            } else {
                setSelected(icon_iv_5, laptop, imageTextCardcl5)
                owned_laptop = true
                itItems.put("Laptop", true)
            }
            validateForm()
        }

        imageTextCardcl_6.setOnClickListener {
            if (owned_smart_phone) {
                resetSelected(icon_iv_6, smart_phone, imageTextCardcl_6)
                owned_smart_phone = false
                itItems.put("Smart Phone", false)
            } else {
                setSelected(icon_iv_6, smart_phone, imageTextCardcl_6)
                owned_smart_phone = true
                itItems.put("Smart Phone", true)
            }
            validateForm()
        }

        imageTextCardcl_7.setOnClickListener {
            if (owned_pc) {
                resetSelected(icon_iv_7, pc, imageTextCardcl_7)
                owned_pc = false
                itItems.put("Personal Computer", false)
            } else {
                setSelected(icon_iv_7, pc, imageTextCardcl_7)
                owned_pc = true
                itItems.put("Personal Computer", true)
            }
            validateForm()
        }

    }

    private fun validateForm() {
        formCompletionListener.enableDisableNextButton(true)
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

    fun getAssetsData(): Map<String,Any> {
        return mapOf("assetsOwned" to mapOf("twoWheeler" to mapOf("ownedBicycle" to owned_bicycle, "ElectricBike" to owned_electric_bike,"MotorBike" to owned_motor_bike),
                    "threeWheeler" to mapOf("eRickshaw" to owned_e_rickshaw,"autoRickshaw" to owned_auto_rickshaw),
                "other" to mapOf("car" to owned_car,"commercialVehicle" to owned_commercial_vehicle),
                "it" to mapOf("laptop" to owned_laptop,"smartPhone" to owned_smart_phone, "pc" to owned_pc))
        )
    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener.enableDisableNextButton(true)
        return false
    }

    override fun nextButtonActionFound(): Boolean {
        var props = HashMap<String, Any>()
        props.put("Two Wheeler", twoWheeler)
        props.put("Three Wheeler", threeWheeler)
        props.put("Other", other)
        props.put("IT", itItems)

        eventTracker.pushEvent(TrackingEventArgs("Assets Owned",props))
        eventTracker.setUserProperty(props)
        return false
    }

    override fun activeNextButton() {
        formCompletionListener.enableDisableNextButton(true)
    }

}