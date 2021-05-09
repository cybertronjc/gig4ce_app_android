package com.gigforce.profile.onboarding.fragments.assetsowned

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.onboarding.MiddleDividerItemDecoration
import com.gigforce.profile.onboarding.OnFragmentFormCompletionListener
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import com.gigforce.profile.onboarding.fragments.interest.InterestDM
import com.gigforce.profile.onboarding.fragments.interest.InterestFragment
import com.gigforce.profile.onboarding.fragments.interest.SkillDetailsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.asset_owned_fragment.*
import kotlinx.android.synthetic.main.asset_owned_fragment.icon_iv1
import kotlinx.android.synthetic.main.asset_owned_fragment.icon_iv_1
import kotlinx.android.synthetic.main.asset_owned_fragment.imageTextCardcl
import kotlinx.android.synthetic.main.asset_owned_fragment.imageTextCardcl_
import kotlinx.android.synthetic.main.image_text_item_view.view.*
import kotlinx.android.synthetic.main.interest_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class AssetOwnedFragment() : Fragment(),OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener,OnboardingFragmentNew.SetInterfaceListener {

    companion object {
        fun newInstance() = AssetOwnedFragment()
        private var allAssetsList = ArrayList<AssestDM>()
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
        observer()
        listeners()
    }

    private fun observer() {
        //getting assets from DB
        viewModel.getAssets()
        viewModel.assets.observe(viewLifecycleOwner, Observer {
            allAssetsList = it
            Log.d("asset list", allAssetsList.toString())
            setUpTypedRecyclerView("two_wheeler", allAssetsList)
            setUpTypedRecyclerView("three_wheeler", allAssetsList)
            setUpTypedRecyclerView("other", allAssetsList)
            setUpTypedRecyclerView("it", allAssetsList)
        })
    }

    private fun setUpTypedRecyclerView(type: String, assetsList: ArrayList<AssestDM>){
        when(type){
            "two_wheeler" -> {
                context?.let {
                    two_wheeler_rv.layoutManager = GridLayoutManager(
                        activity, 4,
                        GridLayoutManager.VERTICAL, false
                    )
                    two_wheeler_rv.adapter = AssetAdapter(
                        it,
                        getTypedAssetsList("two_wheeler", assetsList),
                        object : AssetAdapter.OnAssestClickListener {
                            override fun onclick(view: View, position: Int) {
                                var foundSelected = false
                                if (allAssetsList.get(position).selected) {
                                    resetSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = false
                                    foundSelected = true
                                }
                                else{
                                    setSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = true
                                }
                                validateForm()
                            }
                        }, this
                    )
                    context?.let {
                        val itemDecoration =
                            MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
                        itemDecoration.setDividerColor(ContextCompat.getColor(it, R.color.light_blue_cards))
                        two_wheeler_rv.addItemDecoration(itemDecoration)
                    }
                }
            }
            "three_wheeler" -> {
                context?.let {
                    three_wheeler_rv.layoutManager = GridLayoutManager(
                        activity, 4,
                        GridLayoutManager.VERTICAL, false
                    )
                    three_wheeler_rv.adapter = AssetAdapter(
                        it,
                        getTypedAssetsList("three_wheeler", assetsList),
                        object : AssetAdapter.OnAssestClickListener {
                            override fun onclick(view: View, position: Int) {
                                var foundSelected = false
                                if (allAssetsList.get(position).selected) {
                                    resetSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = false
                                    foundSelected = true
                                }
                                else{
                                    setSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = true
                                }
                                validateForm()
                            }
                        }, this
                    )
                    context?.let {
                        val itemDecoration =
                            MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
                        itemDecoration.setDividerColor(ContextCompat.getColor(it, R.color.light_blue_cards))
                        three_wheeler_rv.addItemDecoration(itemDecoration)
                    }
                }
            }
            "other" -> {
                context?.let {
                    other_assets_rv.layoutManager = GridLayoutManager(
                        activity, 4,
                        GridLayoutManager.VERTICAL, false
                    )
                    other_assets_rv.adapter = AssetAdapter(
                        it,
                        getTypedAssetsList("other", assetsList),
                        object : AssetAdapter.OnAssestClickListener {
                            override fun onclick(view: View, position: Int) {
                                var foundSelected = false
                                if (allAssetsList.get(position).selected) {
                                    resetSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = false
                                    foundSelected = true
                                }
                                else{
                                    setSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = true
                                }
                                validateForm()
                            }
                        }, this
                    )
                    context?.let {
                        val itemDecoration =
                            MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
                        itemDecoration.setDividerColor(ContextCompat.getColor(it, R.color.light_blue_cards))
                        other_assets_rv.addItemDecoration(itemDecoration)
                    }
                }
            }
            "it" -> {
                context?.let {
                    it_assets_rv.layoutManager = GridLayoutManager(
                        activity, 4,
                        GridLayoutManager.VERTICAL, false
                    )
                    it_assets_rv.adapter = AssetAdapter(
                        it,
                          getTypedAssetsList("it", assetsList),
                        object : AssetAdapter.OnAssestClickListener {
                            override fun onclick(view: View, position: Int) {
                                var foundSelected = false
                                if (allAssetsList.get(position).selected) {
                                    resetSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = false
                                    foundSelected = true
                                }
                                else{
                                    setSelected(view.icon_iv, view.interest_name, view)
                                    allAssetsList.get(position).selected = true
                                }
                                validateForm()
                            }
                        }, this
                    )
                    context?.let {
                        val itemDecoration =
                            MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
                        itemDecoration.setDividerColor(ContextCompat.getColor(it, R.color.light_blue_cards))
                        it_assets_rv.addItemDecoration(itemDecoration)
                    }
                }
            }
        }
    }

    private fun getTypedAssetsList(type: String, assetsList: ArrayList<AssestDM>): ArrayList<AssestDM>{
        var list : ArrayList<AssestDM> = arrayListOf()
        for (i in 0..assetsList.size - 1){
            if (assetsList.get(i).assetsType == type){
                list.add(assetsList.get(i))
            }
        }
        return list
    }

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
            } else {
                setSelected(icon_iv, bicycle, imageTextCardcl)
                owned_bicycle = true
            }
            validateForm()
        }
        imageTextCardcl_.setOnClickListener {
            if (owned_electric_bike) {
                resetSelected(icon_iv1, electric_bike, imageTextCardcl_)
                owned_electric_bike = false
            } else {
                setSelected(icon_iv1, electric_bike, imageTextCardcl_)
                owned_electric_bike = true
            }
            validateForm()
        }

        imageTextCardcl_x.setOnClickListener {
            if (owned_motor_bike) {
                resetSelected(icon_iv_x, motor_bike, imageTextCardcl_x)
                owned_motor_bike = false
            } else {
                setSelected(icon_iv_x, motor_bike, imageTextCardcl_x)
                owned_motor_bike = true
            }
            validateForm()
        }

        imageTextCardcl_1.setOnClickListener {
            if (owned_e_rickshaw) {
                resetSelected(icon_iv_1, e_rickshaw, imageTextCardcl_1)
                owned_e_rickshaw = false
            } else {
                setSelected(icon_iv_1, e_rickshaw, imageTextCardcl_1)
                owned_e_rickshaw = true
            }
            validateForm()
        }

        imageTextCardcl_2.setOnClickListener {
            if (owned_auto_rickshaw) {
                resetSelected(icon_iv_2, auto_rickshaw, imageTextCardcl_2)
                owned_auto_rickshaw = false
            } else {
                setSelected(icon_iv_2, auto_rickshaw, imageTextCardcl_2)
                owned_auto_rickshaw = true
            }
            validateForm()
        }

        imageTextCardcl_3.setOnClickListener {
            if (owned_car) {
                resetSelected(icon_iv_3, car, imageTextCardcl_3)
                owned_car = false
            } else {
                setSelected(icon_iv_3, car, imageTextCardcl_3)
                owned_car = true
            }
            validateForm()
        }
        imageTextCardcl_4.setOnClickListener {
            if (owned_commercial_vehicle) {
                resetSelected(icon_iv_4, commercial_vehicle, imageTextCardcl_4)
                owned_commercial_vehicle = false
            } else {
                setSelected(icon_iv_4, commercial_vehicle, imageTextCardcl_4)
                owned_commercial_vehicle = true
            }
            validateForm()
        }

        imageTextCardcl5.setOnClickListener {
            if (owned_laptop) {
                resetSelected(icon_iv_5, laptop, imageTextCardcl5)
                owned_laptop = false
            } else {
                setSelected(icon_iv_5, laptop, imageTextCardcl5)
                owned_laptop = true
            }
            validateForm()
        }

        imageTextCardcl_6.setOnClickListener {
            if (owned_smart_phone) {
                resetSelected(icon_iv_6, smart_phone, imageTextCardcl_6)
                owned_smart_phone = false
            } else {
                setSelected(icon_iv_6, smart_phone, imageTextCardcl_6)
                owned_smart_phone = true
            }
            validateForm()
        }

        imageTextCardcl_7.setOnClickListener {
            if (owned_pc) {
                resetSelected(icon_iv_7, pc, imageTextCardcl_7)
                owned_pc = false
            } else {
                setSelected(icon_iv_7, pc, imageTextCardcl_7)
                owned_pc = true
            }
            validateForm()
        }

    }

    private fun validateForm() {
        formCompletionListener?.enableDisableNextButton(true)
    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                            it,
                            R.drawable.rect_light_blue_border
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

    private fun getDataForAnalytics(type: String): Map<String, Any> {
        var map = mapOf<String, Any>()
        allAssetsList.forEach {
            if ( it.assetsType.equals(type) ){
                map = mapOf(type to mapOf(it.name to it.selected))
            }
        }
        return map
    }

//    fun getAssetsData(): Map<String,Any> {
//        return mapOf("assetsOwned" to mapOf("twoWheeler" to mapOf("ownedBicycle" to owned_bicycle, "ElectricBike" to owned_electric_bike,"MotorBike" to owned_motor_bike),
//                    "threeWheeler" to mapOf("eRickshaw" to owned_e_rickshaw,"autoRickshaw" to owned_auto_rickshaw),
//                "other" to mapOf("car" to owned_car,"commercialVehicle" to owned_commercial_vehicle),
//                "it" to mapOf("laptop" to owned_laptop,"smartPhone" to owned_smart_phone, "pc" to owned_pc))
//        )
//    }


    fun getAssetsData(): Map<String, Any> {
        return mapOf("assetsOwned" to mapOf("twoWheeler" to getDataForAnalytics("two_wheeler"),
                "threeWheeler" to getDataForAnalytics("three_wheeler"),
                "other" to getDataForAnalytics("other"),
                "it" to getDataForAnalytics("it")

        ))
    }

    fun getAssetsDataForAnalytics(): Map<String,Any> {

        val twoWheelersOwned = mutableListOf<String>()
        if(owned_bicycle) twoWheelersOwned.add("Bicycle")
        if(owned_electric_bike) twoWheelersOwned.add("ElectricBike")
        if(owned_motor_bike) twoWheelersOwned.add("MotorBike")


        val threeWheelersOwned = mutableListOf<String>()
        if(owned_e_rickshaw) threeWheelersOwned.add("eRickshaw")
        if(owned_auto_rickshaw) threeWheelersOwned.add("autoRickshaw")


        val otherVehicleOwned = mutableListOf<String>()
        if(owned_car) otherVehicleOwned.add("car")
        if(owned_commercial_vehicle) otherVehicleOwned.add("commercialVehicle")

        val itAssetsOwned = mutableListOf<String>()
        if(owned_laptop) itAssetsOwned.add("laptop")
        if(owned_smart_phone) itAssetsOwned.add("smartPhone")
        if(owned_pc) itAssetsOwned.add("pc")

        return mapOf(
                "two_wheelers_owned" to twoWheelersOwned,
                "three_wheelers_owned" to threeWheelersOwned,
                "other_vehicles_owned" to otherVehicleOwned,
                "it_assets_owned" to itAssetsOwned,
        )
    }


    override fun lastStateFormFound(): Boolean {
        formCompletionListener?.enableDisableNextButton(true)
        return false
    }

    override fun nextButtonActionFound(): Boolean {
        var assetsData = getAssetsDataForAnalytics()
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_ASSETS_SELECTED,assetsData))
        eventTracker.setUserProperty(assetsData)
        eventTracker.setProfileProperty(ProfilePropArgs("Assets Owned", assetsData))
        return false
    }

    override fun activeNextButton() {
        formCompletionListener?.enableDisableNextButton(true)
    }
    var formCompletionListener: OnFragmentFormCompletionListener? = null
    override fun setInterface(onFragmentFormCompletionListener: OnFragmentFormCompletionListener) {
        formCompletionListener = formCompletionListener?:onFragmentFormCompletionListener
    }

    fun getAssetLocalIcon(name: String) : Int{
        var icon = R.drawable.ic_driving_wheel
        var map = mapOf<String, Int>("Bicycle" to R.drawable.ic_bicycle,
            "E-Bike" to R.drawable.ic_electric_bike,
            "Motor Bike" to R.drawable.ic_motor_bike,
            "E-rickshaw" to R.drawable.ic_e_rickshaw,
            "Auto-rickshaw" to R.drawable.ic_auto_rickshaw,
            "Car" to R.drawable.ic_car,
            "Commercial" to R.drawable.ic_truck_commercial,
            "Laptop" to R.drawable.ic_laptop,
            "Smartphone" to R.drawable.ic_smartphone,
            "PC" to R.drawable.ic_computer,)

        if (map.containsKey(name)){
            icon = map.get(name)!!
        }
        return icon
    }

}