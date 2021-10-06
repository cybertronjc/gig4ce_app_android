package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.repository.BannerCardRepository
import com.gigforce.common_ui.viewdatamodels.BannerCardDVM
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class BannerCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    val title: TextView
    val image: ImageView
    var bannerCardData: BannerCardDVM? = null
    val topLayout: View
    val progressBar: View
    private var bannerCardRepo = BannerCardRepository()
    private var onPauseState = false

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.banner_card_component, this, true)
        title = this.findViewById(R.id.title)
        image = this.findViewById(R.id.background_img)
        progressBar = this.findViewById(R.id.progressBar)
        topLayout = this.findViewById(R.id.top_layout)
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private fun setImage(imageStr: String) {
        if (imageStr.contains("http") or imageStr.contains("https")) {
            Glide.with(context)
                .load(imageStr)
                .into(image)
        }
    }

    override fun bind(data: Any?) {
        this.setOnClickListener(null)
        if (data is BannerCardDVM) {
            bannerCardData = data
            if (data.image.isNullOrEmpty() || data.apiUrl.isNullOrEmpty()) {
                topLayout.gone()
            } else {
                if (data.image.isNotBlank()) {
                    setImage(data.image)
                }
                if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                    if (data.hi?.title.isNullOrBlank()) {
                        if (data.title.isNullOrBlank()) {
                            title.gone()
                        } else {
                            title.text = data.title
                        }
                    } else {
                        title.text = data.hi?.title
                    }
                } else if (data.title.isNullOrBlank()) {
                    title.gone()
                } else
                    title.text = data.title
            }

            data.getNavArgs()?.let { navArgs ->
                if (title.isVisible)
                    navArgs.args?.putString("title", title.text.toString())
                else {
                    if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                        if (data.hi?.defaultDocTitle.isNullOrBlank()) {
                            if (data.defaultDocTitle.isNullOrBlank()) {
                                navArgs.args?.putString(
                                    "title",
                                    resources.getString(R.string.back_to_gigforce_ui)
                                )
                            } else {
                                navArgs.args?.putString("title", data.defaultDocTitle)
                            }
                        } else {
                            navArgs.args?.putString("title", data.hi?.defaultDocTitle)
                        }
                    } else {
                        if (data.defaultDocTitle.isNullOrBlank()) {
                            navArgs.args?.putString(
                                "title",
                                resources.getString(R.string.back_to_gigforce_ui)
                            )
                        } else {
                            navArgs.args?.putString("title", data.defaultDocTitle)
                        }
                    }
                }
                image.setOnClickListener {
                    bannerCardData?.let {
                        it.apiUrl?.let {
                            getAndRedirectToDocUrl(it, navArgs.args)
                        } ?: run {
                            Toast.makeText(context, "API url not found!!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun getAndRedirectToDocUrl(apiUrl: String, bundle: Bundle?) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                val source = bundle?.getString("source") ?: ""
                val bannerName = bundle?.getString("bannerName") ?: ""
                val id = bundle?.getString("bannerId") ?: ""
                progressBar.visible()
                try {
                    val accessLogResponse = bannerCardRepo.createLogs(
                        apiUrl,
                        uid,
                        source,
                        bannerName,
                        id
                    )
                    findViewTreeLifecycleOwner()
                    if (!onPauseState) {
                        if (accessLogResponse.status == true) {
                            if (accessLogResponse.siplyResponseStatus == true) {
                                accessLogResponse.responseURL?.let {
                                    if (it.isNotBlank()) {
                                        bundle?.putString("_id", accessLogResponse._id)
                                        navigation.navigateToDocViewerActivity(
                                            null,
                                            it,
                                            "banner",
                                            bundle,
                                            context
                                        )

                                    } else {
                                        Toast.makeText(
                                            context,
                                            "There is some internal error. Please try after sometime.",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }

                                } ?: run {
                                    Toast.makeText(
                                        context,
                                        "There is some internal error. Please try after sometime.",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    accessLogResponse.responseURL,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "There is some internal error. Please try after sometime.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    progressBar?.gone()

                } catch (e: Exception) {
                    if (!onPauseState) {
                        Toast.makeText(
                            context,
                            "There is some internal error. Please try after sometime.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    progressBar?.gone()
                }
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        onPauseState = visibility != View.VISIBLE
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        onPauseState = hasWindowFocus
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onPauseState = true

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onPauseState = false
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        onPauseState = !isVisible
    }
}