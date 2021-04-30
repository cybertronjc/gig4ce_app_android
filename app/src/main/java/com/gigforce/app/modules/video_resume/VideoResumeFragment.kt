package com.gigforce.app.modules.video_resume

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.R.*
import com.gigforce.app.modules.onboarding.utils.DepthPageTransformer
import com.gigforce.app.core.dp
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_video_resume.*
import kotlinx.android.synthetic.main.fragment_video_resume.view.*
import java.io.File
import java.util.*

/*
To do:

Video resume package? separate
Video capture layout part of the video slides screens
Video view layout


 */

class VideoResumeFragment:Fragment() {

    private val videoView: VideoView? = null
    private val mediaPlayer: MediaPlayer? = null
    private lateinit var storage: FirebaseStorage

    private var mChronometer: Chronometer? = null

    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101

    private fun makeRequest(req:String) {
        this.activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(req),
                RECORD_REQUEST_CODE)
        }
    }

    private fun setupPermissions(req: String) {
        val permission = this.context?.let {
            ContextCompat.checkSelfPermission(
                it,
                req)
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission to record denied")
            makeRequest(req)

            mChronometer?.setBase(SystemClock.elapsedRealtime());
            mChronometer?.setVisibility(View.VISIBLE);
            mChronometer?.start();
        }
    }

    fun uploadVideotoFB(videoRef: StorageReference, localPath:Uri) {
        // File or Blob
        //var file = Uri.fromFile(File(localPath))

        // Create the file metadata
        var metadata: StorageMetadata? = null;
//            StorageMetadata() {
//            contentType = "video/mp4"
//        }

        // Upload file and metadata to the path 'images/mountains.jpg'
        val uploadTask = metadata?.let { videoRef.putFile(localPath, it) }

        // Listen for state changes, errors, and completion of the upload.
        uploadTask?.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            println("Upload is $progress% done")
        }?.addOnPausedListener {
            println("Upload is paused")
        }?.addOnFailureListener {
            // Handle unsuccessful uploads
        }?.addOnSuccessListener {
            // Handle successful uploads on complete
            // ...
        }
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                //setupPermissions(Manifest.permission.CAMERA)
                //setupPermissions(Manifest.permission.RECORD_AUDIO)
                //setupPermissions(Manifest.permission)
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            storage = FirebaseStorage.getInstance()
            mChronometer = view?.findViewById(R.id.chronometer);
            setupPermissions(Manifest.permission.CAMERA)
            setupPermissions(Manifest.permission.RECORD_AUDIO)
            setupPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val window: Window = activity!!.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            //window.statusBarColor() to default ie transparent
            // Inflate the layout for this fragment
            // This callback will only be called when MyFragment is at least Started.
            // This callback will only be called when MyFragment is at least Started.
            requireActivity().onBackPressedDispatcher.addCallback(this, callback)
            return inflater.inflate(layout.fragment_video_resume, container, false)
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() { // Handle the back button event
                    onBackPressed()
                }
            }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
