package com.gigforce.client_activation.client_activation

import android.content.Context
import android.graphics.Typeface
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
//import com.gigforce.client_activation.client_activation.models.BulletPoints
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.client_activation.BulletPoints
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.layout_rv_bullet_points.view.*

class AdapterBulletPoints constructor(
    private val fragmentContext: Context
): RecyclerView.Adapter<AdapterBulletPoints.ViewHolder>() {
    var items: List<BulletPoints> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_bullet_points, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bulletPoint = items[position]
        holder.itemView.tv_title.text = bulletPoint.title
        if (!bulletPoint.url.isNullOrEmpty()) {
            holder.itemView.iv_bullets.visible()
            Glide.with(holder.itemView.context).load(bulletPoint.url).placeholder(
                getCircularProgressDrawable(holder.itemView.context)
            ).into(holder.itemView.iv_bullets)
        }else{
            holder.itemView.iv_bullets.gone()
        }
        if (bulletPoint.type == "expanded") {
            setItem(
                bulletPoint.pointsData,
                holder.itemView.tl_bullets,
                bulletPoint.requiredShowPoints,
                bulletPoint.showPoints,
                fragmentContext
            )
        } else if (bulletPoint.type == "collapsed") {
            holder.itemView.setOnClickListener {
                setItem(
                    bulletPoint.faqs?.map { item ->
                        item.htmlString

                    },
                    holder.itemView.tl_bullets,
                    bulletPoint.requiredShowPoints,
                    bulletPoint.showPoints,
                    fragmentContext
                )
            }
        }

        holder.itemView.divider_three.visibility =
            if (position == items.size - 1) View.GONE else View.VISIBLE

    }

    override fun getItemCount(): Int {
        return items.size
    }


    fun addData(items: List<BulletPoints>) {
        this.items = items;
        notifyDataSetChanged()
    }

    fun setItem(
        role: List<String>?,
        layout: TableLayout,
        moreText: Boolean,
        maxPoints: Int,
        context: Context
    ) {
        if (layout.childCount > 0) {
            layout.removeAllViews()
        } else {
            if (moreText) {
                addBulletsTill(
                    0,
                    if (role?.size!! > maxPoints) maxPoints else role.size!! - 1,
                    layout,
                    role,
                    true, context
                )
                if (role.size > maxPoints) {
                    val moreTextView = AppCompatTextView(context)
                    moreTextView.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        14F
                    )
                    moreTextView.setTextColor(context.resources.getColor(R.color.lipstick))
                    moreTextView.text = context.getString(R.string.plus_more_client)
                    val face =
                        Typeface.createFromAsset(context.assets, "fonts/Lato-Regular.ttf")
                    moreTextView.typeface = face
                    moreTextView.setPadding(
                        context.resources.getDimensionPixelSize(R.dimen.size_16),
                        0,
                        0,
                        0
                    )

                    layout.addView(moreTextView)
                    moreTextView.setOnClickListener {
                        layout.removeViewInLayout(moreTextView)
                        addBulletsTill(
                            maxPoints + 1,
                            role.size!! - 1,
                            layout,
                            role,
                            false, context
                        )
                    }
                }
            } else {
                addBulletsTill(
                    0,
                    role?.size!! - 1,
                    layout,
                    role,
                    true,
                    context
                )

            }

        }

    }

    fun addBulletsTill(
        from: Int,
        to: Int,
        layout: TableLayout,
        arr: List<String>?,
        removeAllViews: Boolean,
        context: Context
    ) {
        if (removeAllViews)
            layout.removeAllViews()
        for (i in from..to) {

            val iv = ImageView(context)
            val layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(
                0,
                context.resources.getDimensionPixelSize(R.dimen.font_16),
                context.resources.getDimensionPixelSize(R.dimen.size_8),
                0
            )
            iv.layoutParams = layoutParams
            iv.setImageResource(R.drawable.shape_circle_lipstick)
            val textView = TextView(context)
            val face =
                Typeface.createFromAsset(context.assets, "fonts/Lato-Regular.ttf")
            textView.typeface = face
            val layoutParams1 = TableRow.LayoutParams(
                1000 - (context.resources.getDimensionPixelSize(
                    R.dimen.size_66
                )),
                TableRow.LayoutParams.WRAP_CONTENT
            )

                layoutParams1.setMargins(
                    0,
                    context.resources.getDimensionPixelSize(R.dimen.size_8),
                    0,
                    0
                )


            textView.layoutParams = layoutParams1

            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                14F
            )
            textView.text = Html.fromHtml(arr?.get(i))


            textView.setTextColor(context.resources.getColor(R.color.black))
            val tr = TableRow(context)


            tr.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            tr.addView(iv)
            tr.addView(textView)
            layout.addView(
                tr,
                TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            )

        }
    }
}