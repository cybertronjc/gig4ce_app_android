package com.gigforce.app.modules.learning.models.progress

import com.google.firebase.firestore.PropertyName

data class LearningProgressInfo (

    @get:PropertyName("ongoingCourses")
    @set:PropertyName("ongoingCourses")
    var ongoingCourses : List<CourseProgressInfo>,
    @get:PropertyName("completedCourses")
    @set:PropertyName("completedCourses")
    var completedCourses : List<CourseProgressInfo>,

    @get:PropertyName("ongoingModules")
    @set:PropertyName("ongoingModules")
    var ongoingModules : List<ModuleProgressInfo>,
    @get:PropertyName("completedModules")
    @set:PropertyName("completedModules")
    var completedModules : List<ModuleProgressInfo>,

    @get:PropertyName("ongoingLessons")
    @set:PropertyName("ongoingLessons")
    var ongoingLessons : List<LessonsProgressInfo>,
    @get:PropertyName("completedLessons")
    @set:PropertyName("completedLessons")
    var completedLessons : List<LessonsProgressInfo>,

    @get:PropertyName("ongoingSlides")
    @set:PropertyName("ongoingSlides")
    var ongoingSlides : List<SlidesProgressInfo>,
    @get:PropertyName("completedSlides")
    @set:PropertyName("completedSlides")
    var completedSlides : List<SlidesProgressInfo>
)