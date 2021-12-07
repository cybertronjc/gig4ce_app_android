package com.gigforce.common_ui.signature


import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.R
import com.gigforce.common_ui.views.GigforceSignatureView


class FullScreenSignatureDrawerDialogFragment : DialogFragment() {

    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    private lateinit var signatureView: GigforceSignatureView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_singature_drawer_full_screen, container, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signatureView = view.findViewById(R.id.signature_view)
        changeOrientation()
    }

    private fun changeOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        adjustUiforOrientation()
    }

    private fun adjustUiforOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                Log.d(TAG, "PORTRAIT")

                val scale = resources.displayMetrics.density
                val pixels = (303 * scale + 0.5f).toInt()

                signatureView.layoutParams?.height = pixels
                signatureView.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                Log.d(TAG, "LANDSCAPE")

                signatureView.layoutParams?.height = LinearLayout.LayoutParams.MATCH_PARENT
                signatureView.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }

    override fun onDestroy() {
        changeOrientation()
        super.onDestroy()
    }

    companion object {
        const val TAG = "ViewFullScreenVideoDF"

        fun launch(
            childFragmentManager: FragmentManager,
        ) {
            val frag = FullScreenSignatureDrawerDialogFragment()
            frag.show(childFragmentManager, TAG)
        }
    }
}