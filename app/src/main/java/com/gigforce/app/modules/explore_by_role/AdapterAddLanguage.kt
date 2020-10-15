package com.gigforce.app.modules.explore_by_role

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Language
import com.gigforce.app.utils.addAsteriskHint
import kotlinx.android.synthetic.main.layout_rv_add_language.view.*


class AdapterAddLanguage() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ACTION -> {
            }
            else -> {
                val viewHolderAddLanguage: ViewHolderAddLanguage = holder as ViewHolderAddLanguage
                addAsteriskHint(
                    "#979797",
                    "#e02020",
                    viewHolderAddLanguage.itemView.add_language_name
                )
                viewHolderAddLanguage.itemView.add_language_speaking_level.setOnSeekBarChangeListener(
                    object :
                        SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            viewHolderAddLanguage.itemView.add_language_speaking_level.setThumbText(
                                seekBar?.progress.toString()
                            );
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }

                    })

            }
        }
    }


    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size + 1
    }

    class ViewHolderAddLanguage(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderAction(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ADD_LANGUAGE = 0;
        const val ACTION = 1;
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items?.size) ACTION else ADD_LANGUAGE
    }

    fun addData(items: MutableList<Language>) {
        this.items = items;
        notifyDataSetChanged()
    }


}