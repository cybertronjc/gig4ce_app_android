package com.gigforce.app.modules.onboarding

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R
import com.gigforce.app.R.*
import com.gigforce.app.modules.onboarding.utils.DepthPageTransformer
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.dp
import kotlinx.android.synthetic.main.fragment_video_resume.*
import kotlinx.android.synthetic.main.fragment_video_resume.view.*
import java.io.File


class VideoResumeFragment:Fragment() {

    private val videoView: VideoView? = null
    private val mediaPlayer: MediaPlayer? = null

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
            return inflater.inflate(layout.fragment_video_resume, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

//            if(viewpager.currentItem <=2 )
//            {
//                Toast.makeText(context, "counter:>>>>>>>>>>>>>> "+viewpager.currentItem.toString(), Toast.LENGTH_SHORT).show()
//                button_video.visibility = View.INVISIBLE
//            }
            if(button_video?.visibility==View.VISIBLE){
                button_video?.setOnClickListener {
                    dispatchTakeVideoIntent()
                }
            }
/*
            fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
                if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
                    val videoUri: Uri? = intent.data
                    var videoView = view.findViewById(R.id.videoView) as VideoView
                    videoView.setVideoURI(videoUri)

                    var p = videoUri.toString()//getRealPathFromURI(videoUri)
                    val from = File(p)
                    val onlyPath: String = from.getParentFile().getAbsolutePath()
                    val newPath = R.raw.hello
                    //    onlyPath + "hi.mp4"
                    var to = File(newPath.toString())
                    videoView.setVideoPath(to.absolutePath)
                    videoView.start()
                }
            }
*/
            this.setupViewPager()
/*
            play_button.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW)
                onActivityResult(REQUEST_VIDEO_CAPTURE, RESULT_OK, intent )
            }
*/
//            button_video.setOnClickListener (object : View.OnClickListener {
//                val REQUEST_TAKE_GALLERY_VIDEO = 1;
//                    override fun onClick(v: View?) {
//                        val intent = Intent()
//                        intent.type = "video/*"
//                        intent.action = Intent.ACTION_GET_CONTENT
//                        startActivityForResult(
//                            Intent.createChooser(intent, "Select Video"),
//                            REQUEST_TAKE_GALLERY_VIDEO
//                        )
//                    }
//                })
        }


    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            //takeVideoIntent.resolveActivity()?.also {
                startActivityForResult(takeVideoIntent, Companion.REQUEST_VIDEO_CAPTURE)
            //}
        }
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
                image.setImageResource(drawable.dottab_indicator_selected)
            else
                image.setImageResource(drawable.dottab_indicator_default)
            return image
        }

    companion object {
        const val REQUEST_VIDEO_CAPTURE = 1
    }
}

    class VideoResumeViewPagerAdapter(val viewpager: ViewPager2,
                                      val onVideoResumeCompleted: OnVideoResumeCompleted): RecyclerView.Adapter<VideoResumeViewPagerAdapter.ViewHolder>(){

        class ViewHolder(view: View,
                         val viewpager: ViewPager2,
                         val onVideoResumeCompleted: OnVideoResumeCompleted): RecyclerView.ViewHolder(view) {

            var mainArtImageView: ImageView
            //var mainArtVideoView: VideoView

            //var nextButton: Button

            var currentPosition:Int = -1

            init {
                mainArtImageView = this.itemView.findViewById<ImageView>(id.iv_main_art_video)
            }

            fun Bind(position: Int){

                //currentPosition = position


                //Toast.makeText(holder.viewpager.rootView.context, "counter:>>>>>>>>>>>>>> "+viewpager.currentItem.toString(), Toast.LENGTH_SHORT).show()



                if(position == 0) {
                    GlideApp.with(itemView)
                        .load(drawable.ic_intro_slides1_mainart)
                        .into(mainArtImageView)
                    //viewpager.button_video?.visibility=View.INVISIBLE
                    viewpager.rootView.button_video?.visibility=View.INVISIBLE;
                    //nextButton.setText("next")

                }else if(position == 1){
                    GlideApp.with(itemView)
                        .load(drawable.ic_intro_slides2_mainart)
                        .into(mainArtImageView)
                    //nextButton.setText("next")
                    Toast.makeText(viewpager.rootView.context, "counter:>>asdfas>>>>>> "+position.toString(), Toast.LENGTH_SHORT).show()
                    //viewpager.button_video?.visibility=View.INVISIBLE
                    viewpager.rootView.button_video?.visibility=View.INVISIBLE;
                }else if(position == 2) {
                    GlideApp.with(itemView)
                        .load(drawable.ic_intro_slides3_mainart)
                        .into(mainArtImageView)
                    Toast.makeText(viewpager.rootView.context, "counter:>>asdfas>>>>>> "+position.toString(), Toast.LENGTH_SHORT).show()
                    viewpager.rootView.button_video?.visibility=View.VISIBLE;

//                    mainArtImageView.setOnClickListener {
//
//                    }
                        //.load(R.raw.hello)
                        //.into(mainArtVideoView)
                    //titleTextView.setText(R.string.intro_slide3_title)
                    //subTitleTextView.setText(R.string.intro_slide3_subtitle)
                    //nextButton.setText("Get Started")
                }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(layout.layout_video_resume_slide, parent, false)
            return ViewHolder(view, viewpager, onVideoResumeCompleted)
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.Bind(position)
        }
}