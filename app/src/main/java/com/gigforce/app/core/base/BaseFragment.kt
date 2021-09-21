package com.gigforce.app.core.base

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.gigforce.app.core.base.language.LanguageUtilInterface
import com.gigforce.app.core.base.navigation.NavigationImpl
import com.gigforce.app.core.base.navigation.NavigationInterface
import com.gigforce.app.core.base.viewsfromviews.ViewsFromViewsImpl
import com.gigforce.app.core.base.viewsfromviews.ViewsFromViewsInterface
import com.gigforce.app.di.implementations.AppDialogsImp
import com.gigforce.app.di.implementations.SharedPreAndCommonUtilDataImp
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.common_ui.OptionSelected
import com.gigforce.common_ui.configrepository.ConfigDataModel
import com.gigforce.common_ui.configrepository.ConfigRepository
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.base.utilfeatures.CommonUtilImp
import com.gigforce.core.base.utilfeatures.CommonUtilInterface
import com.gigforce.core.utils.NavFragmentsData

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [BaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class BaseFragment : Fragment(),
    ViewsFromViewsInterface, NavigationInterface,
    IOnBackPressedOverride,
    SharedPreAndCommonUtilInterface, AppDialogsInterface, CommonUtilInterface,
    LanguageUtilInterface {
    var navFragmentsData: NavFragmentsData? = null
    lateinit var viewsFromViewsInterface: ViewsFromViewsInterface
    lateinit var navigationInterface: NavigationInterface
    lateinit var sharedDataInterface: SharedPreAndCommonUtilInterface
    lateinit var appDialogsInterface: AppDialogsInterface
//    lateinit var languageUtilInterface: LanguageUtilInterface
    lateinit var utilAndValidationInterface: CommonUtilInterface
//    lateinit var baseFragment: BaseFragment
    var mView: View? = null

    private var configrepositoryObj: ConfigRepository? = null
    private var requestOptions: RequestOptions? = null

    open fun isConfigRequired(): Boolean {
        return false
    }

    companion object {
        var configDataModel: ConfigDataModel? = null
    }

    open fun inflateView(
        resource: Int, inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        navFragmentsData = activity as NavFragmentsData
//        baseFragment = this
        mView = inflater.inflate(resource, container, false)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initializeDI()
        init()
        try {
            if (isDeviceLanguageChangedDialogRequired())
                showDialogIfDeviceLanguageChanged()
        } catch (e: Exception) {

        }
        return mView
    }

    private fun initializeDI() {
        // there will be no any requirement further after using DI
        viewsFromViewsInterface = ViewsFromViewsImpl(requireActivity())
        navigationInterface = NavigationImpl(findNavController())
        sharedDataInterface =
            SharedPreAndCommonUtilDataImp(
                requireActivity()
            )
        appDialogsInterface =
            AppDialogsImp(requireActivity())
//        languageUtilInterface = LanguageUtilImp(this)
        utilAndValidationInterface = CommonUtilImp(requireActivity())
    }

    open fun getFragmentView(): View {
        return mView!!
    }

    open fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return true
    }

    private fun init() { // GPS=new GPSTracker(this);
        if (isConfigRequired()) {
            configObserver()
        }
    }

    private fun configObserver() {
        this.configrepositoryObj = ConfigRepository()//ConfigRepository.getInstance()
//        this.configrepositoryObj?.configLiveDataModel?.observe(
//                viewLifecycleOwner,
//                androidx.lifecycle.Observer { configDataModel1 ->
//                    configDataModel = configDataModel1
//                })
//        this.configrepositoryObj?.configCollectionListener()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDetach() {
//        if (this::languageUtilInterface.isInitialized && languageUtilInterface.getDeviceLanguageDialog() != null) languageUtilInterface.getDeviceLanguageDialog()!!
//            .dismiss()
        super.onDetach()
    }


    fun initGlide(placeHolder: Int, errorImg: Int): RequestManager? {
        if (requestOptions == null) {
            requestOptions = RequestOptions().placeholder(placeHolder)
                .error(errorImg)
        }
        return Glide.with(this).setDefaultRequestOptions(requestOptions!!)
    }


    override fun getTextView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): TextView {
        return viewsFromViewsInterface.getTextView(view, id)
    }

    override fun getTextView(view: View, id: Int): TextView {
        return getTextView(view, id)
    }

    override fun getEditText(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): EditText {
        return viewsFromViewsInterface.getEditText(view, id)
    }

    override fun getImageView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): ImageView {
        return viewsFromViewsInterface.getImageView(view, id)
    }

    override fun getImageView(view: View, id: Int): ImageView {
        return viewsFromViewsInterface.getImageView(view, id)
    }

    override fun getRecyclerView(
        view: PFRecyclerViewAdapter<Any?>.ViewHolder,
        id: Int
    ): RecyclerView {
        return viewsFromViewsInterface.getRecyclerView(view, id)
    }

    override fun getView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): View {
        return viewsFromViewsInterface.getView(view, id)
    }

    override fun getView(view: View, id: Int): View {
        return viewsFromViewsInterface.getView(view, id)
    }

    override fun setTextViewColor(textView: TextView, color: Int) {
        viewsFromViewsInterface.setTextViewColor(textView, color)
    }

    override fun setTextViewSize(textView: TextView, size: Float) {
        viewsFromViewsInterface.setTextViewSize(textView, size)
    }

    override fun setViewBackgroundColor(view: View, color: Int) {
        viewsFromViewsInterface.setViewBackgroundColor(view, color)
    }

    override fun getNavigationController(): NavController {
        return navigationInterface.getNavigationController()
    }


    //Navigation
    override fun popFragmentFromStack(id: Int) {
        navigationInterface.popFragmentFromStack(id)
    }

    override fun navigate(resId: Int, args: Bundle?, navOptions: NavOptions?) {
        navigationInterface.navigate(resId, args, navOptions)
    }

    override fun navigate(resId: Int) {
        navigationInterface.navigate(resId)
    }

    override fun navigate(resId: Int, args: Bundle?) {
        navigationInterface.navigate(resId, args)
    }

    override fun navigateWithAllPopupStack(resId: Int) {
        navigationInterface.navigateWithAllPopupStack(resId)
    }

    override fun popAllBackStates() {
        navigationInterface.popAllBackStates()
    }

    override fun popBackState() {
        navigationInterface.popBackState()
    }

    //SharedPreference
    override fun getLastStoredDeviceLanguage(): String? {
        return sharedDataInterface.getLastStoredDeviceLanguage()
    }

    override fun saveDeviceLanguage(deviceLanguage: String) {
        sharedDataInterface.saveDeviceLanguage(deviceLanguage)
    }

    override fun saveAppLanuageCode(appLanguage: String) {
        sharedDataInterface.saveAppLanuageCode(appLanguage)
    }

    override fun getAppLanguageCode(): String? {
        return sharedDataInterface.getAppLanguageCode()
    }

    override fun saveAppLanguageName(appLanguage: String) {
        sharedDataInterface.saveAppLanguageName(appLanguage)
    }

    override fun getAppLanguageName(): String? {
        return sharedDataInterface.getAppLanguageName()
    }

    override fun saveIntroCompleted() {
        sharedDataInterface.saveIntroCompleted()
    }

    override fun getIntroCompleted(): String? {
        return sharedDataInterface.getIntroCompleted()
    }

    override fun removeIntroComplete() {
        sharedDataInterface.removeIntroComplete()
    }

    override fun saveOnBoardingCompleted() {
        sharedDataInterface.saveOnBoardingCompleted()
    }

    override fun isOnBoardingCompleted(): Boolean? {
        return sharedDataInterface.isOnBoardingCompleted()
    }

    override fun saveAllMobileNumber(allMobileNumber: String) {
        sharedDataInterface.saveAllMobileNumber(allMobileNumber)
    }

    override fun getAllMobileNumber(): String? {
        return sharedDataInterface.getAllMobileNumber()
    }

    override fun saveData(key: String, value: String?) {
        sharedDataInterface.saveData(key, value)
    }

    override fun saveDataBoolean(key: String, value: Boolean?) {
        sharedDataInterface.saveDataBoolean(key, value)
    }

    override fun getData(key: String?): String? {
        return sharedDataInterface.getData(key)
    }

    override fun getDataBoolean(key: String?): Boolean? {
        return sharedDataInterface.getDataBoolean(key)
    }

    override fun remove(key: String?) {
        sharedDataInterface.remove(key)
    }

    override fun saveInt(key: String?, value: Int) {
        sharedDataInterface.saveInt(key, value)
    }

    override fun getInt(key: String?): Int {
        return sharedDataInterface.getInt(key)
    }

    override fun getChangedDeviceLanguageCode(deviceLanguage: String): String {
        return ""//languageUtilInterface.getChangedDeviceLanguageCode(deviceLanguage)

    }

    override fun confirmDialogForDeviceLanguageChanged(
        currentDeviceLanguageCode: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
//        languageUtilInterface.confirmDialogForDeviceLanguageChanged(
//            currentDeviceLanguageCode,
//            buttonClickListener
//        )
    }

    override fun showDialogIfDeviceLanguageChanged() {
//        languageUtilInterface.showDialogIfDeviceLanguageChanged()
    }

    override fun getDeviceLanguageDialog(): Dialog? {
        return null //languageUtilInterface.getDeviceLanguageDialog()
    }

    override fun showConfirmationDialogType1(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        appDialogsInterface.showConfirmationDialogType1(title, buttonClickListener)
    }

    override fun showConfirmationDialogType2(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        appDialogsInterface.showConfirmationDialogType2(title, buttonClickListener)
    }

    override fun showConfirmationDialogType3(
        title: String,
        subTitle: String,
        yesButtonText: String,
        noButtonText: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        appDialogsInterface.showConfirmationDialogType3(
            title,
            subTitle,
            yesButtonText,
            noButtonText,
            buttonClickListener
        )
    }

    override fun showConfirmationDialogType5(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        appDialogsInterface.showConfirmationDialogType5(title, buttonClickListener)
    }

    override fun showConfirmationDialogType4(
        title: String,
        subTitle: String,
        optionSelected: OptionSelected
    ) {
        appDialogsInterface.showConfirmationDialogType4(title, subTitle, optionSelected)
    }

    override fun showConfirmationDialogType7(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        appDialogsInterface.showConfirmationDialogType7(title, buttonClickListener)
    }

    override fun getLanguageCodeToName(languageCode: String): String {
        return ""//languageUtilInterface.getLanguageCodeToName(languageCode)
    }

