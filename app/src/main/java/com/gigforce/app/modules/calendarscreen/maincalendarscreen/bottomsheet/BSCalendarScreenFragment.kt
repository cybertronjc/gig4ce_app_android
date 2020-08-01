package com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet

import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.toDate
import com.gigforce.app.modules.gigPage.GigAttendancePageFragment
import com.gigforce.app.modules.gigPage.GigPageFragment
import com.gigforce.app.modules.gigPage.GigPageNavigationFragment
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.landingscreen.LandingScreenFragment
import com.gigforce.app.utils.DateHelper
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.home_screen_bottom_sheet_fragment.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class BSCalendarScreenFragment : BaseFragment() {

    companion object {
        fun newInstance() = BSCalendarScreenFragment()
    }

    private lateinit var viewModel: BSCalendarScreenViewModel
    private val gigViewModel: GigViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.home_screen_bottom_sheet_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BSCalendarScreenViewModel::class.java)
        initializeBottomSheet()
        initGigViewModel()
    }

    private fun initGigViewModel() {
        gigViewModel.upcomingGigs
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> initializeUpcomingGigBottomSheet(it.content)
                    is Lce.Error -> {
                    }
                }
            })

        gigViewModel.watchUpcomingGigs()
    }

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }

    private fun initializeBottomSheet() {
//        nsv.setBackground(generateBackgroundWithShadow(nsv,R.color.white,
//            R.dimen.eight_dp,R.color.gray_color,R.dimen.five_dp, Gravity.TOP))
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        initializeVerificationAlert()
        initializeFeaturesBottomSheet()
        initializeLearningModule()
        initializeAssessmentBottomSheet()
        application_version.text = "version " + getCurrentVersion()
        listener()
    }

    private fun initializeVerificationAlert() {
        var clickhere: String = "Click here";
        var content: SpannableString = SpannableString(clickhere);
        content.setSpan(UnderlineSpan(), 0, content.length, 0);
        kyc_tv.text =
            Html.fromHtml("Kyc Verification is not done , <font color='#060606'><u>Click here</u></font> to  complete.")
        video_resume_tv.text =
            Html.fromHtml("Your Video resume is Pending , <font color='#060606'><u>Click here</u></font> to  complete.")
    }

    private fun listener() {
        kyc_tv.setOnClickListener() {
            navigate(R.id.gigerVerificationFragment)
        }
        video_resume.setOnClickListener() {
            navigate(R.id.videoResumeFragment)
        }
        show_upcominggig_layout.setOnClickListener() {
            showKYCAndHideUpcomingLayout(false)
        }
    }

    private fun showKYCAndHideUpcomingLayout(show: Boolean) {
        if (show) {
            kyc_video_resume.visibility = View.VISIBLE
            upcoming_gig_title.visibility = View.GONE
            upcoming_gig_rv.visibility = View.GONE
        } else {
            upcoming_gig_title.visibility = View.VISIBLE
            upcoming_gig_rv.visibility = View.VISIBLE
            kyc_video_resume.visibility = View.GONE
        }
    }

    private val timeFormatter = SimpleDateFormat("hh.mm aa")

    var width: Int = 0
    private fun initializeUpcomingGigBottomSheet(upcomingGigs: List<Gig>) {
        if (upcomingGigs.isNotEmpty()) {
            upcoming_gig_rv.visibility = View.VISIBLE
            upcoming_gig_title.visibility = View.VISIBLE

            val itemWidth = ((width / 5) * 4).toInt()
            val recyclerGenericAdapter: RecyclerGenericAdapter<Gig> =
                RecyclerGenericAdapter<Gig>(
                    activity?.applicationContext,
                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                        val gig = item as Gig
                        navigate(R.id.presentGigPageFragment, Bundle().apply {
                            this.putString(GigPageFragment.INTENT_EXTRA_GIG_ID, gig.gigId)
                        })

//                    showKYCAndHideUpcomingLayout(
//                        true
//                    )
                    },
                    RecyclerGenericAdapter.ItemInterface<Gig?> { obj, viewHolder, position ->
                        val lp = getView(viewHolder, R.id.card_view).layoutParams
                        lp.height = lp.height
                        lp.width = itemWidth
                        getView(viewHolder, R.id.card_view).layoutParams = lp

                        getView(viewHolder, R.id.card_view).layoutParams = lp
                        getTextView(viewHolder, R.id.textView41).text = obj?.title
                        getTextView(viewHolder, R.id.contactPersonTV).text =
                            obj?.gigContactDetails?.contactName

                        if (obj!!.isGigOfToday()) {

                            val gigTiming = if (obj.endDateTime != null)
                                "${timeFormatter.format(obj.startDateTime!!.toDate())} - ${timeFormatter.format(
                                    obj.endDateTime!!.toDate()
                                )}"
                            else
                                "${timeFormatter.format(obj.startDateTime!!.toDate())} - "
                            getTextView(viewHolder, R.id.textView67).text = gigTiming
                            getView(viewHolder, R.id.checkInTV).setOnClickListener(
                                CheckInClickListener(
                                    upcoming_gig_rv,
                                    position
                                )
                            )

                            if (!obj.isPresentGig()) {
                                getView(viewHolder, R.id.checkInTV).isEnabled = false
                            } else if (obj.isCheckInAndCheckOutMarked()) {
                                getView(viewHolder, R.id.checkInTV).isEnabled = false
                            } else if (obj.isCheckInMarked()) {
                                getView(viewHolder, R.id.checkInTV).isEnabled = true
                                (getView(viewHolder, R.id.checkInTV) as Button).text = "Check Out"
                            } else {
                                getView(viewHolder, R.id.checkInTV).isEnabled = true
                                (getView(viewHolder, R.id.checkInTV) as Button).text = "Check In"
                            }

                        } else {
                            getView(viewHolder, R.id.checkInTV).isEnabled = false

                            val date = DateHelper.getDateInDDMMYYYY(obj.startDateTime!!.toDate())
                            getTextView(viewHolder, R.id.textView67).text = date
                        }

                        getView(viewHolder, R.id.navigateTV).setOnClickListener(
                            NavigationClickListener(upcoming_gig_rv, position)
                        )

                        getView(viewHolder, R.id.callCardView).setOnClickListener(
                            CallClickListener(
                                upcoming_gig_rv,
                                position
                            )
                        )
                        getView(viewHolder, R.id.messageCardView).setOnClickListener(
                            ChatClickListener(upcoming_gig_rv, position)
                        )
                        val companyLogoIV = getImageView(viewHolder, R.id.companyLogoIV)

                        if (!obj.companyLogo.isNullOrBlank()) {

                            if (obj.companyLogo!!.startsWith("http", true)) {

                                Glide.with(requireContext())
                                    .load(obj.companyLogo)
                                    .into(companyLogoIV)

                            } else {
                                FirebaseStorage.getInstance()
                                    .getReference("companies_gigs_images")
                                    .child(obj.companyLogo!!)
                                    .downloadUrl
                                    .addOnSuccessListener {

                                        Glide.with(requireContext())
                                            .load(it)
                                            .into(companyLogoIV)
                                    }
                            }
                        } else {
                            val companyInitials = if (obj.companyName.isNullOrBlank())
                                "C"
                            else
                                obj.companyName!![0].toString().toUpperCase()
                            val drawable = TextDrawable.builder().buildRound(
                                companyInitials,
                                ResourcesCompat.getColor(resources, R.color.lipstick, null)
                            )

                            companyLogoIV.setImageDrawable(drawable)
                        }

                    })!!
            recyclerGenericAdapter.setList(upcomingGigs)
            recyclerGenericAdapter.setLayout(R.layout.upcoming_gig_item)
            upcoming_gig_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            upcoming_gig_rv.adapter = recyclerGenericAdapter
        } else {
            upcoming_gig_rv.visibility = View.GONE
            upcoming_gig_title.visibility = View.GONE
        }
    }

    inner class NavigationClickListener(val rv: RecyclerView, var position: Int) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)

            navigate(R.id.gigPageNavigationFragment, Bundle().apply {
                this.putString(GigPageNavigationFragment.INTENT_EXTRA_GIG_ID, gig.gigId)
            })
        }
    }

    inner class CheckInClickListener(val rv: RecyclerView, var position: Int) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)

            navigate(R.id.gigAttendancePageFragment, Bundle().apply {
                this.putString(GigAttendancePageFragment.INTENT_EXTRA_GIG_ID, gig.gigId)
            })
        }
    }

    inner class CallClickListener(val rv: RecyclerView, var position: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)

            if(gig.contactNo.isNullOrBlank()) return
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig.contactNo, null))
            startActivity(intent)
        }
    }

    inner class ChatClickListener(val rv: RecyclerView, var position: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)
            navigate(R.id.contactScreenFragment)
        }
    }

    fun generateBackgroundWithShadow(
        view: View, @ColorRes backgroundColor: Int,
        @DimenRes cornerRadius: Int,
        @ColorRes shadowColor: Int,
        @DimenRes elevation: Int,
        shadowGravity: Int
    ): Drawable? {
        val cornerRadiusValue =
            view.context.resources.getDimension(cornerRadius)
        val elevationValue = view.context.resources.getDimension(elevation).toInt()
        val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)
        val backgroundColorValue = ContextCompat.getColor(view.context, backgroundColor)
        val outerRadius = floatArrayOf(
            cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
            cornerRadiusValue, cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
            cornerRadiusValue
        )
        val backgroundPaint = Paint()
        backgroundPaint.setStyle(Paint.Style.FILL)
        backgroundPaint.setShadowLayer(cornerRadiusValue, 0F, 0F, 0)
        val shapeDrawablePadding = Rect()
        shapeDrawablePadding.left = elevationValue
        shapeDrawablePadding.right = elevationValue
        val DY: Int
        when (shadowGravity) {
            Gravity.CENTER -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue
                DY = 0
            }
            Gravity.TOP -> {
                shapeDrawablePadding.top = elevationValue * 2
                shapeDrawablePadding.bottom = elevationValue
                DY = -1 * elevationValue / 3
            }
            Gravity.BOTTOM -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue * 2
                DY = elevationValue / 3
            }
            else -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue * 2
                DY = elevationValue / 3
            }
        }
        val shapeDrawable = ShapeDrawable()
        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColorValue
        shapeDrawable.paint
            .setShadowLayer(cornerRadiusValue / 3, 0f, DY.toFloat(), shadowColorValue)
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
        val drawable =
            LayerDrawable(arrayOf<Drawable>(shapeDrawable))
        drawable.setLayerInset(
            0,
            elevationValue,
            elevationValue * 2,
            elevationValue,
            elevationValue * 2
        )
        return drawable
    }

    private fun initializeFeaturesBottomSheet() {
        var datalist: ArrayList<FeatureModel> = ArrayList<FeatureModel>()
        datalist.add(FeatureModel("My Gig", R.drawable.mygig, -1))
        datalist.add(FeatureModel("Explore", R.drawable.ic_landinghome_search, -1))
        datalist.add(FeatureModel("Wallet", R.drawable.wallet, R.id.walletBalancePage))
        datalist.add(FeatureModel("Profile", R.drawable.profile, R.id.profileFragment))
        datalist.add(FeatureModel("Learning", R.drawable.learning, R.id.mainLearningFragment))
        datalist.add(FeatureModel("Settings", R.drawable.settings, R.id.settingFragment))
        datalist.add(FeatureModel("Chat", R.drawable.chat, R.id.contactScreenFragment))
        datalist.add(FeatureModel("Landing HS", R.drawable.chat, R.id.landinghomefragment))

        val itemWidth = ((width / 7) * 1.6).toInt()
        val recyclerGenericAdapter: RecyclerGenericAdapter<FeatureModel> =
            RecyclerGenericAdapter<FeatureModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<FeatureModel?> { view, position, item ->
                    if (item?.navigationID != -1) navigate(item?.navigationID!!)
                },
                RecyclerGenericAdapter.ItemInterface<FeatureModel?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.card_view).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.card_view).layoutParams = lp
                    getImageView(viewHolder, R.id.feature_icon).setImageResource(obj?.icon!!)
                    getTextView(viewHolder, R.id.feature_title).text = obj.title

                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.feature_item)
        feature_rv.setLayoutManager(
            GridLayoutManager(
                activity, 2,
                GridLayoutManager.HORIZONTAL, false
            )
        );
        feature_rv.adapter = recyclerGenericAdapter
    }

