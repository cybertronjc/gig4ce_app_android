package com.gigforce.app.core.base

import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.CoreConstants
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.utils.popAllBackStates

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [BaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
abstract class BaseFragment : Fragment() {
//    abstract fun Activate(fragmentView: View?)
    var mView: View? = null
    open fun activate(view:View?){}

    private lateinit var navController: NavController

    open fun inflateView(
        resource: Int, inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        mView = inflater.inflate(resource, container, false)
        init()
        activate(mView)
        return mView
    }
    lateinit var SP: SharedPreferences
    // SP = this.getPreferences(Context.MODE_PRIVATELD_WRITEABLE);
    var editor: SharedPreferences.Editor? = null
    private fun init() { // GPS=new GPSTracker(this);
        navController = activity?.findNavController(R.id.nav_fragment)!!
        SP = activity?.getSharedPreferences(CoreConstants.SHARED_PREFERENCE_DB, 0)!!
        this.editor = SP.edit()

    }
    fun getFragmentView():View{
        return mView!!
    }
    open fun saveSharedData(Key: String?, Value: String?): Boolean {
        return try {
            editor?.putString(Key, Value)
            editor?.commit()
            true
        } catch (ex: Exception) {
            Log.e("Error:", ex.toString())
            false
        }
    }

    // for delete


    open fun getSharedData(key: String?, defValue: String?): String? {
        return SP.getString(key, defValue)
    }

    open fun showToast(Message: String) {
        if (isNullOrWhiteSpace(Message)) return
        val toast = Toast.makeText(context, Message, Toast.LENGTH_SHORT)
        toast.show()
    }

    // Toast Message
    open fun isNullOrWhiteSpace(str: String): Boolean {
        return if (TextUtils.isEmpty(str)) true else if (str.startsWith("null")) true else false
    }

    open fun showToastLong(Message: String, Duration: Int) {
        if (isNullOrWhiteSpace(Message)) return
        val toast = Toast.makeText(context, Message, Toast.LENGTH_LONG)
        toast.show()
    }
    open fun navigate(
        @IdRes resId: Int, args: Bundle?,
        navOptions: NavOptions?
    ) {
        navController
            .navigate(resId, null, navOptions)
    }
    fun navigate(@IdRes resId: Int){
        navController.navigate(resId)
    }

    fun navigateWithAllPopupStack(@IdRes resId: Int){
        popAllBackStates()
        navigate(resId)
    }

    fun popAllBackStates(){
    navController.popAllBackStates()
    }

    fun findViewById(id: Int): View? {
        return this.mView!!.findViewById(id)
    }

    fun getTextView(view : PFRecyclerViewAdapter<Any?>.ViewHolder, id:Int):TextView{
        return view.getView(id) as TextView
    }

    fun getTextView(view : View, id:Int):TextView{
        return view.findViewById(id) as TextView
    }

    fun getImageView(view : PFRecyclerViewAdapter<Any?>.ViewHolder, id:Int):ImageView{
        return view.getView(id) as ImageView
    }

    fun getImageView(view : View, id:Int):ImageView{
        return view.findViewById(id) as ImageView
    }

    fun getView(view : View, id:Int):View{
        return view.findViewById(id)
    }

    open fun getView(view : PFRecyclerViewAdapter<Any?>.ViewHolder, id:Int):View{
        return view.getView(id)
    }

    fun setTextViewColor(textView:TextView,color:Int){
        textView.setTextColor(ContextCompat.getColor(activity!!.applicationContext, color))
    }

    fun setTextViewSize(textView:TextView,size:Float){
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,size)
    }

    fun setViewBackgroundColor(view:View,color:Int){
        view.setBackgroundColor(ContextCompat.getColor(activity!!.applicationContext, color))
    }
}