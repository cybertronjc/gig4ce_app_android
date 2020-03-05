package com.gigforce.app.modules.onboarding

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gigforce.app.R
import com.gigforce.app.modules.onboarding.utils.DepthPageTransformer
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.dp
import kotlinx.android.synthetic.main.fragment_video_resume.*

class VideoResumeFragment:Fragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {

            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val window: Window = activity!!.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            //window.statusBarColor() to default ie transparent
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_video_resume, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            this.setupViewPager()
        }

        fun setupViewPager(){
            this.viewpager.adapter = VideoResumeViewPagerAdapter(this.viewpager, object: OnVideoResumeCompleted(){

                override fun invoke() {
                    this@VideoResumeFragment.findNavController().navigate(getResourceToNavigateTo())
                }
            })
            this.viewpager.setPageTransformer(DepthPageTransformer())
            this.viewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateDotPresenter()
                }
            })
        }

        fun updateDotPresenter(){
            this.dots_presenter.removeAllViews()
            this.dots_presenter.addView(getDot(this.viewpager.currentItem == 0))
            this.dots_presenter.addView(getDot(this.viewpager.currentItem == 1))
            this.dots_presenter.addView(getDot(this.viewpager.currentItem == 2))
        }

        private fun getDot(selected:Boolean = false): ImageView {
            val image: ImageView = ImageView(context)

            val params = LinearLayout.LayoutParams(14.dp,14.dp)
            params.marginEnd = 6.dp
            image.layoutParams = params

            if(selected)
                image.setImageResource(R.drawable.dottab_indicator_selected)
            else
                image.setImageResource(R.drawable.dottab_indicator_default)
            return image
        }
    }

    class VideoResumeViewPagerAdapter(val viewpager: ViewPager2,
                                      val onVideoResumeCompleted: OnVideoResumeCompleted): RecyclerView.Adapter<VideoResumeViewPagerAdapter.ViewHolder>(){

        class ViewHolder(view: View,
                         val viewpager: ViewPager2,
                         val onVideoResumeCompleted: OnVideoResumeCompleted): RecyclerView.ViewHolder(view) {

            var mainArtImageView: ImageView

            var nextButton: Button

            var currentPosition:Int = -1

            init {
                mainArtImageView = this.itemView.findViewById<ImageView>(R.id.iv_main_art)

                nextButton = this.itemView.findViewById<Button>(R.id.btn_next)

                nextButton.setOnClickListener {
                    if(currentPosition < 2)
                        viewpager.setCurrentItem(currentPosition+1, true)
                    else if (currentPosition == 2) {
                        // on Final CA Executed
                        onVideoResumeCompleted.invoke()
                    }
                }
            }

            fun Bind(position: Int){
                currentPosition = position
                if(position == 0) {

                    GlideApp.with(itemView)
                        .load(R.drawable.ic_intro_slides1_mainart)
                        .into(mainArtImageView)

                    nextButton.setText("next")

                }else if(position == 1){
                    GlideApp.with(itemView)
                        .load(R.drawable.ic_intro_slides2_mainart)
                        .into(mainArtImageView)


                    nextButton.setText("next")

                }else if(position == 2) {
                    GlideApp.with(itemView)
                        .load(R.drawable.ic_intro_slides3_mainart)
                        .into(mainArtImageView)

                    //titleTextView.setText(R.string.intro_slide3_title)
                    //subTitleTextView.setText(R.string.intro_slide3_subtitle)
                    nextButton.setText("Get Started")
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_video_resume_slide, parent, false)
            return ViewHolder(view, viewpager, onVideoResumeCompleted)
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.Bind(position)
        }
}