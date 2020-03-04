package com.gigforce.app.modules.auth.utils

//import com.franmontiel.localechanger.sample.SampleApplication.SUPPORTED_LOCALES

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import butterknife.ButterKnife
import butterknife.OnClick
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
            //val view: View = inflater.inflate(R.layout.fragment_select_language, container, false)
            LocaleChanger.initialize(this.context, SUPPORTED_LOCALES)
            layout = inflater.inflate(R.layout.fragment_select_language, container, false)
            //localeSpinner = layout.findViewById(R.id.localeSpinner)
            radioGroup = layout.findViewById(R.id.groupradio);
            radioGroup.clearCheck();
            unbinder = ButterKnife.bind(this, layout)
            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            //super.onViewCreated(savedInstanceState)
            //setHasOptionsMenu(true)
            /*
            val adapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
                activity!!,
                R.layout.simple_spinner_dropdown_item,
                SUPPORTED_LOCALES as List<Any?>
            )
            localeSpinner?.adapter = adapter
            */
            layout.nextbuttonLang.setOnClickListener(){
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId == -1) {
                    Toast.makeText(this.context,"No answer has been selected",Toast.LENGTH_SHORT).show()
                } else {
                    val radioButton = radioGroup.findViewById<View>(selectedId) as RadioButton
                    //LocaleChanger.setLocale(Locale("en", "", ""))
                    //LocaleChanger.setLocale(Locale(radioButton.text.toString(),"india",""))
                    LocaleChanger.setLocale(Locale("hi","IN",""))
                    updateResources(radioButton.text.toString())
                    Toast.makeText(this.context,radioButton.text,Toast.LENGTH_SHORT).show()
                    // TODO findNavController().navigate(R.id.loginFragment)
                    findNavController().navigate(R.id.videoResumeFragment)
                    Toast.makeText(this.context,">>>>"+LocaleChanger.getLocale().language.toString(),Toast.LENGTH_SHORT).show()
                }
            }

            /*layout.localeUpdate.setOnClickListener() {
                Log.d("????????????????????", localeSpinner!!.selectedItem.toString())
                LocaleChanger.setLocale(localeSpinner!!.selectedItem as Locale)
                updateResources(localeSpinner!!.selectedItem.toString())
                findNavController().navigate(R.id.loginFragment)
            }*/

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
        Locale.setDefault(locale)
        val configuration =
            context!!.resources.configuration
        configuration.setLocale(locale)
    }
/*
    override fun onResume() {
            super.onResume()
            currentLocale?.text = Locale.getDefault().toString()
            date?.setText(DateProvider.provideSystemLocaleFormattedDate())
        }

        @OnClick(R.id.localeUpdate)
        fun onUpdateLocaleClick() {
            Log.d("????????????????????",localeSpinner!!.selectedItem.toString())
            LocaleChanger.setLocale(localeSpinner!!.selectedItem as Locale)
//            ActivityRecreationHelper.recreate(activity, false)
        }
        */

/*
        @OnClick(R.id.showDatePicker)
        fun onShowDatePickerClick() {
            val now = Calendar.getInstance()
            val dialog = DatePickerDialog(
                activity!!,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth -> },
                now[Calendar.YEAR],
                now[Calendar.MONTH],
                now[Calendar.DAY_OF_MONTH]
            )
            dialog.show()
        }
*/
        override fun onDestroyView() {
            unbinder?.unbind()
            LocaleChanger.resetLocale()
            super.onDestroyView()
        }
    }