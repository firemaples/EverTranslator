package tw.firemaples.onscreenocr.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.floatings.manager.StateNavigatorImpl
import tw.firemaples.onscreenocr.floatings.manager.StateOperator
import tw.firemaples.onscreenocr.floatings.manager.StateOperatorImpl

@Module
@InstallIn(SingletonComponent::class)
interface SingletonModule {

    @Binds
    fun bindStateNavigator(stateNavigatorImpl: StateNavigatorImpl): StateNavigator

    @Binds
    fun bindStateOperator(stateOperatorImpl: StateOperatorImpl): StateOperator
}
