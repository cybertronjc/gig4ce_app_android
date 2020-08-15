package com.gigforce.app.modules.preferences

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.core.setDarkStatusBarTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.preferences_fragment.*


class PreferencesFragment : BaseFragment() {
    companion object {
        fun newInstance() =
            PreferencesFragment()

        const val DAY_TIME = 0
        const val LOCATION = 1
        const val EARNING = 2
        const val TITLE_OTHER = 3
        const val LANGUAGE = 4
        const val TITLE_SIGNOUT = 5
        var storage = FirebaseStorage.getInstance()

    }

    private lateinit var viewModel: SharedPreferenceViewModel
    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<PreferencesScreenItem>
    val arrPrefrancesList: ArrayList<PreferencesScreenItem> = ArrayList<PreferencesScreenItem>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.preferences_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setDarkStatusBarTheme(false)
//        viewModel = ViewModelProvider(this).get(SharedPreferenceViewModel::class.java)
        viewModel = ViewModelProvider(this, ParameterizedSharedPreferenceVM(configDataModel)).get(
            SharedPreferenceViewModel::class.java
        )
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
    }

    private fun observeProfileData() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)

        })

    }

    override fun isConfigRequired(): Boolean {
        return true
    }
    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "")  {
            val profilePicRef: StorageReference =
                storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }else{
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    private fun listener() {
        imageView8.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        imageView9.setOnClickListener(View.OnClickListener { navigate(R.id.profileFragment) })
    }

    private fun initializeViews() {
        initializeRecyclerView()
        setPreferenecesList()
    }

    private fun initializeRecyclerView() {
        recyclerGenericAdapter =
            RecyclerGenericAdapter<PreferencesScreenItem>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<PreferencesScreenItem> { view, position, item ->
                    prefrencesItemSelect(
                        position
                    )
                },
                RecyclerGenericAdapter.ItemInterface<PreferencesScreenItem?> { obj, viewHolder, position ->
                    setPreferencesItems(obj, viewHolder, position)
                })!!
        recyclerGenericAdapter.list = arrPrefrancesList
        recyclerGenericAdapter.setLayout(R.layout.prefrences_item)

        prefrences_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        prefrences_rv.hasFixedSize()
        prefrences_rv.itemAnimator = DefaultItemAnimator()
        prefrences_rv.adapter = recyclerGenericAdapter
    }

    private fun setPreferenecesList() {
        arrPrefrancesList.clear()
        arrPrefrancesList.addAll(viewModel.getPrefrencesData())
        recyclerGenericAdapter.notifyDataSetChanged()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            if (preferenceData != null) {
                viewModel.setPreferenceDataModel(preferenceData)
                setPreferenecesList()
            }
            else if(configDataModel==null){
                    showToast("Config data not loaded!!")
                }
        })
    }


    private fun setPreferencesItems(
        obj: PreferencesScreenItem?,
        viewHolder: PFRecyclerViewAdapter<Any?>.ViewHolder,
        position: Int
    ) {
        var constraintView = getView(viewHolder, R.id.constraintLayout)

        var signOutView = getView(viewHolder, R.id.signOutLayout)
        var signOutTV = getTextView(viewHolder, R.id.signOutTitle)
        var signOutIV = getImageView(viewHolder, R.id.signOutIcon)

        var othersTV = getTextView(viewHolder, R.id.others_and_signout)
        var title = getTextView(viewHolder, R.id.item_title)
        var subTitle = getTextView(viewHolder, R.id.item_subtitle)
        var imageView = getImageView(viewHolder, R.id.item_icon)
        if (position == TITLE_OTHER) {
            signOutView.visibility = View.GONE
            visibleInvisibleMainItemView(constraintView, othersTV, false)
            setItemAsOther(othersTV, obj)
        }
        else if (position == TITLE_SIGNOUT) {
            signOutView.visibility = View.VISIBLE
            hideMainConstraintViewAndOthersViewInItemView(constraintView, othersTV)
            setItemAsSignOut(signOutTV, signOutIV, obj)
        }
        else {
            signOutView.visibility = View.GONE
            visibleInvisibleMainItemView(constraintView, othersTV, true)
            setItems(imageView, title, subTitle, obj)
        }
    }

    private fun setItems(
        imageView: ImageView,
        title: TextView,
        subTitle: TextView,
        obj: PreferencesScreenItem?
    ) {
        title.text = obj?.title
        subTitle.text = obj?.subtitle
        imageView.setImageResource(obj!!.icon)
    }

    private fun visibleInvisibleMainItemView(
        constraintView: View,
        otherAndSignout: TextView,
        isVisible: Boolean
    ) {
        constraintView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        otherAndSignout.visibility = if (!isVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun hideMainConstraintViewAndOthersViewInItemView(
        constraintView: View,
        otherAndSignout: TextView
    ) {
        constraintView.visibility = View.INVISIBLE
        otherAndSignout.visibility = View.INVISIBLE
    }


    private fun setItemAsSignOut(
        signOutTV: TextView,
        signOutIV: ImageView,
        obj: PreferencesScreenItem?
    ) {
        signOutTV.text = obj?.title
        signOutIV.setImageResource(obj!!.icon)
    }

    private fun setItemAsOther(otherAndSignout: TextView, obj: PreferencesScreenItem?) {
        otherAndSignout.text = obj?.title
    }

    private fun prefrencesItemSelect(position: Int) {
        if (position == DAY_TIME) navigate(R.id.dayTimeFragment)
        else if (position == TITLE_SIGNOUT) {
            logoutConfirmationDialog()
        } else if (position == EARNING) navigate(R.id.earningFragment)
        else if (position == LOCATION) navigate(R.id.locationFragment)
        else if (position == LANGUAGE) navigate(R.id.languagePreferenceFragment)
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
            "Are you sure?\n" +
                    "Signing out means missing out on gigs around you."
        val yesBtn = dialog.findViewById(R.id.yes) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            removeIntroComplete()
            popFragmentFromStack(R.id.settingFragment)
            dialog.dismiss()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

}