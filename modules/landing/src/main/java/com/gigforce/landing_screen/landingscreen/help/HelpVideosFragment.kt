package com.gigforce.landing_screen.landingscreen.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.core.extensions.gone
import com.gigforce.landing_screen.R
import kotlinx.android.synthetic.main.fragment_help_video.*
import com.jaeger.library.StatusBarUtil

class HelpVideosFragment : Fragment() {

    private val viewModel: HelpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_help_video, container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
        initView()
        initViewModel()
    }

    private fun listener() {
        help_call.setOnClickListener{
            viewModel.helpAndSupportMobileNumber.value?.number?.let {
                if(it.isNotBlank()){
                    callManager(it)

                }
            }
        }
    }
    fun callManager(number: String?) {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", number, null)
        )
        context?.startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(
                resources,
                R.color.lipstick_two,
                null
        ))
    }

    private fun initView() {
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        shimmerFrameLayout.startShimmer()
    }

    private fun initViewModel() {
        viewModel.helpVideos
            .observe(viewLifecycleOwner, Observer {

                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.gone()
                setHelpVideosOnView(it)
            })

        viewModel.getAllHelpVideos()
    }

    private fun setHelpVideosOnView(helpVideos: ArrayList<HelpVideo>?) {

//        val recyclerGenericAdapter: RecyclerGenericAdapter<HelpVideo> =
//            RecyclerGenericAdapter<HelpVideo>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    val id = (item as HelpVideo).videoYoutubeId
//                    val appIntent =
//                        Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
//                    val webIntent = Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse("http://www.youtube.com/watch?v=$id")
//                    )
//                    try {
//                        requireContext().startActivity(appIntent)
//                    } catch (ex: ActivityNotFoundException) {
//                        requireContext().startActivity(webIntent)
//                    }
//                },
//                RecyclerGenericAdapter.ItemInterface<HelpVideo> { obj, viewHolder, position ->
//
//                    var iconIV = getImageView(viewHolder, R.id.help_first_card_img)
//                    Glide.with(requireContext()).load(obj?.getThumbNailUrl()).placeholder(getCircularProgressDrawable()).into(iconIV)
//
//                    var titleTV = getTextView(viewHolder, R.id.titleTV)
//                    titleTV.text = obj?.videoTitle
//
//                    var timeTV = getTextView(viewHolder, R.id.time_text)
//                    timeTV.text = if (obj!!.videoLength >= 60) {
//                        val minutes = obj.videoLength / 60
//                        val secs = obj.videoLength % 60
//                        "$minutes:$secs"
//                    } else {
//                        "00:${obj.videoLength}"
//                    }
//
//
////                    var img = getImageView(viewHolder, R.id.learning_img)
////                    img.setImageResource(obj?.imgIcon!!)
//                })
//        recyclerGenericAdapter.setList(helpVideos)
//        recyclerGenericAdapter.setLayout(R.layout.item_help_video)
        context?.let {
            var helpVideoAdapter = HelpVideoAdapter(it)
            helpVideoAdapter.setData(helpVideos as ArrayList<HelpVideo>)
            helpVideoRV.layoutManager = LinearLayoutManager(
                    activity?.applicationContext,
                    LinearLayoutManager.VERTICAL,
                    false
            )
            helpVideoRV.adapter = helpVideoAdapter
        }

    }


}