package com.gigforce.profile.onboarding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.models.ViewType
import kotlinx.android.synthetic.main.experience_item.view.*
import kotlinx.android.synthetic.main.highest_qualification_item.view.*
import kotlinx.android.synthetic.main.name_gender_item.view.*
import kotlinx.android.synthetic.main.name_gender_item.view.icon
import kotlinx.android.synthetic.main.name_gender_item.view.icon1
import kotlinx.android.synthetic.main.name_gender_item.view.icon2
import kotlinx.android.synthetic.main.name_gender_item.view.imageTextCardMol
import kotlinx.android.synthetic.main.name_gender_item.view.imageTextCardMol3
import kotlinx.android.synthetic.main.name_gender_item.view.imageTextCardMol4
import kotlinx.android.synthetic.main.name_gender_item.view.option
import kotlinx.android.synthetic.main.name_gender_item.view.option1
import kotlinx.android.synthetic.main.name_gender_item.view.option2

class MultiviewsAdapter(
    context: Context,
    val list: ArrayList<Any>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val NameGenderVT = 1
        const val AgeGroupDMVT = 3
        const val HighestQualificationVT = 5
        const val ExperienceVT = 7
        const val DeliveryExecutiveExperienceVT = 9

        const val CurrentlyWorkingVT = 11
        const val WorkingDaysVT = 13
        const val TimingVT = 15

        const val InterestVT = 17
    }

    fun validateScreen(firstVisibleItem: Int): Boolean {

        return false
    }

    private val context: Context = context

    private fun setSelected(icon: ImageView, option: TextView, view: View) {
        icon.setColorFilter(ContextCompat.getColor(context, R.color.selected_image_color))
        option.setTextColor(ContextCompat.getColor(context, R.color.selected_text_color))
        view.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.option_selection_border
            )
        )
    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        icon.setColorFilter(ContextCompat.getColor(context, R.color.default_color))
        option.setTextColor(ContextCompat.getColor(context, R.color.default_color))
        view.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.option_default_border
            )
        )
    }

    private inner class NameGenderVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            itemView.imageTextCardMol.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon, itemView.option, itemView.imageTextCardMol)
            })
            itemView.imageTextCardMol4.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon1, itemView.option1, itemView.imageTextCardMol4)
            })
            itemView.imageTextCardMol3.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon2, itemView.option2, itemView.imageTextCardMol3)
            })

        }

        private fun resetAll() {
            resetSelected(itemView.icon, itemView.option, itemView.imageTextCardMol)
            resetSelected(itemView.icon1, itemView.option1, itemView.imageTextCardMol4)
            resetSelected(itemView.icon2, itemView.option2, itemView.imageTextCardMol3)

        }
    }

    private inner class AgeGroupDMVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {

        }


    }

    private inner class HighestQualificationVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            itemView.imageTextCardMol.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon, itemView.option, itemView.imageTextCardMol)
            })
            itemView.imageTextCardMol4.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon1, itemView.option1, itemView.imageTextCardMol4)
            })
            itemView.imageTextCardMol3.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon2, itemView.option2, itemView.imageTextCardMol3)
            })
            itemView.imageTextCardMol_.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon_, itemView.option_, itemView.imageTextCardMol_)
            })
            itemView.imageTextCardMol4_.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon1_, itemView.option1_, itemView.imageTextCardMol4_)
            })
            itemView.imageTextCardMol3_.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon2_, itemView.option2_, itemView.imageTextCardMol3_)
            })

        }
        private fun resetAll() {
            resetSelected(itemView.icon, itemView.option, itemView.imageTextCardMol)
            resetSelected(itemView.icon1, itemView.option1, itemView.imageTextCardMol4)
            resetSelected(itemView.icon2, itemView.option2, itemView.imageTextCardMol3)
            resetSelected(itemView.icon_, itemView.option_, itemView.imageTextCardMol_)
            resetSelected(itemView.icon1_, itemView.option1_, itemView.imageTextCardMol4_)
            resetSelected(itemView.icon2_, itemView.option2_, itemView.imageTextCardMol3_)
        }
    }

    private inner class ExperienceVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            itemView.imageTextCardMol.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon, itemView.option,itemView.imageTextCardMol)
            })
            itemView.imageTextCardMol3.setOnClickListener(View.OnClickListener {
                resetAll()
                setSelected(itemView.icon2,itemView.option2,itemView.imageTextCardMol3)
            })

        }
        private fun resetAll() {
            resetSelected(itemView.icon, itemView.option, itemView.imageTextCardMol)
            resetSelected(itemView.icon2,itemView.option2,itemView.imageTextCardMol3)
        }
    }

    private inner class DeliveryExecutiveExperienceVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
        }
    }

    private inner class CurrentlyWorkingVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {

        }
    }

    private inner class WorkingDaysVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
        }
    }

    private inner class TimingVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
        }
    }

    private inner class InterestVH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == NameGenderVT) {
            return NameGenderVH(
                LayoutInflater.from(context).inflate(R.layout.name_gender_item, parent, false)
            )
        } else if (viewType == AgeGroupDMVT) {
            return AgeGroupDMVH(
                LayoutInflater.from(context).inflate(R.layout.age_group_item, parent, false)
            )
        } else if (viewType == HighestQualificationVT) {
            return HighestQualificationVH(
                LayoutInflater.from(context)
                    .inflate(R.layout.highest_qualification_item, parent, false)
            )
        } else if (viewType == ExperienceVT) {
            return ExperienceVH(
                LayoutInflater.from(context).inflate(R.layout.experience_item, parent, false)
            )
        } else if (viewType == DeliveryExecutiveExperienceVT) {
            return DeliveryExecutiveExperienceVH(
                LayoutInflater.from(context)
                    .inflate(R.layout.experience_item, parent, false)
            )
        } else if (viewType == CurrentlyWorkingVT) {
            return CurrentlyWorkingVH(
                LayoutInflater.from(context)
                    .inflate(R.layout.experience_item, parent, false)
            )
        } else if (viewType == WorkingDaysVT) {
            return WorkingDaysVH(
                LayoutInflater.from(context)
                    .inflate(R.layout.experience_item, parent, false)
            )
        } else if (viewType == TimingVT) {
            return TimingVH(
                LayoutInflater.from(context).inflate(R.layout.experience_item, parent, false)
            )
        } else { //(viewType == InterestVT)
            return InterestVH(
                LayoutInflater.from(context).inflate(R.layout.interest_item, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        list.get(position).let {
            if (it is ViewType) {
                if (it.viewType === NameGenderVT) {
                    (holder as NameGenderVH).bind(position)
                } else if (it.viewType === AgeGroupDMVT) {
                    (holder as AgeGroupDMVH).bind(position)
                } else if (it.viewType === HighestQualificationVT) {
                    (holder as HighestQualificationVH).bind(position)
                } else if (it.viewType === ExperienceVT) {
                    (holder as ExperienceVH).bind(position)
                } else if (it.viewType === DeliveryExecutiveExperienceVT) {
                    (holder as DeliveryExecutiveExperienceVH).bind(position)
                } else if (it.viewType === CurrentlyWorkingVT) {
                    (holder as CurrentlyWorkingVH).bind(position)
                } else if (it.viewType === WorkingDaysVT) {
                    (holder as WorkingDaysVH).bind(position)
                } else if (it.viewType === TimingVT) {
                    (holder as TimingVH).bind(position)
                } else if (it.viewType === InterestVT) {
                    (holder as InterestVH).bind(position)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        list[position].let {
            if (it is ViewType)
                return it.viewType
        }
        return -1
    }
}