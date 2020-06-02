package com.gigforce.app.utils

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import com.franmontiel.localechanger.LocaleChanger
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.auth.AuthFlowFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_select_language.*
import java.util.*


class LanguageSelectFragment : BaseFragment() {

    val SUPPORTED_LOCALES =
        Arrays.asList(
            Locale("en", "US"),
            Locale("hi", "IN"),
            Locale("fr", "FR")
            //Locale("ar", "JO")
        )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        this.setDarkStatusBarTheme(true)
        try {
            LocaleChanger.initialize(this.context, SUPPORTED_LOCALES)
        } catch (e: Exception) {
        }
        return inflateView(R.layout.fragment_select_language, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dismissLanguageSelectionDialog()
        storeDeviceLanguage()
        initializer()
        setDefaultLanguage()
        listener()
    }
    private fun dismissLanguageSelectionDialog() {
        //LanguageSelectFragment is the first screen we don't need alert here and its picking up device language already for radio button.
        if(languageSelectionDialog!=null){
            languageSelectionDialog!!.dismiss()
        }
    }
    private fun storeDeviceLanguage() {
        saveSharedData(AppConstants.DEVICE_LANGUAGE, Resources.getSystem().getConfiguration().locale.getLanguage())
    }

    private fun initializer() {
        groupradio.clearCheck()
    }

    private fun listener() {
        nextbuttonLang.setOnClickListener() {
            onNextButtonClicked()
        }

    }

    private fun setDefaultLanguage() {
        when(Resources.getSystem().getConfiguration().locale.getLanguage()){
            "en" -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
            "hi" -> groupradio.findViewById<RadioButton>(R.id.hi).isChecked = true
            "te" -> groupradio.findViewById<RadioButton>(R.id.te).isChecked = true
            "gu" -> groupradio.findViewById<RadioButton>(R.id.gu).isChecked = true
            "pa" -> groupradio.findViewById<RadioButton>(R.id.pu).isChecked = true
            "fr" -> groupradio.findViewById<RadioButton>(R.id.fr).isChecked = true
            "mr" -> groupradio.findViewById<RadioButton>(R.id.mr).isChecked = true
            else -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
        }

    }

    private fun onNextButtonClicked() {
        val selectedId = groupradio.checkedRadioButtonId
        if (selectedId == -1) {
            Snackbar.make(getFragmentView(), "Please Select a Language!", Snackbar.LENGTH_SHORT)
                .show()
        } else {
            val radioButton = groupradio.findViewById<RadioButton>(selectedId)
            val lang = radioButton.hint.toString()
            updateResources(lang)
            saveSharedData(AppConstants.APP_LANGUAGE, lang)
            navNext()
        }
    }

    private fun navNext() {
        navigate(R.id.authFlowFragment)
    }



    override fun onDestroyView() {
        LocaleChanger.resetLocale()
        super.onDestroyView()
    }
}