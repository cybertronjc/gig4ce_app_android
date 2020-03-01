package com.gigforce.app.modules.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gigforce.app.R
import com.gigforce.app.modules.onboarding.utils.DepthPageTransformer
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.dp
import kotlinx.android.synthetic.main.fragment_intro_slides.*

/**
 * A simple [Fragment] subclass.
 * Use the [IntroSlidesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntroSlidesFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro_slides, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.setupViewPager()
    }

    fun setupViewPager(){
        this.viewpager.adapter = IntroSlidesViewPagerAdapter(this.viewpager)
        this.viewpager.setPageTransformer(DepthPageTransformer())
        this.viewpager.registerOnPageChangeCallback(object:ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDotPresenter()
            }
        })
    }

    fun updateDotPresenter(){
        this.dots_presenter.removeAllViews()
        this.dots_presenter.addView(getDot(this.viewpager.currentItem == 0))
        this.dots_presenter.addView(getDot(this.viewpager.currentItem == 1))
        this.dots_presenter.addView(getDot(this.viewpager.currentItem == 2))
    }

    private fun getDot(selected:Boolean = false):ImageView{
        val image:ImageView = ImageView(context)

        val params = LinearLayout.LayoutParams(14.dp,14.dp)
        params.marginEnd = 6.dp
        image.layoutParams = params

        if(selected)
            image.setImageResource(R.drawable.dottab_indicator_selected)
        else
            image.setImageResource(R.drawable.dottab_indicator_default)
        return image
    }
}

class IntroSlidesViewPagerAdapter(val viewpager:ViewPager2): RecyclerView.Adapter<IntroSlidesViewPagerAdapter.ViewHolder>(){

    class ViewHolder(view:View, val viewpager:ViewPager2):RecyclerView.ViewHolder(view) {

        var mainArtImageView:ImageView
        var titleTextView:TextView
        var subTitleTextView:TextView
        var nextButton:Button

        var currentPosition:Int = -1

        init {
            mainArtImageView = this.itemView.findViewById<ImageView>(R.id.iv_main_art)
            titleTextView = this.itemView.findViewById<TextView>(R.id.tv_title)
            subTitleTextView = this.itemView.findViewById<TextView>(R.id.tv_subtitle)
            nextButton = this.itemView.findViewById<Button>(R.id.btn_next)

            nextButton.setOnClickListener {
                if(currentPosition < 2)
                    viewpager.setCurrentItem(currentPosition+1, true)
            }
        }

        fun Bind(position: Int){
            currentPosition = position
            if(position == 0) {

                GlideApp.with(itemView)
                    .load(R.drawable.ic_intro_slides1_mainart)
                    .into(mainArtImageView)

                titleTextView.setText(R.string.intro_slide1_title)
                subTitleTextView.setText(R.string.intro_slide1_subtitle)
                nextButton.setText("next")

            }else if(position == 1){
                GlideApp.with(itemView)
                    .load(R.drawable.ic_intro_slides2_mainart)
                    .into(mainArtImageView)

                titleTextView.setText(R.string.intro_slide2_title)
                subTitleTextView.setText(R.string.intro_slide2_subtitle)
                nextButton.setText("next")

            }else if(position == 2) {
                GlideApp.with(itemView)
                    .load(R.drawable.ic_intro_slides3_mainart)
                    .into(mainArtImageView)

                titleTextView.setText(R.string.intro_slide3_title)
                subTitleTextView.setText(R.string.intro_slide3_subtitle)
                nextButton.setText("Get Started")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_intro_slide, parent, false)
        return ViewHolder(view, viewpager)
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.Bind(position)
    }
}