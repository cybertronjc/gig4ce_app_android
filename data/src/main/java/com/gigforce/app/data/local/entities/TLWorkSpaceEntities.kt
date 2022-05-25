package com.gigforce.app.data.local.entities

import androidx.room.*
import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import java.time.LocalDateTime

@Entity(tableName = CachedTLWorkSpaceSectionModel.TABLE_NAME)
data class CachedTLWorkSpaceSectionModel(

    @PrimaryKey()
    @ColumnInfo(name = COLUMN_ID)
    val sectionId : String,

    @ColumnInfo(name = "index")
    val index: Int? = null,

//    @Embedded(prefix = "filter")
//    val filters: List<CachedFiltersItemModel>? = null,

    @ColumnInfo(name = "title")
    val title: String? = null,

//    @Embedded
//    val items: List<SectionItemApiModel>? = null,
//
//    @Embedded
//    val upcomingGigers : List<UpcomingGigersApiModel>? = null
){

    companion object {
        @Ignore
        const val TABLE_NAME: String = "work_space_home_screen_data"

        @Ignore
        const val COLUMN_ID: String = "id"

        @Ignore
        const val COLUMN_NAME: String = "name"

        @Ignore
        const val COLUMN_DESCRIPTION: String = "description"

        @Ignore
        const val COLUMN_UPDATED_ON: String = "updated_on"
    }
}


data class CachedFiltersItemModel(

    @ColumnInfo(name = "endDate")
    val endDate: LocalDateTime? = null,

    @ColumnInfo(name = "text")
    val text: String? = null,

    @ColumnInfo(name = "filterId")
    val filterId: String? = null,

    @ColumnInfo(name = "startDate")
    val startDate: LocalDateTime? = null,

    @ColumnInfo(name = "default")
    val default: Boolean? = false,
)