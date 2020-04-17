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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.utils.setDarkStatusBarTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.setting_fragment.*


class SettingFragment : BaseFragment() {
    companion object {
        fun newInstance() =
            SettingFragment()
        const val DAY_TIME = 2;
        const val TITLE_OTHER = 5;
        const val TITLE_SIGNOUT = 8;
    }

    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.setting_fragment, inflater, container)
    }
    private fun showConfirmationDialog() {
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
            dialog?.dismiss()
        }
        noBtn.setOnClickListener { dialog .dismiss() }
        dialog?.show()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setDarkStatusBarTheme(false)
        viewModel = ViewModelProviders.of(this).get(SettingViewModel::class.java)
        val arrPrefrancesList = viewModel.getPrefrencesData()
        val recyclerGenericAdapter: RecyclerGenericAdapter<PrefrencesItem> =
            RecyclerGenericAdapter<PrefrencesItem>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<PrefrencesItem> { view, position, item -> prefrencesItemSelect(position) },
                RecyclerGenericAdapter.ItemInterface <PrefrencesItem?> { obj, viewHolder,position ->
                    setPreferencesItems(obj,viewHolder,position)
                })!!
        recyclerGenericAdapter.setList(arrPrefrancesList)
        recyclerGenericAdapter.setLayout(R.layout.prefrences_item)
        prefrences_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        prefrences_rv.adapter = recyclerGenericAdapter
    }

    private fun setPreferencesItems(
        obj: PrefrencesItem?,
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

    private fun setItems(imageView:ImageView,title: TextView, subTitle: TextView, obj: PrefrencesItem?) {
        title.text = obj?.title
        subTitle.text = obj?.subtitle
        imageView.setImageResource(obj!!.icon)
    }

    private fun visibleInvisibleMainItemView(constraintView: View, otherAndSignout: TextView,isVisible:Boolean){
        constraintView.visibility = if(isVisible) View.VISIBLE else View.INVISIBLE
        otherAndSignout.visibility = if(!isVisible) View.VISIBLE else View.INVISIBLE
    }
    private fun setItemAsSignOut(otherAndSignout: TextView,obj: PrefrencesItem?) {
        val spannableString1 = SpannableString(obj?.title)
        spannableString1.setSpan(UnderlineSpan(),0,obj?.title!!.length,0)
        otherAndSignout.text = spannableString1
    }

    private fun setItemAsOther(otherAndSignout: TextView,obj: PrefrencesItem?) {
        otherAndSignout.text = obj?.title
    }

    private fun prefrencesItemSelect(position: Int) {
        if(position== DAY_TIME) navigate(R.id.dayTimeFragment)
        if(position== TITLE_SIGNOUT){showConfirmationDialog()}
    }

}