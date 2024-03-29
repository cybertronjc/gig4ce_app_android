package com.gigforce.user_preferences

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.common_ui.viewmodels.userpreferences.SharedPreferenceViewModel
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.analytics.AuthEvents
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.setDarkStatusBarTheme
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.preferences_fragment.*
import javax.inject.Inject


@AndroidEntryPoint
class PreferencesFragment : Fragment() {
    companion object {
        const val DAY_TIME = 0
        const val LOCATION = 1
        const val EARNING = 2
        const val TITLE_OTHER = 3
        const val LANGUAGE = 4
        const val TITLE_SIGNOUT = 5
        var storage = FirebaseStorage.getInstance()
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private lateinit var viewModel: SharedPreferenceViewModel

    //    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<PreferencesScreenItem>
    val arrPrefrancesList: ArrayList<PreferencesScreenItem> = ArrayList<PreferencesScreenItem>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.preferences_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setDarkStatusBarTheme(false)
//        viewModel = ViewModelProvider(this).get(SharedPreferenceViewModel::class.java)
//        viewModel = ViewModelProvider(this, ParameterizedSharedPreferenceVM(configDataModel)).get(
//            SharedPreferenceViewModel::class.java
//        )
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        getIntentData(savedInstanceState)
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }

    private fun observeProfileData() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            viewModel.setProfileDataModel(profile)
            displayImage(profile.profileAvatarName)
            setPreferenecesList()
        })
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            if (preferenceData != null) {
                viewModel.setPreferenceDataModel(preferenceData)
                setPreferenecesList()
            }
//            else if (configDataModel == null) {
//                showToast(getString(R.string.config_data_not_loaded))
//            }
        })

        viewModel.configLiveDataModel.observe(
            viewLifecycleOwner,
            Observer { configDataModel1 ->
                viewModel.setConfiguration(configDataModel1)
                viewModel.getAllData()
            })
        viewModel.getConfiguration()
    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        } else {
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    private fun listener() {
        appBar.setBackButtonListener { activity?.onBackPressed() }

        imageView8.setOnClickListener { activity?.onBackPressed() }
        imageView9.setOnClickListener { navigation.navigateTo("profile")/*navigate(R.id.profileFragment)*/ }
    }

    private fun initializeViews() {
        if (title.isNotBlank())
            appBar.setAppBarTitle(title)
        initializeRecyclerView()
        setPreferenecesList()
    }

    var preferencesAdapter: PreferencesFragmentAdapter? = null
    private fun initializeRecyclerView() {
//        recyclerGenericAdapter =
//            RecyclerGenericAdapter<PreferencesScreenItem>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<PreferencesScreenItem> { view, position, item ->
//                    prefrencesItemSelect(
//                        position
//                    )
//                },
//                RecyclerGenericAdapter.ItemInterface<PreferencesScreenItem?> { obj, viewHolder, position ->
//                    setPreferencesItems(obj, viewHolder, position)
//                })
//        recyclerGenericAdapter.list = arrPrefrancesList
//        recyclerGenericAdapter.setLayout(R.layout.prefrences_item)

        preferencesAdapter = PreferencesFragmentAdapter()
        preferencesAdapter?.data = arrPrefrancesList
        preferencesAdapter?.setItemClickListener(object :
            PreferencesFragmentAdapter.ItemClickListener {
            override fun onItemClickListener(position: Int) {
                prefrencesItemSelect(
                    position
                )
            }

        })
        prefrences_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        prefrences_rv.hasFixedSize()
        prefrences_rv.itemAnimator = DefaultItemAnimator()
        prefrences_rv.adapter = preferencesAdapter
    }

    private fun setPreferenecesList() {
        arrPrefrancesList.clear()
        arrPrefrancesList.addAll(getPrefrencesData())
//        recyclerGenericAdapter.notifyDataSetChanged()
        preferencesAdapter?.notifyDataSetChanged()

    }

    fun getPrefrencesData(): ArrayList<PreferencesScreenItem> {
        val prefrencesItems = ArrayList<PreferencesScreenItem>()
        // prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_link_black,"Category",""))
        // prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_group_black,"Roles","At atm"))
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_clock_black,
                getString(R.string.day_and_time_pref),
                viewModel.getDateTimeSubtitle(requireContext())
            )
        )
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_location_pin_black,
                getString(R.string.location_pref),
                viewModel.getLocation(requireContext())
            )
        )
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_credit_card_black,
                getString(R.string.earning_pref),
                viewModel.getEarning()
            )
        )
        prefrencesItems.add(PreferencesScreenItem(0, getString(R.string.others_pref), ""))
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_language_black,
                getString(R.string.app_language_pref),
                sharedPreAndCommonUtilInterface.getAppLanguageName()!!
            )
        )
        // prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_notifications_on_black,"Notification",""))
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_power_button_black,
                getString(R.string.sign_out_pref),
                ""
            )
        )
        return prefrencesItems
    }


    private fun prefrencesItemSelect(position: Int) {
        if (position == DAY_TIME) navigation.navigateTo("preferences/dayTimeFragment")//navigate(R.id.dayTimeFragment)
        else if (position == TITLE_SIGNOUT) {
            logoutConfirmationDialog()
        } else if (position == EARNING) navigation.navigateTo("preferences/earningFragment")//navigate(R.id.earningFragment)
        else if (position == LOCATION) navigation.navigateTo("preferences/locationFragment")//navigate(R.id.locationFragment)
        else if (position == LANGUAGE) navigation.navigateTo("preferences/languagePreferenceFragment")//navigate(R.id.languagePreferenceFragment)
    }

    private fun logoutConfirmationDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.confirmation_custom_alert_type1)
//        dialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//        val titleDialog = dialog?.findViewById(R.id.dialog_title) as TextView
//        titleDialog.visibility = View.VISIBLE
//        titleDialog?.text = "Missing out on gigs?"
        val title = dialog?.findViewById(R.id.title) as TextView
        title.text =
            getString(R.string.are_you_sure_pref) + "\n" + getString(R.string.signing_out_means_missing_out_on_gigs_pref)
        val yesBtn = dialog.findViewById(R.id.yes) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {

            eventTracker.pushEvent(
                TrackingEventArgs(
                    eventName = AuthEvents.SIGN_OUT_SUCCESS,
                    props = null
                )
            )

            unregisterCurrentFirebaseToken()
            eventTracker.logoutUserFromAnalytics()
            sharedPreAndCommonUtilInterface.removeIntroComplete()
            dialog.dismiss()
        }

        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun unregisterCurrentFirebaseToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            val token = it.token

            FirebaseFirestore
                .getInstance()
                .collection("firebase_tokens")
                .document(token)
                .delete()
                .addOnSuccessListener {
                    Log.d("S", "Token deleted")
                    FirebaseAuth.getInstance().signOut()
                }.addOnFailureListener {
                    Log.d("S", "Token deleted")
                    FirebaseAuth.getInstance().signOut()
                }
        }.addOnFailureListener {
            FirebaseAuth.getInstance().signOut()
        }
    }

}