//    override fun showToast(message: String) {
//        utilAndValidationInterface.showToast(message)
//    }
//
//    override fun isNullOrWhiteSpace(str: String): Boolean {
//        return utilAndValidationInterface.isNullOrWhiteSpace(str)
//    }
//
//    override fun showToastLong(message: String, duration: Int) {
//        utilAndValidationInterface.showToastLong(message, duration)
//    }

    override fun getCurrentVersion(): String {
        return utilAndValidationInterface.getCurrentVersion()
    }

    override fun getCurrentVersionCode(): Int {
        return 0
    }

    override fun saveLong(key: String?, value: Long) {
        sharedDataInterface.saveLong(key, value)
    }

    override fun getLong(key: String?): Long {
        return sharedDataInterface.getLong(key)
    }

    override fun saveLoggedInUserName(username: String) {

    }

    override fun getLoggedInUserName(): String {
        return ""
    }

    override fun saveLoggedInMobileNumber(mobileno: String) {

    }

    override fun getLoggedInMobileNumber(): String {
        return ""
    }

    override fun saveUserProfilePic(profilePic: String) {

    }

    override fun getUserProfilePic(): String {
        return ""
    }


    override fun updateResources(language: String) {
        utilAndValidationInterface.updateResources(language)
    }

    fun getCircularProgressDrawable(): Drawable {
//        val circularProgressDrawable = CircularProgressDrawable(requireContext())
//        circularProgressDrawable.strokeWidth = 5f
//        circularProgressDrawable.centerRadius = 20f
//        circularProgressDrawable.start()
//        return circularProgressDrawable


        return getShimmerDrawable()
    }

    fun getShimmerDrawable(): ShimmerDrawable {


        val shimmer =
            Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
                .setDuration(1800) // how long the shimmering animation takes to do one full sweep
                .setBaseAlpha(0.9f) //the alpha of the underlying children
                .setHighlightAlpha(1.0f) // the shimmer alpha amount
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build()


// This is the placeholder for the imageView
        return ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
    }

    fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

