package com.gigforce.app.utils

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import com.franmontiel.localechanger.LocaleChanger
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
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
    /*
 * Copyright (c)  2017  Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setDarkStatusBarTheme()
        try {
            LocaleChanger.initialize(this.context, SUPPORTED_LOCALES)
        } catch (e: Exception) {

        }
        return inflateView(R.layout.fragment_select_language, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializer()
        setDefaultLanguage()
        listener()
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
        groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
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
        navigateWithAllPopupStack(R.id.introSlidesFragment)
    }

    private fun updateResources(language: String) {
        val locale = Locale(language)
        val config2 = Configuration()
        config2.locale = locale
        // updating locale
        context?.resources?.updateConfiguration(config2, null)
        Locale.setDefault(locale)
    }

    override fun onDestroyView() {
        LocaleChanger.resetLocale()
        super.onDestroyView()
    }
}