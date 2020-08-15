package com.gigforce.app.modules.learning

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
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
import kotlinx.android.synthetic.main.fragment_main_learning.*
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

        dummLayout1.videoTitleTV.text = getString(R.string.achieve_retail_goal)
        dummLayout1.videoDescTV.text = getString(R.string.industry_based)
        dummLayout1.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning))

        dummLayout2.videoTitleTV.text = getString(R.string.apply_driving_license)
        dummLayout2.videoDescTV.text = getString(R.string.role_based)
        dummLayout2.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning2))

        dummLayout3.videoTitleTV.text = getString(R.string.achieve_retail_goal)
        dummLayout3.videoDescTV.text = getString(R.string.industry_based)
        dummLayout3.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning1))

        initializeExploreByIndustry()
        mostPopularLearning()
        listener()
        observerProfile()
    }

    private fun observerProfile() {
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)
        })

    }

    private fun displayImage(profileImg: String) {
        if (profileImg != null && !profileImg.equals("")) {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image_main)
        }
    }

    private fun listener() {
        chat_icon_iv.setOnClickListener {
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
                getString(R.string.delivery),
                getString(R.string.maintaining_safety), R.drawable.man_with_mask
            )
        )

        datalist.add(
            TitleSubtitleModel(
                getString(R.string.cook),
                getString(R.string.cook_salt_meals),
                R.drawable.cook_
            )
        )

        datalist.add(
            TitleSubtitleModel(
                getString(R.string.basrista),
                getString(R.string.prepare_coffee), R.drawable.barista
            )
        )

        datalist.add(
            TitleSubtitleModel(
                getString(R.string.housekeeping),
                getString(R.string.clean_floors),
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

                    var img = getImageView(viewHolder, R.id.img)
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
                getString(R.string.retail_sales_executive),
                getString(R.string.demonstrate_products), R.drawable.learning2
            )
        )
        datalist.add(
            TitleSubtitleModel(
                getString(R.string.driver),
                getString(R.string.accept_ride),
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

                    var img = getImageView(viewHolder, R.id.learning_img)
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

    class TitleSubtitleModel(var title: String, var subtitle: String, var imgIcon: Int = 0) {

    }
}