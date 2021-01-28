package com.gigforce.learning.di

import com.gigforce.learning.fragment.LearningSectionFragment
import dagger.Subcomponent

@Subcomponent
interface ILearningModuleComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ILearningModuleComponent
    }

    fun inject(item: LearningSectionFragment)

}

interface ILearningModuleComponentProvider {
    fun provideLearningModuleComponent(): ILearningModuleComponent
}