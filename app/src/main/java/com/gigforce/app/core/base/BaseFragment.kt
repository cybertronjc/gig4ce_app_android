package com.gigforce.app.core.base

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.CoreConstants
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.configrepository.ConfigDataModel
import com.gigforce.app.utils.configrepository.ConfigRepository
import com.gigforce.app.utils.popAllBackStates
import java.util.*

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [BaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
abstract class BaseFragment : Fragment() {
    //    abstract fun Activate(fragmentView: View?)
    var mView: View? = null
    lateinit var baseFragment: BaseFragment
    lateinit var navController: NavController
    lateinit var preferencesRepositoryForBaseFragment: PreferencesRepository
    private var configrepositoryObj: ConfigRepository? = null;
    companion object {
        var englishCode = "en"
        var hindiCode = "hi"
        var telguCode = "te"
        var gujratiCode = "gu"
        var punjabiCode = "pa"
        var françaisCode = "fr"
        var marathiCode = "mr"
    }

    open fun activate(view: View?) {}


    open fun inflateView(
        resource: Int, inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        baseFragment = this
        mView = inflater.inflate(resource, container, false)
        getActivity()?.setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );
        init()

        activate(mView)
        try {
            showDialogIfDeviceLanguageChanged()
        }catch (e:Exception){

        }
        return mView
    }



    private fun showDialogIfDeviceLanguageChanged() {
        preferencesRepositoryForBaseFragment = PreferencesRepository()
        var currentDeviceLanguageCode =
            Resources.getSystem().getConfiguration().locale.getLanguage()
        var currentDeviceLanguageName = getLanguageCodeToName(currentDeviceLanguageCode)
        if (!currentDeviceLanguageName.equals("") && !currentDeviceLanguageCode.equals(
                lastStoredDeviceLanguage()
            )
        ) {
            confirmDialogForChangedLanguage(currentDeviceLanguageCode, currentDeviceLanguageName)
        }
    }

    fun getLanguageCodeToName(currentDeviceLanguage: String): String {
        when (currentDeviceLanguage) {
            englishCode -> return getString(R.string.english)
            hindiCode -> return getString(R.string.hindi)
            telguCode -> return getString(R.string.telgu)
            gujratiCode -> return getString(R.string.gujrati)
            punjabiCode -> return getString(R.string.punjabi)
            françaisCode -> return getString(R.string.francais)
            marathiCode -> return getString(R.string.marathi)
            else -> return ""
        }
    }

    var languageSelectionDialog: Dialog? = null
    private fun confirmDialogForChangedLanguage(
        currentDeviceLanguageCode: String,
        currentDeviceLanguageString: String
    ) {
        languageSelectionDialog = activity?.let { Dialog(it) }
        languageSelectionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        languageSelectionDialog?.setCancelable(false)
        languageSelectionDialog?.setContentView(R.layout.confirmation_custom_alert_type1)
        val titleDialog = languageSelectionDialog?.findViewById(R.id.title) as TextView
        titleDialog.text =
            "Your device language changed to " + currentDeviceLanguageString + ". Do you want to continue with this language?"
        val yesBtn = languageSelectionDialog?.findViewById(R.id.yes) as TextView
        val noBtn = languageSelectionDialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {

            saveSharedData(AppConstants.DEVICE_LANGUAGE, currentDeviceLanguageCode)
            saveSharedData(AppConstants.APP_LANGUAGE, currentDeviceLanguageCode)
            saveSharedData(AppConstants.APP_LANGUAGE_NAME, currentDeviceLanguageString)
            updateResources(currentDeviceLanguageCode)
            preferencesRepositoryForBaseFragment.setDataAsKeyValue(
                "languageName",
                currentDeviceLanguageString
            )
            preferencesRepositoryForBaseFragment.setDataAsKeyValue(
                "languageCode",
                currentDeviceLanguageCode
            )
            languageSelectionDialog?.dismiss()
        }
        noBtn.setOnClickListener {
            saveSharedData(AppConstants.DEVICE_LANGUAGE, currentDeviceLanguageCode)
            languageSelectionDialog!!.dismiss()
        }
        languageSelectionDialog?.show()
    }

    //Confirmation dialog start
    // this dialog having right side yes button with gradient. Need to create one having swipable functionality
    fun showConfirmationDialogType1(title:String, buttonClickListener:ConfirmationDialogOnClickListener){
        var customialog:Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type1)
        val titleDialog = customialog?.findViewById(R.id.title) as TextView
        titleDialog.text = title
        val yesBtn = customialog?.findViewById(R.id.yes) as TextView
        val noBtn = customialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener (View.OnClickListener {
            buttonClickListener.clickedOnYes(customialog)
        })
        noBtn.setOnClickListener (View.OnClickListener { buttonClickListener.clickedOnNo(customialog) })
        customialog?.show()
    }
    fun showConfirmationDialogType2(title:String, buttonClickListener:ConfirmationDialogOnClickListener){
        var customialog:Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type2)
        val titleDialog = customialog?.findViewById(R.id.title) as TextView
        titleDialog.text = title
        val yesBtn = customialog?.findViewById(R.id.yes) as TextView
        val noBtn = customialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener (View.OnClickListener {
            buttonClickListener.clickedOnYes(customialog)
        })
        noBtn.setOnClickListener (View.OnClickListener { buttonClickListener.clickedOnNo(customialog) })
        customialog?.show()
    }
    interface ConfirmationDialogOnClickListener{
        fun clickedOnYes(dialog:Dialog?)
        fun clickedOnNo(dialog:Dialog?)
    }
    //Confirmation dialog end

    fun updateResources(language: String) {
        val locale = Locale(language)
        val config2 = Configuration()
        config2.locale = locale
        // updating locale
        context?.resources?.updateConfiguration(config2, null)
        Locale.setDefault(locale)
    }

    private fun lastStoredDeviceLanguage(): String? {
        return getSharedData(AppConstants.DEVICE_LANGUAGE, "")
    }

    lateinit var SP: SharedPreferences
    // SP = this.getPreferences(Context.MODE_PRIVATELD_WRITEABLE);
    var editor: SharedPreferences.Editor? = null

    private fun init() { // GPS=new GPSTracker(this);
        navController = activity?.findNavController(R.id.nav_fragment)!!
        SP = activity?.getSharedPreferences(
            CoreConstants.SHARED_PREFERENCE_DB,
            Context.MODE_PRIVATE
        )!!
        this.editor = SP.edit()
        configObserver()
    }
    var configDataModel : ConfigDataModel? = null
    private fun configObserver() {
        this.configrepositoryObj = ConfigRepository.getInstance()
        this.configrepositoryObj?.configLiveDataModel?.observe(viewLifecycleOwner, androidx.lifecycle.Observer { configDataModel ->
            this.configDataModel = configDataModel
        })
    }

    fun getFragmentView(): View {
        return mView!!
    }

    open fun saveSharedData(Key: String?, Value: String?): Boolean {
        return try {
            editor?.putString(Key, Value)
            editor?.commit()
            true
        } catch (ex: Exception) {
            Log.e("Error:", ex.toString())
            false
        }
    }

    // for delete
    fun removeSavedShareData(key: String?): Boolean {
        return try {
            editor?.remove(key)
            editor?.commit()
            true
        } catch (ex: Exception) {
            Log.e("Error:", ex.toString())
            false
        }
    }

    open fun getSharedData(key: String?, defValue: String?): String? {
        return SP.getString(key, defValue)
    }

    open fun showToast(Message: String) {
        if (isNullOrWhiteSpace(Message)) return
        val toast = Toast.makeText(context, Message, Toast.LENGTH_SHORT)
        toast.show()
    }

    // Toast Message
    open fun isNullOrWhiteSpace(str: String): Boolean {
        return if (TextUtils.isEmpty(str)) true else if (str.startsWith("null")) true else false
    }

    open fun showToastLong(Message: String, Duration: Int) {
        if (isNullOrWhiteSpace(Message)) return
        val toast = Toast.makeText(context, Message, Toast.LENGTH_LONG)
        toast.show()
    }

    fun popFragmentFromStack(id: Int) {
        navController.popBackStack(id, true)
    }

    open fun navigate(
        @IdRes resId: Int, args: Bundle?,
        navOptions: NavOptions?
    ) {
        navController
            .navigate(resId, null, navOptions)
    }

    fun navigate(@IdRes resId: Int) {
        navController.navigate(resId)
    }

    fun navigateWithAllPopupStack(@IdRes resId: Int) {
        popAllBackStates()
        navigate(resId)
    }

    fun popAllBackStates() {
        navController.popAllBackStates()
    }

    fun popBackState() {
        navController.popBackStack()
    }

    fun findViewById(id: Int): View? {
        return this.mView!!.findViewById(id)
    }

    fun getTextView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): TextView {
        return view.getView(id) as TextView
    }

    fun getEditText(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): EditText {
        return view.getView(id) as EditText
    }

    fun getTextView(view: View, id: Int): TextView {
        return view.findViewById(id) as TextView
    }

    fun getImageView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): ImageView {
        return view.getView(id) as ImageView
    }

    fun getImageView(view: View, id: Int): ImageView {
        return view.findViewById(id) as ImageView
    }

    fun getRecyclerView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): RecyclerView {
        return view.getView(id) as RecyclerView
    }

    fun getView(view: View, id: Int): View {
        return view.findViewById(id)
    }

    open fun getView(view: PFRecyclerViewAdapter<Any?>.ViewHolder, id: Int): View {
        return view.getView(id)
    }

    fun setTextViewColor(textView: TextView, color: Int) {
        textView.setTextColor(ContextCompat.getColor(requireActivity().applicationContext, color))
    }

    fun setTextViewSize(textView: TextView, size: Float) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    fun setViewBackgroundColor(view: View, color: Int) {
        view.setBackgroundColor(ContextCompat.getColor(requireActivity().applicationContext, color))
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    fun getCurrentVersion(): String {
        try {
            val pInfo: PackageInfo =
                activity?.applicationContext!!.packageManager.getPackageInfo(
                    requireActivity().getPackageName(),
                    0
                )
            val version = pInfo.versionName
            return version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onDetach() {
        if (languageSelectionDialog != null) languageSelectionDialog!!.dismiss()
        super.onDetach()
    }
}