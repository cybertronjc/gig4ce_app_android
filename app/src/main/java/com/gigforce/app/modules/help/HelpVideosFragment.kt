package com.gigforce.app.modules.help

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import kotlinx.android.synthetic.main.fragment_help_video.*


class HelpVideosFragment : BaseFragment() {

    private val viewModel: HelpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_help_video, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initViewModel() {
        viewModel.helpVideos
            .observe(viewLifecycleOwner, Observer {
                setHelpVideosOnView(it)
            })

        viewModel.getAllHelpVideos()
    }

    private fun setHelpVideosOnView(helpVideos: List<HelpVideo>?) {

        val recyclerGenericAdapter: RecyclerGenericAdapter<HelpVideo> =
            RecyclerGenericAdapter<HelpVideo>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    val id = (item as HelpVideo).videoYoutubeId
                    val appIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=$id")
                    )
                    try {
                        requireContext().startActivity(appIntent)
                    } catch (ex: ActivityNotFoundException) {
                        requireContext().startActivity(webIntent)
                    }
                },
                RecyclerGenericAdapter.ItemInterface<HelpVideo?> { obj, viewHolder, position ->

                    var iconIV = getImageView(viewHolder, R.id.help_first_card_img)
                    Glide.with(requireContext()).load(obj?.getThumbNailUrl()).into(iconIV)

                    var titleTV = getTextView(viewHolder, R.id.titleTV)
                    titleTV.text = obj?.videoTitle

                    var timeTV = getTextView(viewHolder, R.id.time_text)
                    timeTV.text = if (obj!!.videoLength >= 60) {
                        val minutes = obj.videoLength / 60
                        val secs = obj.videoLength % 60
                        "$minutes:$secs"
                    } else {
                        "00:${obj.videoLength}"
                    }


//                    var img = getImageView(viewHolder, R.id.learning_img)
//                    img.setImageResource(obj?.imgIcon!!)
                })
        recyclerGenericAdapter.setList(helpVideos)
        recyclerGenericAdapter.setLayout(R.layout.item_help_video)
        helpVideoRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        helpVideoRV.adapter = recyclerGenericAdapter
    }


}