//            if(viewpager.currentItem <=2 )
//            {
//                Toast.makeText(context, "counter:>>>>>>>>>>>>>> "+viewpager.currentItem.toString(), Toast.LENGTH_SHORT).show()
//                button_video.visibility = View.INVISIBLE
//            }
//            if(button_video?.visibility==View.VISIBLE){
//                button_video?.setOnClickListener {
//                    dispatchTakeVideoIntent()
//                }
//            }

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

            play_video.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW)
                //onActivityResult(REQUEST_VIDEO_CAPTURE, RESULT_OK, intent )
                onActivityResult(VIDEO_CAPTURE, RESULT_OK, intent )
            }

            button_video.setOnClickListener (object : View.OnClickListener {
                val REQUEST_TAKE_GALLERY_VIDEO = 1;
                var mediaFile: File = File(
                    Environment.getExternalStorageDirectory().absolutePath.toString() + "/myvideo.mp4"
                )
                    override fun onClick(v: View?) {
                        val intent = Intent()
//
//                        if (ActivityCompat.checkSelfPermission(
//                                context!!,
//                                Manifest.permission.ACCESS_MEDIA_LOCATION
//                            ) != PackageManager.PERMISSION_GRANTED &&
//                            ActivityCompat.checkSelfPermission(
//                                context!!,
//                                Manifest.permission.CAMERA
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            requestPermissions(
//                                activity!!, arrayOf(
//                                    Manifest.permission.ACCESS_MEDIA_LOCATION,
//                                    Manifest.permission.CAMERA
//                                ),
//                                REQUEST_VIDEO_CAPTURE
//                            )
//                        } else {
//                            Log.d("DB", "PERMISSION GRANTED")
//                        }
                        //intent.type = "camera"
                        intent.action = MediaStore.ACTION_VIDEO_CAPTURE
                        val videoUri: Uri = FileProvider.getUriForFile(Objects.requireNonNull(v?.context!!),
                            BuildConfig.APPLICATION_ID + ".provider", mediaFile);

                        // ref: https://stackoverflow.com/questions/56598480/couldnt-find-meta-data-for-provider-with-authority
                        //val videoUri: Uri =  FileProvider.getUriForFile(v?.context!!,"com.gigforce.app.modules.onboarding",mediaFile)//Uri.fromFile(mediaFile)

//                        val previewVideoUrl = Uri.parse(videoUri)
//                        val builder = PreviewProgram.Builder()
//                        builder.setChannelId(channelId)
//                            // ...
//                            .setPreviewVideoUri(previewVideoUrl)

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                        startActivityForResult(intent, VIDEO_CAPTURE);

                        uploadVideotoFB(storage.reference.child("myvideo.mp4"), videoUri)
//                        intent.type = "video/*"
//                        intent.action = Intent.ACTION_GET_CONTENT
//                        startActivityForResult(
//                            Intent.createChooser(intent, "Select Video"),
//                            REQUEST_TAKE_GALLERY_VIDEO
//                        )
                    }
                })
        }

    val VIDEO_CAPTURE = 101;
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(this.context, "counter:>>VIDEO>>>>>> "+data?.data.toString(), Toast.LENGTH_SHORT).show()
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Toast.makeText(this.context, "Video saved to:\n" + data.data, Toast.LENGTH_LONG).show()
                    val videoUri: Uri? = data.data
                    videoView?.setVideoURI(videoUri)
//                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoUri,MediaStore.Images.Thumbnails.MINI_KIND);
//                    //VideoView video = (VideoView) findViewById(R.id.videoview1);
//                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumbnail);
//                    videoView.setBackgroundDrawable(bitmapDrawable);
                    //ref:https://stackoverflow.com/questions/7037630/how-to-create-a-video-preview-in-android
                };
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this.context, "Video recording cancelled.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this.context, "Failed to record video", Toast.LENGTH_LONG).show();
            }
        }
    }

        private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            //takeVideoIntent.resolveActivity()?.also {
                startActivityForResult(takeVideoIntent,
                    REQUEST_VIDEO_CAPTURE
                )
            //}
        }
    }

        fun setupViewPager(){
            this.viewpager.adapter =
                VideoResumeViewPagerAdapter(
                    this.viewpager,
                    object : OnVideoResumeCompleted() {

                        override fun invoke() {
                            this@VideoResumeFragment.findNavController()
                                .navigate(getResourceToNavigateTo())
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

        fun onBackPressed() {
            var mPager = this.viewpager;
            if (mPager.currentItem == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                //super.onBackPressed()
                findNavController().popBackStack()
            } else {
                // Otherwise, select the previous step.
                mPager.currentItem = mPager.currentItem - 1
            }
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
                                      val onVideoResumeCompleted: OnVideoResumeCompleted
    ): RecyclerView.Adapter<VideoResumeViewPagerAdapter.ViewHolder>(){

        class ViewHolder(view: View,
                         val viewpager: ViewPager2,
                         val onVideoResumeCompleted: OnVideoResumeCompleted
        ): RecyclerView.ViewHolder(view) {

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
            return ViewHolder(
                view,
                viewpager,
                onVideoResumeCompleted
            )
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.Bind(position)
        }
}