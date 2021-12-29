package com.gigforce.modules.feature_chat.screens

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.modules.feature_chat.R

class MediaDocsAndAudioFragment : Fragment() {

    companion object {
        fun newInstance() = MediaDocsAndAudioFragment()
    }

    private lateinit var viewModel: MediaDocsAndAudioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.media_docs_and_audio_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MediaDocsAndAudioViewModel::class.java)
        // TODO: Use the ViewModel
    }

}