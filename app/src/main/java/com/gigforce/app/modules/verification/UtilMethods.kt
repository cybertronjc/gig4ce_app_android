package com.gigforce.app.modules.verification

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.gigforce.app.R
import java.io.ByteArrayOutputStream

object UtilMethods {

    private lateinit var progressDialogBuilder: AlertDialog.Builder
    private lateinit var progressDialog: AlertDialog

    private val TAG: String = "---UtilMethods"

    /**
     * @param context
     * @action show progress loader
     */
    fun showLoading(context: Context){
        progressDialogBuilder = AlertDialog.Builder(context)
        progressDialogBuilder.setCancelable(false) // if you want user to wait for some process to finish,
        progressDialogBuilder.setView(R.layout.layout_loading_dialog)

        progressDialog = progressDialogBuilder.create()
        progressDialog.show()
    }

    /**
     * @action hide progress loader
     */
    fun hideLoading(){
        try {
            progressDialog.dismiss()
        }catch (ex: java.lang.Exception){
            Log.e(TAG, ex.toString())
        }

    }


    /**
     * @param context
     * @action show Long toast message
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    /**
     * @param context
     * @param img
     * @action return the base64 encoded string of an image given as uri
     */
    fun encodeImageToBase64(mContext:Context, img: Bitmap):String{
        val baos = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * @param context
     * @param uri
     * @action return the base64 encoded string of an image given as uri
     */
    fun encodeImageToBase64(mContext:Context, uri: Uri):String{
        val baos = ByteArrayOutputStream()
        val bitmap =  MediaStore.Images.Media.getBitmap(mContext?.contentResolver, uri);//BitmapFactory.decodeResource(resources, uri)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * @param context
     * @return true or false mentioning the device is connected or not
     * @brief checking the internet connection on run time
     */
    fun isConnectedToInternet(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val allNetworks = manager?.allNetworks?.let { it } ?: return false
            allNetworks.forEach { network ->
                val info = manager.getNetworkInfo(network)
                if (info.state == NetworkInfo.State.CONNECTED) return true
            }
        } else {
            val allNetworkInfo = manager?.allNetworkInfo?.let { it } ?: return false
            allNetworkInfo.forEach { info ->
                if (info.state == NetworkInfo.State.CONNECTED) return true
            }
        }
        return false
    }

    /**
     * @param context
     * @param uri
     * @action return the base64 encoded string of an image given as uri
     */
    fun encodeImagesToBase64(mContext:Context, uriFront: Uri, uriBack:Uri):String{
        val baos = ByteArrayOutputStream()
        val bitmapFront =  MediaStore.Images.Media.getBitmap(mContext?.contentResolver, uriFront);
        val bitmapBack =  MediaStore.Images.Media.getBitmap(mContext?.contentResolver, uriBack);
        val bitmap = combineImages(bitmapFront, bitmapBack);
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * @param c
     * @param s
     * @return return c+s
     * @brief concatenating two bitmaps side by side
     */
    fun combineImages(
        c: Bitmap,
        s: Bitmap
    ): Bitmap? { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        var cs: Bitmap? = null
        val width: Int
        var height = 0
        if (c.width > s.width) {
            width = c.width + s.width
            height = c.height
        } else {
            width = s.width + s.width
            height = c.height
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

//        val comboImage = Canvas(cs)
//        comboImage.drawBitmap(c, 0f, 0f, null)
//        comboImage.drawBitmap(s, c.width, 0f, null)

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
        /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/
        return cs
    }

}