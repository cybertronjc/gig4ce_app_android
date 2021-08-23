package com.gigforce.learning.learning.slides.types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.learning.R
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lse
//import com.gigforce.app.R
//import com.gigforce.app.core.gone
//import com.gigforce.app.core.visible
import com.gigforce.learning.learning.slides.SlideViewModel
//import com.gigforce.app.utils.Lse
import kotlinx.android.synthetic.main.fragment_learning_slide_completed.*

class SlidesCompletedFragment : Fragment() {

    companion object {
        const val TAG = "SlidesCompletedFragment"

        const val INTENT_EXTRA_MODULE_ID = "module_id"
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"

        fun getInstance(
            moduleId: String,
            lessonId: String
        ): SlidesCompletedFragment {
            return SlidesCompletedFragment().apply {
                val bundle = bundleOf(
                    INTENT_EXTRA_MODULE_ID to  moduleId,
                    INTENT_EXTRA_LESSON_ID to lessonId
                )
                arguments = bundle
            }
        }
    }

    private lateinit var moduleId : String
    private lateinit var lessonId : String

    private val slidesViewModel : SlideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_learning_slide_completed, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            moduleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
            lessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
        }

        savedInstanceState?.let {

            moduleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
            lessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
        }

        initViewModel()



        go_back_btn.setOnClickListener {
            slidesViewModel.markAllSlidesAsComplete(moduleId, lessonId)
        }
    }

    private fun initViewModel() {
        slidesViewModel.markAllSlidesAsComplete.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    main_layout.gone()
                    slideProgressSavingPB.visible()
                }
                Lse.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.slided_marked), Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                }
                is Lse.Error -> {
                    Toast.makeText(requireContext(), getString(R.string.unable_to_save_progress) + it.error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MODULE_ID , moduleId)
        outState.putString(INTENT_EXTRA_LESSON_ID , lessonId)
    }
}