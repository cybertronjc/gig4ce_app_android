package com.gigforce.app.modules.preferences

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.setDarkStatusBarTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.preferences_fragment.*


class PreferencesFragment : BaseFragment() {
    companion object {
        fun newInstance() =
            PreferencesFragment()
        const val DAY_TIME = 2;
        const val LOCATION = 3;
        const val TITLE_OTHER = 5;
        const val TITLE_SIGNOUT = 8;
        var storage = FirebaseStorage.getInstance()

    }

    private lateinit var viewModel: SharedPreferenceViewModel
    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<PreferencesScreenItem>
    val arrPrefrancesList : ArrayList<PreferencesScreenItem> = ArrayList<PreferencesScreenItem> ()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.preferences_fragment, inflater, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setDarkStatusBarTheme(false)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
    }

    private fun observeProfileData() {
        viewModel.userProfileData.observe(this, Observer { profile ->
            displayImage(profile.profileAvatarName)
        })

    }

    private fun displayImage(profileImg:String) {
        if(profileImg!=null && !profileImg.equals("")) {
            val profilePicRef: StorageReference =
                storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.context!!)
                .load(profilePicRef)
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
                PFRecyclerViewAdapter.OnViewHolderClick<PreferencesScreenItem> { view, position, item -> prefrencesItemSelect(position) },
                RecyclerGenericAdapter.ItemInterface <PreferencesScreenItem?> { obj, viewHolder, position ->
                    setPreferencesItems(obj,viewHolder,position)
                })!!
        recyclerGenericAdapter.setList(arrPrefrancesList)
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
            if(preferenceData!=null) {
                viewModel.setPreferenceDataModel(preferenceData)
                setPreferenecesList()
            }
        })
    }



    private fun setPreferencesItems(
        obj: PreferencesScreenItem?,
        viewHolder: PFRecyclerViewAdapter<Any?>.ViewHolder,
        position: Int
    ) {
        var constraintView = getView(viewHolder,R.id.constraintLayout)
        var otherAndSignout = getTextView(viewHolder,R.id.others_and_signout)
        var title = getTextView(viewHolder,R.id.item_title)
        var subTitle = getTextView(viewHolder,R.id.item_subtitle)
        var imageView = getImageView(viewHolder,R.id.item_icon)
        if(position== TITLE_OTHER){
            visibleInvisibleMainItemView(constraintView,otherAndSignout,false)
            setItemAsOther(otherAndSignout,obj);
        }
        else if(position== TITLE_SIGNOUT)
        {
            visibleInvisibleMainItemView(constraintView,otherAndSignout,false)
            setItemAsSignOut(otherAndSignout,obj)
        }
        else{
            visibleInvisibleMainItemView(constraintView,otherAndSignout,true)
            setItems(imageView,title,subTitle,obj)
        }
    }

    private fun setItems(imageView:ImageView,title: TextView, subTitle: TextView, obj: PreferencesScreenItem?) {
        title.text = obj?.title
        subTitle.text = obj?.subtitle
        imageView.setImageResource(obj!!.icon)
    }

    private fun visibleInvisibleMainItemView(constraintView: View, otherAndSignout: TextView,isVisible:Boolean){
        constraintView.visibility = if(isVisible) View.VISIBLE else View.INVISIBLE
        otherAndSignout.visibility = if(!isVisible) View.VISIBLE else View.INVISIBLE
    }
    private fun setItemAsSignOut(otherAndSignout: TextView,obj: PreferencesScreenItem?) {
        val spannableString1 = SpannableString(obj?.title)
        spannableString1.setSpan(UnderlineSpan(),0,obj?.title!!.length,0)
        otherAndSignout.text = spannableString1
    }

    private fun setItemAsOther(otherAndSignout: TextView,obj: PreferencesScreenItem?) {
        otherAndSignout.text = obj?.title
    }

    private fun prefrencesItemSelect(position: Int) {
        if(position== DAY_TIME) navigate(R.id.dayTimeFragment)
        if(position== TITLE_SIGNOUT){logoutConfirmationDialog()}
        if(position== LOCATION) navigate(R.id.locationFragment)
    }

    private fun logoutConfirmationDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.signout_custom_alert)
        val titleDialog = dialog?.findViewById(R.id.title) as TextView
        titleDialog.text = "Do you really want to sign out?"
        val yesBtn = dialog?.findViewById(R.id.yes) as TextView
        val noBtn = dialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            removeSavedShareData(AppConstants.INTRO_COMPLETE)
            popFragmentFromStack(R.id.settingFragment)
            dialog?.dismiss()
        }
        noBtn.setOnClickListener { dialog .dismiss() }
        dialog?.show()
    }

}