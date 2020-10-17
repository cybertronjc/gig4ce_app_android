package com.gigforce.app.modules.explore_by_role

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Language
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.ThumbTextSeekBar
import com.gigforce.app.utils.addAsteriskHint
import kotlinx.android.synthetic.main.layout_next_add_profile_segments.view.*
import kotlinx.android.synthetic.main.layout_rv_add_language.view.*


class AdapterAddLanguage() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adapterAddLanguageCallbacks: AdapterAddLanguageCallbacks? = null
    private var items: MutableList<Language>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ACTION -> ViewHolderAction(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_next_add_profile_segments, parent, false)
            )
            else ->
                ViewHolderAddLanguage(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_add_language, parent, false)
                )
        }


    }

    fun setCallbacks(adapterAddLanguageCallbacks: AdapterAddLanguageCallbacks) {
        this.adapterAddLanguageCallbacks = adapterAddLanguageCallbacks
    }

    interface AdapterAddLanguageCallbacks {
        fun submitClicked(items: MutableList<Language>)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ACTION -> {
                val viewholder: ViewHolderAction = holder as ViewHolderAction
                PushDownAnim.setPushDownAnimTo(viewholder.itemView.tv_action).setOnClickListener(
                    View.OnClickListener {

                        adapterAddLanguageCallbacks?.submitClicked(items!!)
                    })
            }
            else -> {
                val language = items?.get(position)
                val viewHolderAddLanguage: ViewHolderAddLanguage = holder as ViewHolderAddLanguage
                viewHolderAddLanguage.itemView.add_language_name.hint =
                    holder.itemView.resources.getString(
                        R.string.language
                    )
                viewHolderAddLanguage.itemView.add_language_name.setText(language?.name)
                viewHolderAddLanguage.itemView.mother_language.isChecked =
                    language?.isMotherLanguage ?: false
                addAsteriskHint(
                    "#979797",
                    "#e02020",
                    viewHolderAddLanguage.itemView.add_language_name
                )
                viewHolderAddLanguage.itemView.add_language_name.setText(language?.name)
                viewHolderAddLanguage.itemView.add_language_name.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (viewHolderAddLanguage.adapterPosition == -1) return
                        items?.get(viewHolderAddLanguage.adapterPosition)?.name = s.toString()
                    }
                })
                setSeekBarListener(
                    viewHolderAddLanguage.itemView.add_language_speaking_level,
                    SPEAKING_LEVEL,
                    viewHolderAddLanguage
                )

                viewHolderAddLanguage.itemView.add_language_speaking_level.setProgress(
                    language?.speakingSkill?.toInt() ?: 0
                )
                setSeekBarListener(
                    viewHolderAddLanguage.itemView.arround_current_add_seekbar,
                    WRITING_LEVEL,
                    viewHolderAddLanguage
                )
                viewHolderAddLanguage.itemView.arround_current_add_seekbar.setProgress(
                    language?.writingSkill?.toInt() ?: 0
                )
                viewHolderAddLanguage.itemView.mother_language.setOnClickListener {
                    if (viewHolderAddLanguage.adapterPosition == -1) return@setOnClickListener
                    items?.get(viewHolderAddLanguage.adapterPosition)?.isMotherLanguage =
                        viewHolderAddLanguage.itemView.mother_language.isChecked
                }
                viewHolderAddLanguage.itemView.add_language_add_more.visibility =
                    if (position == items?.size?.minus(1) ?: false || items?.size == 1) View.VISIBLE else View.GONE
                viewHolderAddLanguage.itemView.add_language_add_more.setOnClickListener {
                    if (viewHolderAddLanguage.adapterPosition == -1) return@setOnClickListener
                    items?.add(Language())
                    notifyItemInserted(viewHolderAddLanguage.adapterPosition + 1)
                    notifyItemChanged(viewHolderAddLanguage.adapterPosition)
                }
                viewHolderAddLanguage.itemView.remove_language.visibility =
                    if (position != 0) View.VISIBLE else View.GONE
                viewHolderAddLanguage.itemView.remove_language.setOnClickListener {
                    if (viewHolderAddLanguage.adapterPosition == -1) return@setOnClickListener
                    items?.removeAt(viewHolderAddLanguage.adapterPosition)
                    notifyItemRemoved(viewHolderAddLanguage.adapterPosition)
                    items?.size?.minus(1)?.let { it1 -> notifyItemChanged(it1) }
                }
                if (language?.validateFields == true) {
                    validate(viewHolderAddLanguage)
                }
            }
        }

    }

    private fun validate(viewholder: ViewHolderAddLanguage) {
        viewholder.itemView.form_error.visibility =
            if (viewholder.itemView.add_language_name.text.isEmpty()) View.VISIBLE else View.GONE
        viewholder.itemView.name_line.setBackgroundColor(
            if (viewholder.itemView.add_language_name.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )

    }

    private fun setSeekBarListener(
        thumbTextSeekbar: ThumbTextSeekBar,
        seekBarType: Int,
        viewholder: ViewHolderAddLanguage
    ) {
        thumbTextSeekbar.setOnSeekBarChangeListener(
            object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (viewholder.adapterPosition == -1) return
                    if (seekBarType == SPEAKING_LEVEL) {
                        items?.get(viewholder.adapterPosition)?.speakingSkill = progress.toString()
                    } else {
                        items?.get(viewholder.adapterPosition)?.writingSkill = progress.toString()

                    }

                    thumbTextSeekbar.setThumbText(
                        when {
                            progress < 25 -> thumbTextSeekbar.context.resources.getString(R.string.low)
                            progress < 50 -> thumbTextSeekbar.context.resources.getString(
                                R.string.moderately_low
                            )
                            progress < 75 -> thumbTextSeekbar.context.resources.getString(
                                R.string.moderate_add_language
                            )
                            else -> thumbTextSeekbar.context.resources.getString(
                                R.string.high
                            )
                        }
                    );
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
    }


    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size + 1
    }

    class ViewHolderAddLanguage(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderAction(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ADD_LANGUAGE = 0;
        const val ACTION = 1;
        const val SPEAKING_LEVEL = 2;
        const val WRITING_LEVEL = 3;


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items?.size) ACTION else ADD_LANGUAGE
    }

    fun addData(items: MutableList<Language>) {
        this.items = items;
        notifyDataSetChanged()
    }


}