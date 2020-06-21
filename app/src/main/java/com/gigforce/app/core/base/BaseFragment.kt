package com.gigforce.app.core.base

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.dialog.AppDialogsImp
import com.gigforce.app.core.base.dialog.AppDialogsInterface
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.core.base.navigation.NavigationImpl
import com.gigforce.app.core.base.navigation.NavigationInterface
import com.gigforce.app.core.base.shareddata.SharedDataImp
import com.gigforce.app.core.base.shareddata.SharedDataInterface
import com.gigforce.app.core.base.utilfeatures.UtilAndValidationImp
import com.gigforce.app.core.base.utilfeatures.UtilAndValidationInterface
import com.gigforce.app.core.base.viewsfromviews.ViewsFromViewsImpl
import com.gigforce.app.core.base.viewsfromviews.ViewsFromViewsInterface
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.utils.configrepository.ConfigDataModel
import com.gigforce.app.utils.configrepository.ConfigRepository

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [BaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class BaseFragment : Fragment(), ViewsFromViewsInterface, NavigationInterface,
    SharedDataInterface, AppDialogsInterface, UtilAndValidationInterface {

    lateinit var viewsFromViewsInterface: ViewsFromViewsInterface
    lateinit var navigationInterface: NavigationInterface
    lateinit var sharedDataInterface: SharedDataInterface
    lateinit var appDialogsInterface: AppDialogsInterface
    lateinit var utilAndValidationInterface: UtilAndValidationInterface
    lateinit var baseFragment: BaseFragment
    var mView: View? = null

    lateinit var preferencesRepositoryForBaseFragment: PreferencesRepository
    private var configrepositoryObj: ConfigRepository? = null;
    private var requestOptions: RequestOptions? = null

    open fun isConfigRequired(): Boolean {
        return false
    }

    open fun inflateView(
        resource: Int, inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        baseFragment = this
        mView = inflater.inflate(resource, container, false)
        getActivity()?.setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );
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
        navigationInterface = NavigationImpl(requireActivity())
        sharedDataInterface = SharedDataImp(requireActivity())
        appDialogsInterface = AppDialogsImp(requireActivity())
        utilAndValidationInterface = UtilAndValidationImp(requireActivity())
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

    var configDataModel: ConfigDataModel? = null
    private fun configObserver() {
        this.configrepositoryObj = ConfigRepository.getInstance()
        this.configrepositoryObj?.configCollectionListener()
        this.configrepositoryObj?.configLiveDataModel?.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { configDataModel ->
                this.configDataModel = configDataModel
            })
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    override fun onDetach() {
        if (appDialogsInterface.getDeviceLanguageDialog() != null) appDialogsInterface.getDeviceLanguageDialog()!!.dismiss()
        super.onDetach()
    }


    fun initGlide(): RequestManager? {
        if (requestOptions == null) {
            requestOptions = RequestOptions().placeholder(R.drawable.white_background)
                .error(R.drawable.white_background)
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


    //Dialogs
    private fun showDialogIfDeviceLanguageChanged() {
        var currentDeviceLanguageCode =
            appDialogsInterface.getDeviceLanguageChanged(sharedDataInterface.getLastStoredDeviceLanguage()!!)
        if (!(currentDeviceLanguageCode.equals(""))) {
            preferencesRepositoryForBaseFragment = PreferencesRepository()
            confirmDialogForDeviceLanguageChanged(currentDeviceLanguageCode,
                object : ConfirmationDialogOnClickListener {
                    override fun clickedOnYes(dialog: Dialog?) {
                        sharedDataInterface.saveDeviceLanguage(currentDeviceLanguageCode)
                        sharedDataInterface.saveAppLanuageCode(currentDeviceLanguageCode)
                        sharedDataInterface.saveAppLanguageName(
                            appDialogsInterface.getLanguageCodeToName(
                                currentDeviceLanguageCode
                            )
                        )
                        updateResources(currentDeviceLanguageCode)
                        preferencesRepositoryForBaseFragment.setDataAsKeyValue(
                            "languageName",
                            appDialogsInterface.getLanguageCodeToName(currentDeviceLanguageCode)
                        )
                        preferencesRepositoryForBaseFragment.setDataAsKeyValue(
                            "languageCode",
                            currentDeviceLanguageCode
                        )
                        dialog?.dismiss()
                    }

                    override fun clickedOnNo(dialog: Dialog?) {
                        sharedDataInterface.saveDeviceLanguage(currentDeviceLanguageCode)
                        dialog!!.dismiss()
                    }
                })
        }
    }

    override fun getDeviceLanguageChanged(deviceLanguage: String): String {
        return appDialogsInterface.getDeviceLanguageChanged(deviceLanguage)

    }

    override fun confirmDialogForDeviceLanguageChanged(
        currentDeviceLanguageCode: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        appDialogsInterface.confirmDialogForDeviceLanguageChanged(
            currentDeviceLanguageCode,
            buttonClickListener
        )
    }

    override fun getDeviceLanguageDialog(): Dialog? {
        return appDialogsInterface.getDeviceLanguageDialog()
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

    override fun getLanguageCodeToName(languageCode: String): String {
        return appDialogsInterface.getLanguageCodeToName(languageCode)
    }

    override fun showToast(message: String) {
        utilAndValidationInterface.showToast(message)
    }

    override fun isNullOrWhiteSpace(str: String): Boolean {
        return utilAndValidationInterface.isNullOrWhiteSpace(str)
    }

    override fun showToastLong(message: String, duration: Int) {
        utilAndValidationInterface.showToastLong(message, duration)
    }

    override fun getCurrentVersion(): String {
        return utilAndValidationInterface.getCurrentVersion()
    }

    override fun updateResources(language: String) {
        utilAndValidationInterface.updateResources(language)
    }

}