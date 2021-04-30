package com.gigforce.app.modules.video_resume

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import kotlinx.android.synthetic.main.layout_video_resume_view.*

class VideoView:Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState);

        var vidview: View = inflater.inflate(R.layout.layout_video_resume_view, container, false)

        var videoView =
            vidview.findViewById<View>(R.id.videoview1)//videoview1//vidview?.findViewById(R.id.videoview1)

        //videoView; // set path

        //videoView.start();
        return vidview
    }
}