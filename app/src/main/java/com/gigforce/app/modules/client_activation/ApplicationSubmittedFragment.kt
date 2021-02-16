package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_application_submitted.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ApplicationSubmittedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ApplicationSubmittedFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_application_submitted, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pending_tv.text = Html.fromHtml("Application check : <b>Pending</b>")
    }


}