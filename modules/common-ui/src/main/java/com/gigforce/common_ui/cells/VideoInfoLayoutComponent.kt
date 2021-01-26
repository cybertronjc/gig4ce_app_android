package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.VideoInfoLayoutDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.google.android.material.button.MaterialButton

class VideoInfoLayoutComponent (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    private val image : ImageView
    private val title : TextView
    private val coreRecyclerView : CoreRecyclerView
    private val loadMore : MaterialButton
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_video_info_card, this, true)
        image = this.findViewById(R.id.image)
        title = this.findViewById(R.id.title)
        coreRecyclerView = this.findViewById(R.id.videos_list)
        loadMore = this.findViewById(R.id.load_more)


    }

    override fun bind(data: Any?) {
        if(data is VideoInfoLayoutDVM){
            if(data.image is String && data.image.contains("http")){
//                GlideApp.with(context)
//                    .load(data.image)
//                    .error(R.drawable.ic_learning_default_back)
//                    .into(img)
            }
            else if(data.image is Int){
                image.setImageResource(data.image)
            }
            else{
                //layout issue if iv is gone
//                img.gone()
            }

            title.text = data.title
            coreRecyclerView.collection = data.allVideos
            loadMore.text = data.loadMore
        }
    }
}