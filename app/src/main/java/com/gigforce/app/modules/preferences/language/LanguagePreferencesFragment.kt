package com.gigforce.app.modules.preferences.language

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.configrepository.ConfigViewModel
import kotlinx.android.synthetic.main.fragment_language_preferences.*
import kotlinx.android.synthetic.main.fragment_select_language.groupradio


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
    private val appConfigViewModel : ConfigViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.fragment_language_preferences, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SharedPreferenceViewModel::class.java)
        initializer()
        setDefaultLanguage()
        listener()
        initViewModel()
    }


    private fun initViewModel() {
        appConfigViewModel
            .activeLanguages
            .observe(viewLifecycleOwner, Observer {activeLangs ->

                activeLangs.forEach {

                    when (it) {
                        "en" -> groupradio.findViewById<RadioButton>(R.id.en).isEnabled = true
                        "hi" -> groupradio.findViewById<RadioButton>(R.id.hi).isEnabled = true
                        "kn" -> groupradio.findViewById<RadioButton>(R.id.kn).isEnabled = true
                        "te" -> groupradio.findViewById<RadioButton>(R.id.te).isEnabled = true
                        "gu" -> groupradio.findViewById<RadioButton>(R.id.gu).isEnabled = true
                        "pa" -> groupradio.findViewById<RadioButton>(R.id.pu).isEnabled = true
                        "fr" -> groupradio.findViewById<RadioButton>(R.id.fr).isEnabled = true
                        "mr" -> groupradio.findViewById<RadioButton>(R.id.mr).isEnabled = true
                    }
                }
        })
    }

    private fun listener() {
        groupradio.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radioButton = groupradio.findViewById<RadioButton>(checkedId)
                val lang = radioButton.hint.toString()
                updateResources(lang)
                saveAppLanuageCode(lang)
                saveAppLanguageName(radioButton.text.toString())
                viewModel.saveLanguageToFirebase(radioButton.text.toString(),radioButton.hint.toString())
            })
        back_arrow_iv.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
    }

    private fun initializer() {
        groupradio.clearCheck()
    }
    private fun setDefaultLanguage() {
        val lang = getAppLanguageCode()
        when(lang){
            "en" -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
            "hi" -> groupradio.findViewById<RadioButton>(R.id.hi).isChecked = true
            "kn" -> groupradio.findViewById<RadioButton>(R.id.kn).isEnabled = true
            "te" -> groupradio.findViewById<RadioButton>(R.id.te).isChecked = true
            "gu" -> groupradio.findViewById<RadioButton>(R.id.gu).isChecked = true
            "pa" -> groupradio.findViewById<RadioButton>(R.id.pu).isChecked = true
            "fr" -> groupradio.findViewById<RadioButton>(R.id.fr).isChecked = true
            "mr" -> groupradio.findViewById<RadioButton>(R.id.mr).isChecked = true
            else -> groupradio.findViewById<RadioButton>(R.id.en).isChecked = true
        }
    }
}