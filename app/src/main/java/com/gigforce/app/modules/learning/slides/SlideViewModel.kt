package com.gigforce.app.modules.learning.slides

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gigforce.app.R

data class Slide(
    val image : Int,
    val title : String,
    val content : String
)

class SlideViewModel : ViewModel() {

    val slide1 = Slide(
        image = R.drawable.bg_user_learning,
        title = "Title 1",
        content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting"
    )

    val slide2 = Slide(
        image = R.drawable.brista_ls_img,
        title = "Title 2",
        content = "Curabitur vitae velit feugiat lacus faucibus rutrum. Sed ultrices ultricies nisl, eu tincidunt mauris lacinia non. Nunc in lorem eu felis tincidunt luctus a a quam. Donec sit amet molestie purus, sit amet iaculis urna. Etiam bibendum pulvinar sem vitae varius. Sed sit amet maximus nibh. Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    )

    val slide3 = Slide(
        image = R.drawable.bg_user_learning,
        title = "Title 3",
        content = "Nulla facilisis, augue eget interdum lobortis, sapien tortor cursus massa, id blandit eros lectus nec libero. Vivamus egestas efficitur rhoncus. In dignissim, mi eu pretium feugiat, velit mauris mattis eros, vitae placerat dolor ante in nulla. Vestibulum convallis varius mi placerat pharetra"
    )


    val slide4 = Slide(
        image = R.drawable.brista_ls_img,
        title = "Title 4",
        content = "Morbi consequat tellus non lorem laoreet pharetra. Vivamus posuere est ligula, eu interdum purus sagittis facilisis. Curabitur accumsan, nulla eu faucibus ultrices, mi tortor pulvinar ante, vehicula rhoncus lectus orci ut est. In tincidunt sapien nec euismod ultrices. Curabitur commodo ultricies leo ac dictum. Donec facilisis pulvinar vulputate."
    )


    val slidesData = listOf(slide1,slide2,slide3,slide4)


}