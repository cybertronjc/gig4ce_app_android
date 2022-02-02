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
import kotlinx.android.synthetic.main.vaccine_cert_details_component.view.*
import javax.inject.Inject

@AndroidEntryPoint
class VaccineCertDetailsComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder, ICustomClickListener {
    val view: View
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.vaccine_cert_details_component, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.SimpleCardComponent1, 0, 0)
            val title = styledAttributeSet.getString(R.styleable.SimpleCardComponent1_title_scc) ?: ""
            setTitle(title)
        }
    }

    @Inject
    lateinit var navigation : INavigation

    fun setTitle(title: String) {
        vaccineTitle.text = title
    }

    override fun bind(data: Any?) {
        if (data is VaccineCertDetailsDM) {

            data.vaccineLabel?.let {
                setTitle(it)
            }
            if(data.pathOnFirebase?.isNotBlank() == true)
            {
                download_icon.visible()
                edit_icon.visible()
                rightArrow.gone()
            }else{
                download_icon.gone()
                edit_icon.gone()
                rightArrow.visible()
            }

            data.getNavArgs().let {
                it?.let { navData->
                    card_view.setOnClickListener{
                        itemClickListener?.onItemClick(view,0,data)
                        navigation.navigateTo(navData.navPath,args = navData.args)
                    }
                }
            }

        }
    }

    var itemClickListener : ItemClickListener?=null

    override fun onClickListener(itemClickListener: ItemClickListener?) {
        this.itemClickListener = itemClickListener
    }
}