//    fun startShimmer(ll_shimmer: LinearLayout, shimmerModel: ShimmerModel) {
//        ll_shimmer.removeAllViews()
//        ll_shimmer.visible()
//        ll_shimmer.orientation = shimmerModel.orientation
//        for (i in 0 until shimmerModel.itemsToBeDrawn) {
//            val view = LayoutInflater.from(requireContext()).inflate(shimmerModel.cardRes, null)
//            ll_shimmer.addView(view)
//            val layoutParams: LinearLayout.LayoutParams =
//                    view.layoutParams as LinearLayout.LayoutParams
//            layoutParams.height = resources.getDimensionPixelSize(shimmerModel.minHeight)
//            layoutParams.width = resources.getDimensionPixelSize(shimmerModel.minWidth)
//            layoutParams.setMargins(resources.getDimensionPixelSize(shimmerModel.marginLeft),
//                    resources.getDimensionPixelSize(shimmerModel.marginTop), resources.getDimensionPixelSize(shimmerModel.marginRight),
//                    resources.getDimensionPixelSize(shimmerModel.marginBottom))
//            view.layoutParams = layoutParams
//            val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_controller)
//            shimmerLayout.startShimmer()
//        }
//    }

//    fun stopShimmer(view: LinearLayout) {
//        for (i in 0 until view.childCount) {
//            val nestedView = view.getChildAt(i)
//            val shimmerLayout = nestedView.findViewById<ShimmerFrameLayout>(R.id.shimmer_controller)
//            shimmerLayout.stopShimmer()
//
//        }
//        view.removeAllViews()
//        view.gone()
//    }
//data class ShimmerModel(@DimenRes val marginLeft: Int = R.dimen.size_16,
//                        @DimenRes val marginRight: Int =  R.dimen.size_16,
//                        @DimenRes val marginTop: Int =  R.dimen.size_16,
//                        @DimenRes val marginBottom: Int =  R.dimen.size_16,
//                        @LayoutRes var cardRes: Int = R.layout.data_placeholder_layout,
//                        @DimenRes var minWidth: Int = R.dimen.size_300,
//                        @DimenRes var minHeight: Int = R.dimen.size_200,
//                        var itemsToBeDrawn: Int = 4,
//                        var orientation: Int = LinearLayout.VERTICAL)

}