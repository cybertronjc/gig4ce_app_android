package com.gigforce.app.modules.learning.myLearning.journey

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.MainLearningFragment
import com.gigforce.app.modules.learning.models.Module
import com.gigforce.core.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_journey.*
import java.util.*


class MyJourneyInnerFragment : BaseFragment() {

    private val journeyViewModel : JourneyViewModel by viewModels ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_my_journey, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initViewModel()
        inflateSuggestedLearning()
    }

    private fun initViewModel() {

        journeyViewModel
            .allModules
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when(it){
                    Lce.Loading -> {}
                    is Lce.Content -> {
                        inflateOnGoingLearningItems(it.content)
                        inflateCompeltedLearningItem(it.content)
                    }
                    is Lce.Error -> {

                    }
                }
            })

        journeyViewModel.getModulesFromAllAssignedCourses()

    }


    private fun inflateOnGoingLearningItems(content: List<Module>) {

        var width: Int = 0
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 1.5).toInt()
        // model will change when integrated with DB

        val recyclerGenericAdapter: RecyclerGenericAdapter<Module> =
            RecyclerGenericAdapter<Module>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                },
                RecyclerGenericAdapter.ItemInterface<Module> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    getTextView(viewHolder, R.id.title_).text = obj.title

                    var img = getImageView(viewHolder, R.id.learning_img)
                    if (!obj!!.coverPicture.isNullOrBlank()) {
                        if (obj!!.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(requireContext())
                                .load(obj!!.coverPicture!!)
                                .placeholder(getCircularProgressDrawable())
                                .into(img)
                        } else {
                            FirebaseStorage.getInstance()
                                .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                                .child(obj!!.coverPicture!!)
                                .downloadUrl
                                .addOnSuccessListener { fileUri ->

                                    GlideApp.with(requireContext())
                                        .load(fileUri)
                                        .placeholder(getCircularProgressDrawable())
                                        .into(img)
                                }
                        }
                    }else {
                        img.setBackgroundColor(
                            ResourcesCompat.getColor(
                                requireContext().resources,
                                R.color.warm_grey,
                                null
                            )
                        )
                    }


                })
        recyclerGenericAdapter.list = content
        recyclerGenericAdapter.setLayout(R.layout.recycler_item_journey)
        on_going_learning_recycler_view.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        on_going_learning_recycler_view.adapter = recyclerGenericAdapter
    }

    private fun inflateCompeltedLearningItem(content: List<Module>) {

        var width: Int = 0
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 1.5).toInt()
        // model will change when integrated with DB

        val recyclerGenericAdapter: RecyclerGenericAdapter<Module> =
            RecyclerGenericAdapter<Module>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                },
                RecyclerGenericAdapter.ItemInterface<Module> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    getTextView(viewHolder, R.id.title_).text = obj.title

                    var img = getImageView(viewHolder, R.id.learning_img)
                    if (!obj!!.coverPicture.isNullOrBlank()) {
                        if (obj!!.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(requireContext())
                                .load(obj!!.coverPicture!!)
                                .placeholder(getCircularProgressDrawable())
                                .into(img)
                        } else {
                            FirebaseStorage.getInstance()
                                .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                                .child(obj!!.coverPicture!!)
                                .downloadUrl
                                .addOnSuccessListener { fileUri ->

                                    GlideApp.with(requireContext())
                                        .load(fileUri)
                                        .placeholder(getCircularProgressDrawable())
                                        .into(img)
                                }
                        }
                    }else {
                        img.setBackgroundColor(
                            ResourcesCompat.getColor(
                                requireContext().resources,
                                R.color.warm_grey,
                                null
                            )
                        )
                    }

                })
        recyclerGenericAdapter.list = content
        recyclerGenericAdapter.setLayout(R.layout.recycler_item_journey)
        completed_learning_recycler_view.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        completed_learning_recycler_view.adapter = recyclerGenericAdapter
        
    }

    private fun inflateSuggestedLearning() {
        var width: Int = 0
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<MainLearningFragment.TitleSubtitleModel> =
            ArrayList<MainLearningFragment.TitleSubtitleModel>()
//        datalist.add(
//            MainLearningFragment.TitleSubtitleModel(
//                "Behavioral Skills Level1",
//                "Behavioral Skills Level1",
//                R.drawable.learning1
//            )
//        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<MainLearningFragment.TitleSubtitleModel> =
            RecyclerGenericAdapter<MainLearningFragment.TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                   // navigate(R.id.learningVideoFragment)
                },
                RecyclerGenericAdapter.ItemInterface<MainLearningFragment.TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title_)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.title)
                    subtitle.text = obj?.subtitle

//                    var img = getImageView(viewHolder, R.id.learning_img)
//                    img.setImageResource(obj?.imgIcon!!)
                })
        recyclerGenericAdapter.list = datalist
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        suggested_learning_recycler_view.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        suggested_learning_recycler_view.adapter = recyclerGenericAdapter

    }

}