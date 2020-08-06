package com.gigforce.app.modules.gigerid

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.layout_giger_id_fragment.*

class GigerIdFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_giger_id_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        genQrCode()
        initClicks()
    }

    private fun initClicks() {

        tv_gig_date_giger_id.isSelected = true
    }


    fun genQrCode() {

        val content =
            "Hello Gigerssflaj'pgj'lsawdjg[ljawd[gi';lawdjf'jap'oWDUFOPWEufphwE;FHPIhfhew;FH;ewhf;EWH;FHe;fh;PQIEWYF;Ihefoi;hE;OIFH;eyfoi;EYOIFHGOIewfh;EWHFJHGEWfjhgljwEGFLEWTFLEWGFJGWE,ftE2LOBFQ23Y3P9;"

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        iv_qr_code_giger_id.setImageBitmap(bitmap)
    }


}