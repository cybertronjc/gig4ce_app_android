package com.gigforce.app.modules.preferences.language

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.utils.AppConstants
import kotlinx.android.synthetic.main.fragment_language_preferences.*
import kotlinx.android.synthetic.main.fragment_select_language.*
import kotlinx.android.synthetic.main.fragment_select_language.groupradio
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [LanguagePreferencesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LanguagePreferencesFragment : BaseFragment() {
    companion object {
        fun newInstance() = LanguagePreferencesFragment()
    }
    private lateinit var viewModel: SharedPreferenceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.fragment_language_preferences, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializer()
        setDefaultLanguage()
        listener()
    }

    private fun listener() {
        groupradio.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radioButton = groupradio.findViewById<RadioButton>(checkedId)
                val lang = radioButton.hint.toString()
                updateResources(lang)
                saveSharedData(AppConstants.APP_LANGUAGE, lang)
                saveSharedData(AppConstants.APP_LANGUAGE_NAME,radioButton.text.toString())
                viewModel.saveLanguageToFirebase(radioButton.text.toString(),radioButton.hint.toString())
            })
        imageView10.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
    }

    private fun initializer() {
        groupradio.clearCheck()
    }
    private fun setDefaultLanguage() {
        val lang = getSharedData(AppConstants.APP_LANGUAGE, null)
        when(lang){
            "en" -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
            "hi" -> groupradio.findViewById<RadioButton>(R.id.hi).isChecked = true
            "te" -> groupradio.findViewById<RadioButton>(R.id.te).isChecked = true
            "gu" -> groupradio.findViewById<RadioButton>(R.id.gu).isChecked = true
            "pa" -> groupradio.findViewById<RadioButton>(R.id.pu).isChecked = true
            "fr" -> groupradio.findViewById<RadioButton>(R.id.fr).isChecked = true
            "te" -> groupradio.findViewById<RadioButton>(R.id.te).isChecked = true
            "mr" -> groupradio.findViewById<RadioButton>(R.id.mr).isChecked = true
            else -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
        }

    }
}