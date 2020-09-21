package com.gigforce.app.modules.referrals.models

import android.util.Log
import java.net.URLEncoder

class LinkBuilder {
    private var domain: String? = null
    private var link: String? = null
    private var apn: String? = null
    private var amv: String? = null
    private var ibi: String? = null
    private var imv: String? = null
    private var isi: String? = null
    private var st: String? = null
    private var sd: String? = null
    private var si: String? = null
    fun getURLEncode(input: String): String {
        try {
            return URLEncoder.encode(input, "UTF-8")
        } catch (ex: Exception) {
            Log.e("Link Builder", ex.message)
        }
        return input
    }

    fun setDomain(domain: String?): LinkBuilder {
        this.domain = domain
        return this
    }

    fun setLink(link: String): LinkBuilder {
        this.link = getURLEncode(link)
        return this
    }

    fun setApn(apn: String?): LinkBuilder {
        this.apn = apn
        return this
    }

    fun setAmv(amv: String?): LinkBuilder {
        this.amv = amv
        return this
    }

    fun setIbi(ibi: String?): LinkBuilder {
        this.ibi = ibi
        return this
    }

    fun setImv(imv: String?): LinkBuilder {
        this.imv = imv
        return this
    }

    fun setIsi(isi: String?): LinkBuilder {
        this.isi = isi
        return this
    }

    fun setSt(st: String): LinkBuilder {
        this.st = getURLEncode(st)
        return this
    }

    fun setSd(sd: String): LinkBuilder {
        this.sd = getURLEncode(sd)
        return this
    }

    fun setSi(si: String): LinkBuilder {
        this.si = getURLEncode(si)
        return this
    }

    fun build(): String {
        return String.format(
            "https://%s/?link=%s&apn=%s&amv=%s&ibi=%s&imv=%s&isi=%s&st=%s&sd=%s&si=%s"
            , domain, link, apn, amv, ibi, imv, isi, st, sd, si
        )
    }
}