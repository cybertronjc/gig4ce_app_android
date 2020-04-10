package com.gigforce.app.modules.homescreen

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.auth.utils.SignoutTask
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.verification.VeriFirebaseRepository
import com.gigforce.app.utils.setDarkStatusBarTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_home_screen.*
import kotlinx.android.synthetic.main.layout_home_screen.view.*


class HomeScreenIcons : Fragment() {

    //todo
    private lateinit var storage: FirebaseStorage
    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!! //ynLyPDjsBrYgiFT3OWX8Tn8OLjI2 or GigerId1
    private lateinit var layout: View

    private lateinit var docref: DocumentReference;
    private val itemList: Array<String>
        get() = arrayOf("Profile", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12")


    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            /*
                          check the kyc flag and video resume flag and accordingly set the card views visibility
                           */
        //layout.cardviewkyc.visibility
            //layout.text_kyc.visibility=View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        // get instance of the firebase storage
        storage = FirebaseStorage.getInstance()
        layout = inflater.inflate(R.layout.layout_home_screen, container, false)

        return layout
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.setDarkStatusBarTheme(false)
        val gridview = layout.findViewById<GridView>(R.id.gridview)

        val adapter = this.context?.let { HomeScreenAdapter(it, R.layout.item_gridhomescreen, itemList) }
        gridview.adapter = adapter

        //topbar.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        cardviewkyc.text_kyc.setOnClickListener {

            /** Algo:
                    Check the KYC flag of Giger Profile from Profiles collection
                    check which flag is true or false
                    if address is false - go to verification
                    if address is true, aadhaar is false and  all of (dl,voterid,passport) are false - go to aadhaarUpload
                    if address is true, aadhaar is false and any of (dl,voterid,passport) is true - go to bankUpload
                    if address and aadhaar are true, bank is false - go to bankUpload
                    if address and aadhaar are true, bank is true - go to UploadPan
             */
            docref = firebaseDB.collection("Verification").document(uid);
            docref.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val items = document["kycVerified"] as HashMap<*, *>
                        items.forEach { (k, v) ->
                            Log.d(">>",">>$k = $v");
                            // Apply the above algorithm here!
                        }
                    } else {
                        Log.d(">>","null doc")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
            findNavController().navigate(R.id.verification)
            //findNavController().navigate(R.id.uploadDropDown)
        //    Toast.makeText(context, "TODO CTA: jump to kyc docs upload page", Toast.LENGTH_SHORT).show()
        }
        cardviewvideoresume.text_kyc_video.setOnClickListener {
            Toast.makeText(context, "TODO CTA: jump to video resume upload page", Toast.LENGTH_SHORT).show()
        }

        gridview.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // Write code to perform action when item is clicked.

            when (position) {
                0 -> {findNavController().navigate(R.id.profileFragment)}
                1 -> {
                //    findNavController().navigate(R.id.profileFragment)
                    Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    //findNavController().navigate(R.id.profileFragment)
                    Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
                7 -> {
                    findNavController().navigate(R.id.videoResumeFragment)
                    //Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
            }

            if(position==0){

            }
        }

        layout.button_signout.setOnClickListener { FirebaseAuth.getInstance().signOut() }
        Log.d(">>>>>>>>>>>",uid);
        uid = "GigerId1";


//        firebaseDB.collection("Verification").whereEqualTo(FieldPath.documentId(),"GigerId1").whereEqualTo()

        firebaseDB.collection("Verification").document(uid).addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->
            if (e != null) {
                Log.w("HomeScreenIcons", "Listen failed", e)
                return@EventListener
            }

            Log.d("HomeScreenIcons", value.toString())
//
//            Toast.makeText(context,
//                "TODO CTA:"+value?.data?.keys.toString(), Toast.LENGTH_SHORT).show()

            value?.data?.entries?.forEach { (k,v)->if(k == "bio_kyc_verified"){
//                Toast.makeText(context,"TODO CTA: $v", Toast.LENGTH_SHORT).show()
            }}

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




        })
    }
}