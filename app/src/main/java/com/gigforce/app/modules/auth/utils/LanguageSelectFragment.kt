package com.gigforce.app.modules.auth.utils

import com.gigforce.app.R
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.franmontiel.localechanger.LocaleChanger
//import com.franmontiel.localechanger.sample.SampleApplication.SUPPORTED_LOCALES
import com.franmontiel.localechanger.utils.ActivityRecreationHelper
import java.util.*

class LanguageSelectFragment : Fragment(){


    val SUPPORTED_LOCALES =
        Arrays.asList(
            Locale("en", "US"),
            Locale("hi", "IN")
            //Locale("fr", "FR"),
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
        @BindView(com.gigforce.app.R.id.localeSpinner)
        var localeSpinner: Spinner? = null
        @BindView(com.gigforce.app.R.id.currentLocale)
        var currentLocale: TextView? = null
        @BindView(com.gigforce.app.R.id.date)
        var date: TextView? = null
        private var unbinder: Unbinder? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view: View = inflater.inflate(R.layout.fragment_select_language, container, false)
            //LocaleChanger.initialize(getApplicationContext(),SUPPORTED_LOCALES)
            unbinder = ButterKnife.bind(this, view)
            return view
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            //super.onViewCreated(savedInstanceState)
            //setHasOptionsMenu(true)
            val adapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
                activity!!,
                R.layout.simple_spinner_dropdown_item,
                SUPPORTED_LOCALES as List<Any?>
            )
            localeSpinner!!.adapter = adapter
        }

        override fun onResume() {
            super.onResume()
            currentLocale!!.text = Locale.getDefault().toString()
            date?.setText(DateProvider.provideSystemLocaleFormattedDate())
        }

        @OnClick(R.id.localeUpdate)
        fun onUpdateLocaleClick() {
            LocaleChanger.setLocale(localeSpinner!!.selectedItem as Locale)
            ActivityRecreationHelper.recreate(activity, false)
        }

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

        override fun onDestroyView() {
            unbinder?.unbind()
            super.onDestroyView()
        }
    }