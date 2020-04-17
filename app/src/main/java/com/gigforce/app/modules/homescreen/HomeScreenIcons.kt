package com.gigforce.app.modules.homescreen

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.setDarkStatusBarTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_home_screen.*
import kotlinx.android.synthetic.main.layout_home_screen.view.*


class HomeScreenIcons : BaseFragment() {

    //todo
    private lateinit var storage: FirebaseStorage
    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!! //ynLyPDjsBrYgiFT3OWX8Tn8OLjI2 or GigerId1

    private lateinit var docref: DocumentReference;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        return inflateView(R.layout.layout_home_screen, inflater, container)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.setDarkStatusBarTheme(false)
        initialize()
        listener()
        dbCall()
        //topbar.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
    }

    private fun dbCall() {
        uid = "GigerId1";
        firebaseDB.collection("Verification").document(uid).addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->
            if (e != null) {
                Log.w("HomeScreenIcons", "Listen failed", e)
                return@EventListener
            }

            Log.d("HomeScreenIcons", value.toString())
            value?.data?.entries?.forEach { (k,v)->if(k == "bio_kyc_verified"){
            }}
        })
    }

    private fun listener() {
        cardviewkyc.text_kyc.setOnClickListener {
            navigate(R.id.verification)
            //findNavController().navigate(R.id.uploadDropDown)
        }
        cardviewvideoresume.text_kyc_video.setOnClickListener {
            Toast.makeText(context, "TODO CTA: jump to video resume upload page", Toast.LENGTH_SHORT).show()
        }
        button_signout.setOnClickListener {
            showConfirmationDialog()

        }
    }

    private fun showConfirmationDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.signout_custom_alert)
        val titleDialog = dialog?.findViewById(R.id.title) as TextView
        titleDialog.text = "Do you really want to sign out?"
        val yesBtn = dialog?.findViewById(R.id.yes) as TextView
        val noBtn = dialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            dialog?.dismiss()
        }
        noBtn.setOnClickListener { dialog .dismiss() }
        dialog?.show()
    }


    private fun initialize() {
        var gridItems : ArrayList<HSGridItemDataModel> = getGridItems()
        val adapter = this.context?.let { HomeScreenAdapter(it, R.layout.item_gridhomescreen, gridItems) }
        gridview.adapter = adapter
        gridview.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // Write code to perform action when item is clicked.
            if(gridItems.get(position).navigateToFragment!=0)
            navigate(gridItems.get(position).navigateToFragment)
            else
                showToast("TODO CTA ")
        }

    }

    private fun getGridItems(): ArrayList<HSGridItemDataModel> {
        var gridItems : ArrayList<HSGridItemDataModel> = ArrayList<HSGridItemDataModel>()
        gridItems.add(HSGridItemDataModel("Profile",R.drawable.ic_homescreen_profile,R.id.profileFragment))
        gridItems.add(HSGridItemDataModel("Learning",R.drawable.ic_homescreen_learn,0))
        gridItems.add(HSGridItemDataModel("Payment",R.drawable.ic_homescreen_payment,0))
        gridItems.add(HSGridItemDataModel("Search",R.drawable.ic_homescreen_explore,0))
        gridItems.add(HSGridItemDataModel("Chat",R.drawable.ic_homescreen_chat,0))
        gridItems.add(HSGridItemDataModel("Support",R.drawable.ic_homescreen_pref,0))
        gridItems.add(HSGridItemDataModel("Preferences",R.drawable.ic_homescreen_control,0))
        gridItems.add(HSGridItemDataModel("Video Resume",R.drawable.gig4ce_logo,R.id.videoResumeFragment))
	gridItems.add(HSGridItemDataModel("New HomeScreen",R.drawable.gig4ce_logo,R.id.homeScreenNew))
        gridItems.add(HSGridItemDataModel("More",R.drawable.gig4ce_logo,0))
        gridItems.add(HSGridItemDataModel("Prefrences",R.drawable.ic_homescreen_pref,R.id.settingFragment))
        return gridItems
    }
}


/** Algo:
Check the KYC flag of Giger Profile from Profiles collection
check which flag is true or false
if address is false - go to verification
if address is true, aadhaar is false and  all of (dl,voterid,passport) are false - go to aadhaarUpload
if address is true, aadhaar is false and any of (dl,voterid,passport) is true - go to bankUpload
if address and aadhaar are true, bank is false - go to bankUpload
if address and aadhaar are true, bank is true - go to UploadPan
 */
//            docref = firebaseDB.collection("Verification").document(uid);
//            docref.get()
//                .addOnSuccessListener { document ->
//                    if (document != null) {
//                        val items = document["kycVerified"] as HashMap<*, *>
//                        items.forEach { (k, v) ->
//                            Log.d(">>",">>$k = $v");
//                            // Apply the above algorithm here!
//                        }
//                    } else {
//                        Log.d(">>","null doc")
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.d("TAG", "get failed with ", exception)
//                }



//
//            Toast.makeText(context,
//                "TODO CTA:"+value?.data?.keys.toString(), Toast.LENGTH_SHORT).show()

//        firebaseDB.collection("Verification").whereEqualTo(FieldPath.documentId(),"GigerId1").whereEqualTo()

//            value?.data?.keys?.forEach { k-> if(k.equals("bio_kyc_verified")){
//
//                value?.data?.values?.forEach()
//                Toast.makeText(context,
//                    "TODO CTA:"+k[0].toString(), Toast.LENGTH_SHORT).show()
//            } }

//            value?.data?.entries?.forEach { (key, value) -> if(key.equals("bio_kyc_verified")){
//
//                Toast.makeText(context,
//                "TODO CTA: $key = $value", Toast.LENGTH_SHORT).show()
//            }}


/*
              check the kyc flag and video resume flag and accordingly set the card views visibility
               */
//layout.cardviewkyc.visibility
//layout.text_kyc.visibility=View.VISIBLE
