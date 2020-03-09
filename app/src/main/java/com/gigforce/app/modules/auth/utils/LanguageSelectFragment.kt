package com.gigforce.app.modules.auth.utils

//import com.franmontiel.localechanger.sample.SampleApplication.SUPPORTED_LOCALES

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import butterknife.ButterKnife
import butterknife.Unbinder
import com.franmontiel.localechanger.LocaleChanger
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_select_language.view.*
import java.util.*


class LanguageSelectFragment : Fragment(){

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
        lateinit var layout:View;
        ///@BindView(com.gigforce.app.R.id.localeSpinner)
        lateinit var localeSpinner: Spinner;// = layout.findViewById(R.id.localeSpinner)
        lateinit var radioGroup:RadioGroup;
        /*
        @BindView(com.gigforce.app.R.id.currentLocale)
        var currentLocale: TextView? = null
        @BindView(com.gigforce.app.R.id.date)
        var date: TextView? = null

         */
        private var unbinder: Unbinder? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val window: Window = activity!!.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(activity!!.resources.getColor(R.color.colorAccent))
            LocaleChanger.initialize(this.context, SUPPORTED_LOCALES)
            layout = inflater.inflate(R.layout.fragment_select_language, container, false)
            radioGroup = layout.findViewById(R.id.groupradio);
            radioGroup.clearCheck();
            unbinder = ButterKnife.bind(this, layout)
            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            layout.nextbuttonLang.setOnClickListener(){
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId == -1) {
                    Toast.makeText(this.context,"No answer has been selected",Toast.LENGTH_SHORT).show()
                } else {
                    val radioButton = radioGroup.findViewById<View>(selectedId) as RadioButton
                    updateResources(radioButton.hint.toString())
                    //Toast.makeText(this.context, "2>>>>>>>>>>$selectedId",Toast.LENGTH_SHORT).show()
                    // TODO findNavController().navigate(R.id.loginFragment)
                    //findNavController().navigate(R.id.videoResumeFragment)
                    findNavController().navigate(R.id.profileFragment)
                    //Toast.makeText(this.context,">>>>"+LocaleChanger.getLocale().language.toString(),Toast.LENGTH_SHORT).show()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback {
                // navController.popBackStack(R.id.homeFragment, false)
                // Do Nothing on back!
                // todo: experience need to improve.
                LocaleChanger.resetLocale()
                findNavController().popBackStack(R.id.introSlidesFragment,false);
            }
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback {
            // todo: experience need to improve.
            LocaleChanger.resetLocale()
            findNavController().popBackStack(R.id.introSlidesFragment,false);
        }


    }

    private fun updateResources(language: String)   {
        val locale = Locale(language)
        val config2 = Configuration()
        config2.locale = locale
        // updating locale
        context?.resources?.updateConfiguration(config2, null)
        Locale.setDefault(locale)
    }

/*
    override fun onResume() {
            super.onResume()
            currentLocale?.text = Locale.getDefault().toString()
            date?.setText(DateProvider.provideSystemLocaleFormattedDate())
        }
*/

        override fun onDestroyView() {
            unbinder?.unbind()
            LocaleChanger.resetLocale()
            super.onDestroyView()
        }
    }