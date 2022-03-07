package com.gigforce.profile.onboarding.fragments.namegender

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.onboarding.OnFragmentFormCompletionListener
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.name_gender_item.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [NameGenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class NameGenderFragment : Fragment(), OnboardingFragmentNew.FragmentSetLastStateListener,
    OnboardingFragmentNew.FragmentInteractionListener, OnboardingFragmentNew.SetInterfaceListener {
    companion object {
        fun newInstance() = NameGenderFragment()
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    var gender = ""
    private var win: Window? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        changeStatusBarColor()
        return inflater.inflate(R.layout.name_gender_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener()
//        showKeyboard()

    }

    override fun onResume() {
        super.onResume()
        //showKeyboard()
    }

    fun showKeyboard() {
        username?.let {
            it.isFocusableInTouchMode = true
            it.requestFocus()
            val inputMethodManager =
                activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                it.applicationWindowToken,
                InputMethodManager.SHOW_FORCED, 0
            )
        }

    }

    private fun listener() {

        imageTextCardMol.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon, option, imageTextCardMol)
            gender = "Male"
            validateAllValues()
            hideKeyboard()
        })
        imageTextCardMol4.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon1, option1, imageTextCardMol4)
            gender = "Female"
            validateAllValues()
            hideKeyboard()
        })
        imageTextCardMol3.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon2, option2, imageTextCardMol3)
            gender = "Other"
            validateAllValues()
            hideKeyboard()
        })
        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                validateAllValues()

            }
        })

    }

    private fun validateAllValues() {
        if (!gender.equals("") && !username.text.toString().trim().equals("") )  {
            formCompletionListener?.enableDisableNextButton(true)
        } else {
            formCompletionListener?.enableDisableNextButton(false)
        }
    }

    private fun resetAll() {
        resetSelected(icon, option, imageTextCardMol)
        resetSelected(icon1, option1, imageTextCardMol4)
        resetSelected(icon2, option2, imageTextCardMol3)

    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_default_border
                )
            )
        }
    }

    private fun setSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.selected_image_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.selected_text_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_selection_border
                )
            )
        }

    }

    fun hideKeyboard() {
        activity?.let {
            val imm: InputMethodManager =
                it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view: View? = it.currentFocus ?: null
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener?.enableDisableNextButton(true)
        return false
    }

    override fun nextButtonActionFound(): Boolean {
        hideKeyboard()
        var props = HashMap<String, Any>()
        props.put("name", username.text.toString())
        props.put("gender", gender)
        eventTracker.pushEvent(
            TrackingEventArgs(
                OnboardingEvents.EVENT_USER_UPDATED_NAME_GENDER,
                props
            )
        )
        eventTracker.setUserProperty(props)
        eventTracker.setProfileProperty(ProfilePropArgs("\$name", username.text.toString()))
        eventTracker.setProfileProperty(ProfilePropArgs("Gender", gender))
        eventTracker.setUserName(username.text.toString())

        return false
    }

    override fun activeNextButton() {
        validateAllValues()
    }

    private fun changeStatusBarColor() {
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        win?.statusBarColor = resources.getColor(R.color.status_bar_gray)
    }

    var formCompletionListener: OnFragmentFormCompletionListener? = null
    override fun setInterface(onFragmentFormCompletionListener: OnFragmentFormCompletionListener) {
        formCompletionListener = formCompletionListener ?: onFragmentFormCompletionListener
    }
}