package com.gigforce.app.modules.learning

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.calendar_home_screen.*
import kotlinx.android.synthetic.main.fragment_main_learning.*
import kotlinx.android.synthetic.main.fragment_main_learning.chat_icon_iv
import kotlinx.android.synthetic.main.fragment_main_learning_recent_video_item.view.*
import java.util.*


class MainLearningFragment : BaseFragment() {
    lateinit var viewModelProfile: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_main_learning, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelProfile = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        dummLayout1.videoTitleTV.text = "How to acheive your retail goal market?"
        dummLayout1.videoDescTV.text = "Industry Based"
        dummLayout1.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning))

        dummLayout2.videoTitleTV.text = "How to apply for driving license?"
        dummLayout2.videoDescTV.text = "Role Based"
        dummLayout2.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning2))

        dummLayout3.videoTitleTV.text = "How to acheive your retail goal market?"
        dummLayout3.videoDescTV.text = "Industry Based"
        dummLayout3.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning1))

        initializeExploreByIndustry()
        mostPopularLearning()
        listener()
        observerProfile()
    }

    private fun observerProfile() {
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile?.profileAvatarName!!)
        })

    }
    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image_main)
        }else{
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image_main)
        }
    }
    private fun listener() {
        chat_icon_iv.setOnClickListener{
            navigate(R.id.contactScreenFragment)
        }
    }

    private fun mostPopularLearning() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 2.8) * 1).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()

        datalist.add(
            TitleSubtitleModel(
                "Delivery",
                "Maintaining hygiene and safety at gig", R.drawable.man_with_mask
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Cook",
                "How to cook low salt meals",
                R.drawable.cook_
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Barista",
                "How to prepare coffee?", R.drawable.barista
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Housekeeping",
                "Selecting the right reagent to clean different floors?",
                R.drawable.housekeeping
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.learningVideoFragment)
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.subtitle)
                    subtitle.text = obj?.subtitle

                    var img = getImageView(viewHolder,R.id.img)
                    img.setImageResource(obj?.imgIcon!!)
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.most_popular_item)
        mostPopularLearningsRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mostPopularLearningsRV.adapter = recyclerGenericAdapter
    }

    var width: Int = 0
    private fun initializeExploreByIndustry() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()
        datalist.add(
            TitleSubtitleModel(
                "Retail Sales Executive",
                "Demonstrate products to customers", R.drawable.learning2
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Driver",
                "How to accept a ride",
                R.drawable.driver_img
            )
        )
        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.learningVideoFragment)
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title_)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.title)
                    subtitle.text = obj?.subtitle

                    var img = getImageView(viewHolder,R.id.learning_img)
                    img.setImageResource(obj?.imgIcon!!)
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        searchSuggestionBasedVideosRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        searchSuggestionBasedVideosRV.adapter = recyclerGenericAdapter
    }

    class TitleSubtitleModel(var title: String, var subtitle: String,var imgIcon:Int=0) {

    }
}