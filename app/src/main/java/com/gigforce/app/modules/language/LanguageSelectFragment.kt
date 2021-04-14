package com.gigforce.app.modules.language

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.franmontiel.localechanger.LocaleChanger
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.configrepository.ConfigViewModel
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_select_language.*
import java.util.*


class LanguageSelectFragment : BaseFragment(), LanguageAdapter.LanguageAdapterClickListener {

    private val viewModel: ConfigViewModel by viewModels()

    private val languageAdapter: LanguageAdapter by lazy {
        LanguageAdapter(requireContext()).apply {
            setOnCitySelectedListener(this@LanguageSelectFragment)
        }
    }

    val SUPPORTED_LOCALES =
            Arrays.asList(
                    Locale("en", "US"),
                    Locale("hi", "IN"),
                    Locale("kn", "rIN"),
                    Locale("fr", "FR")
                    //Locale("ar", "JO")
            )

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        this.setDarkStatusBarTheme(true)
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(resources, com.gigforce.modules.feature_chat.R.color.lipstick_2,null))

        try {
            LocaleChanger.initialize(this.context, SUPPORTED_LOCALES)
        } catch (e: Exception) {
        }
        return inflateView(R.layout.fragment_select_language, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        dismissLanguageSelectionDialog()
        storeDeviceLanguage()
        initializer()
        setDefaultLanguage()
        listener()
        initViewModel()
    }

    private fun initViewModel() {
//        viewModel
//                .activeLanguages
//                .observe(viewLifecycleOwner, Observer { activeLangs ->
//
//                    activeLangs.forEach {
//
//                        when (it) {
//                            "en" -> groupradio.findViewById<RadioButton>(R.id.en).isEnabled = true
//                            "hi" -> groupradio.findViewById<RadioButton>(R.id.hi).isEnabled = true
//                            "kn" -> groupradio.findViewById<RadioButton>(R.id.kn).isEnabled = true
//                            "te" -> groupradio.findViewById<RadioButton>(R.id.te).isEnabled = true
//                            "gu" -> groupradio.findViewById<RadioButton>(R.id.gu).isEnabled = true
//                            "pa" -> groupradio.findViewById<RadioButton>(R.id.pu).isEnabled = true
//                            "fr" -> groupradio.findViewById<RadioButton>(R.id.fr).isEnabled = true
//                            "mr" -> groupradio.findViewById<RadioButton>(R.id.mr).isEnabled = true
//                        }
//                    }
//                })
    }

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }

//    private fun dismissLanguageSelectionDialog() {
//        //LanguageSelectFragment is the first screen we don't need alert here and its picking up device language already for radio button.
//        if(languageSelectionDialog!=null){
//            languageSelectionDialog!!.dismiss()
//        }
//    }

    private fun storeDeviceLanguage() {
        saveDeviceLanguage(Resources.getSystem().getConfiguration().locale.getLanguage())
    }

    private fun initializer() {
        // groupradio.clearCheck()

        language_recycler_view.layoutManager = GridLayoutManager(requireContext(),2)
        language_recycler_view.adapter = languageAdapter

        languageAdapter.setData(
                listOf(
                        Language(
                                languageCode = "en",
                                languageName = "English",
                                bigTextToDisplay = "Aa"
                        ),
                        Language(
                                languageCode = "hi",
                                languageName = "हिंदी",
                                bigTextToDisplay = "अआ"
                        )
                )
        )
    }

    private fun listener() {

//        nextbuttonLang.setOnClickListener() {
//            onNextButtonClicked()
//        }

        toolbar.apply {
            showTitle("Language")
            hideActionMenu()
            setBackButtonListener{
                activity?.onBackPressed()
            }
        }

    }

    private fun setDefaultLanguage() {
//        when (Resources.getSystem().getConfiguration().locale.getLanguage()) {
//            "en" -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
//            "hi" -> groupradio.findViewById<RadioButton>(R.id.hi).isChecked = true
//            "kn" -> groupradio.findViewById<RadioButton>(R.id.kn).isEnabled = true
//            "te" -> groupradio.findViewById<RadioButton>(R.id.te).isChecked = true
//            "gu" -> groupradio.findViewById<RadioButton>(R.id.gu).isChecked = true
//            "pa" -> groupradio.findViewById<RadioButton>(R.id.pu).isChecked = true
//            "fr" -> groupradio.findViewById<RadioButton>(R.id.fr).isChecked = true
//            "mr" -> groupradio.findViewById<RadioButton>(R.id.mr).isChecked = true
//            else -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
//        }

    }

    private fun onNextButtonClicked() {
//        val selectedId = groupradio.checkedRadioButtonId
//        if (selectedId == -1) {
//            Snackbar.make(getFragmentView(), "Please Select a Language!", Snackbar.LENGTH_SHORT)
//                    .show()
//        } else {
//            val radioButton = groupradio.findViewById<RadioButton>(selectedId)
//            val lang = radioButton.hint.toString()
//            updateResources(lang)
//            saveAppLanuageCode(lang)
//            saveAppLanguageName(radioButton.text.toString())
//            navNext()
//        }
    }

    private fun navNext() {
        navigate(
                R.id.authFlowFragment
        )
    }


    override fun onDestroyView() {
        LocaleChanger.resetLocale()
        super.onDestroyView()
    }

    override fun onLanguageSelected(language: Language) {
        updateResources(language.languageCode)
        saveAppLanuageCode(language.languageCode)
        saveAppLanguageName(language.languageCode)
        navNext()
    }
}