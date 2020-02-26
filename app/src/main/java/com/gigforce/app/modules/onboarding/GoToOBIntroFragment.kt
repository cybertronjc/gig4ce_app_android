package com.gigforce.app.modules.onboarding

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
//import com.gigforce.app.modules.onboarding.adapters.UserDataAdapter
import com.gigforce.app.modules.onboarding.models.UserData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.chat_bottom_modal_sheet.view.*
import kotlinx.android.synthetic.main.chat_bottom_spinner.*
import kotlinx.android.synthetic.main.chat_bottom_spinner.view.*
import kotlinx.android.synthetic.main.fragment_userinfo.view.*
import kotlinx.android.synthetic.main.login_activity.*


class GoToOBIntroFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ob_intro, container, false)
}