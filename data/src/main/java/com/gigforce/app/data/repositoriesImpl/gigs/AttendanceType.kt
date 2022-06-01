package com.gigforce.app.data.repositoriesImpl.gigs

object AttendanceType {

    /**
     * In this case attendance of both TL and Giger is stored in different
     * objects in database , they dont overwrite
     */
    const val PARALLEL_ONLY_TL = "Only TL"

    /**
     * In this case attendance is overwritten in database,
     * ex if giger has already marked attendance then if tl marks gigers attendance then it will
     * be overwritten
     */
    const val OVERWRITE_BOTH = "Both Giger and TL"
}