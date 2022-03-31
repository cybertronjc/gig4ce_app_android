package com.gigforce.verification.mainverification.vaccine.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.core.ICustomClickListener
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.vaccine_certificate_component_layout.view.*
import javax.inject.Inject

@AndroidEntryPoint
class VaccineCertDetailsComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder, ICustomClickListener {
    val view: View

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context)
            .inflate(R.layout.vaccine_certificate_component_layout, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.SimpleCardComponent1, 0, 0)
            val title =
                styledAttributeSet.getString(R.styleable.SimpleCardComponent1_title_scc) ?: ""
            setTitle(title)
        }
    }

    @Inject
    lateinit var navigation: INavigation

    fun setTitle(title: String) {
        tv_title.text = title
    }

    fun setSubtitle(subtitle: String) {
        tv_desc.text = subtitle
    }

    override fun bind(data: Any?) {
        if (data is VaccineCertDetailsDM) {

            data.vaccineLabel?.let {
                setTitle(it)
            }
            data?.vaccineId?.let {
                if (it.equals("vaccine1")){
                    setSubtitle(resources.getString(R.string.vaccine_1_subtitle_veri))
                    cv_top.setBackgroundColor(resources.getColor(R.color.light_pink_veri))
                    image.setImageDrawable(resources.getDrawable(R.drawable.ic_vaccine_dose_1))
                } else if (it.equals("vaccine2")) {
                    setSubtitle(resources.getString(R.string.vaccine_2_subtitle_veri))
                    cv_top.setBackgroundColor(resources.getColor(R.color.light_blue_veri))
                    image.setImageDrawable(resources.getDrawable(R.drawable.ic_vaccine_dose_2))
                } else if (it.equals("vaccine3")) {
                    setSubtitle(resources.getString(R.string.vaccine_3_subtitle_veri))
                    cv_top.setBackgroundColor(resources.getColor(R.color.light_green_veri))
                    image.setImageDrawable(resources.getDrawable(R.drawable.ic_booster_dose))
                }
            }
            if (data.pathOnFirebase?.isNotBlank() == true) {
                download_icon.visible()
                edit_icon.visible()
                primary_action.gone()

                download_icon.setOnClickListener {
                    try {
                        itemClickListener?.onItemClick(it, -1, data)
                    }catch (e:Exception){

                    }
                }
            } else {
                download_icon.gone()
                edit_icon.gone()
                primary_action.visible()
            }

            primary_action.setOnClickListener { it1 ->
                try {
                    itemClickListener?.onItemClick(it1, 0, data)
                }catch (e:Exception){
                }
            }

//            data.getNavArgs().let {
//                it?.let { navData ->
//                    primary_action.setOnClickListener { it1 ->
//                        try {
//                            itemClickListener?.onItemClick(it1, 0, data)
//                            navigation.navigateTo(navData.navPath, args = navData.args)
//                        }catch (e:Exception){
//                        }
//                    }
//                }
//            }

        }
    }

    var itemClickListener: ItemClickListener? = null

    override fun onClickListener(itemClickListener: ItemClickListener?) {
        this.itemClickListener = itemClickListener
    }


}