//    private fun navigateToFeature(position: Int) {
//        when (position) {
//            0 -> showToast("")
//            1 -> showToast("")
//            2 -> navigate(R.id.walletBalancePage)
//            3 -> navigate(R.id.profileFragment)
//            4 -> showToast("")
//            5 -> navigate(R.id.settingFragment)
//            6 -> navigate(R.id.helpChatFragment)
//            7 -> navigate(R.id.landinghomefragment)
//        }
//    }

    private fun initializeLearningModule() {

        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<LandingScreenFragment.TitleSubtitleModel> =
            ArrayList<LandingScreenFragment.TitleSubtitleModel>()

        datalist.add(
            LandingScreenFragment.TitleSubtitleModel(
                "Retail Sales Executive",
                "Demonstrate products to customers", R.drawable.learning2
            )
        )

        datalist.add(
            LandingScreenFragment.TitleSubtitleModel(
                "Quick Service Restaurant",
                "Manage food displays",
                R.drawable.learning1
            )
        )
        datalist.add(
            LandingScreenFragment.TitleSubtitleModel(
                "Delivery",
                "Maintaining hygiene and safety",
                R.drawable.learning_bg
            )
        )
        datalist.add(
            LandingScreenFragment.TitleSubtitleModel(
                "Retail Sales Executive",
                "Help customers choose the right products",
                R.drawable.learning2
            )
        )
        val recyclerGenericAdapter: RecyclerGenericAdapter<LandingScreenFragment.TitleSubtitleModel> =
            RecyclerGenericAdapter<LandingScreenFragment.TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.mainLearningFragment)
                },
                RecyclerGenericAdapter.ItemInterface<LandingScreenFragment.TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title_)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.title)
                    subtitle.text = obj?.subtitle

                    var img = getImageView(viewHolder, R.id.learning_img)
                    img.setImageResource(obj?.imgIcon!!)
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        learning_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_rv.adapter = recyclerGenericAdapter
    }

    class Assessment(var title: String, var time: String, var status: Boolean) {

    }


    private fun initializeAssessmentBottomSheet() {
        val itemWidth = ((width / 5) * 2.8).toInt()
        var datalist: ArrayList<Assessment> = ArrayList<Assessment>()

        datalist.add(
            Assessment(
                "Getting prepared for a product demo",
                "02:00 Min",
                true
            )
        )
        datalist.add(
            Assessment(
                "Conducting an effective product demo",
                "05:00 Min",
                false
            )
        )
        datalist.add(
            Assessment(
                "How to connect with customer",
                "05:00 Min",
                false
            )
        )
        datalist.add(
            Assessment(
                "Closing the product demo",
                "05:00 Min",
                false
            )
        )


        val recyclerGenericAdapter: RecyclerGenericAdapter<Assessment> =
            RecyclerGenericAdapter<Assessment>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> navigate(R.id.assessment_fragment) },
                RecyclerGenericAdapter.ItemInterface<Assessment?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.assessment_cl).layoutParams = lp
                    getTextView(viewHolder, R.id.title).text = obj?.title
                    getTextView(viewHolder, R.id.time).text = obj?.time

                    if (obj?.status!!) {
                        getTextView(viewHolder, R.id.status).text = "COMPLETED"
                        getTextView(
                            viewHolder,
                            R.id.status
                        ).setBackgroundResource(R.drawable.rect_assessment_status_completed)
                        (getView(
                            viewHolder,
                            R.id.side_bar_status
                        ) as CardView).setCardBackgroundColor(resources.getColor(R.color.status_bg_completed))

                    } else {
                        getTextView(viewHolder, R.id.status).text = "PENDING"
                        getTextView(
                            viewHolder,
                            R.id.status
                        ).setBackgroundResource(R.drawable.rect_assessment_status_pending)
                        (getView(
                            viewHolder,
                            R.id.side_bar_status
                        ) as CardView).setCardBackgroundColor(resources.getColor(R.color.status_bg_pending))
                    }

                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
        assessment_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        assessment_rv.adapter = recyclerGenericAdapter
    }

}