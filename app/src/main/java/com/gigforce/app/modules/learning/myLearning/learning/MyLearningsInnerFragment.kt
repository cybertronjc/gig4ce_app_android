package com.gigforce.app.modules.learning.myLearning.learning

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.learning.MainLearningFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_my_learning_inner.*
import java.util.ArrayList


class MyLearningsInnerFragment : Fragment() {

    private val viewModelProfile: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_my_learning_inner, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initActiveLearning()
        initPendingLearning()
        initCompletedLearning()
    }

    private fun initCompletedLearning() {
        var width: Int = 0
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        val datalist: ArrayList<MainLearningFragment.TitleSubtitleModel> =
            ArrayList<MainLearningFragment.TitleSubtitleModel>()
//        datalist.add(
//            MainLearningFragment.TitleSubtitleModel(
//                "Behavioral Skills Level1",
//                "Module 0 Of 0",
//                R.drawable.learning1
//            )
//        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<MainLearningFragment.TitleSubtitleModel> =
            RecyclerGenericAdapter<MainLearningFragment.TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    //navigate(R.id.learningVideoFragment)
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
        recyclerGenericAdapter.setLayout(R.layout.recycler_item_learning_inner)
        on_going_learning_recycler_view.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        on_going_learning_recycler_view.adapter = recyclerGenericAdapter

    }

    private fun initPendingLearning() {

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
//                "Module 0 Of 0",
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

//                            var img = getImageView(viewHolder, R.id.learning_img)
//                            img.setImageResource(obj?.imgIcon!!)
                        })
        recyclerGenericAdapter.list = datalist
        recyclerGenericAdapter.setLayout(R.layout.recycler_item_learning_inner)
        pending_learning_recycler_view.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        pending_learning_recycler_view.adapter = recyclerGenericAdapter
    }

    private fun initActiveLearning() {

        var width: Int = 0
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<MainLearningFragment.TitleSubtitleModel> =
                ArrayList<MainLearningFragment.TitleSubtitleModel>()
//        datalist.add(
//                MainLearningFragment.TitleSubtitleModel(
//                        "Behavioral Skills Level1",
//                        "Module 0 Of 0",
//                    R.drawable.learning1
//                )
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

//                            var img = getImageView(viewHolder, R.id.learning_img)
//                            img.setImageResource(obj?.imgIcon!!)
                        })
        recyclerGenericAdapter.list = datalist
        recyclerGenericAdapter.setLayout(R.layout.recycler_item_learning_inner)
        completed_learning_recycler_view.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        completed_learning_recycler_view.adapter = recyclerGenericAdapter
    }

}