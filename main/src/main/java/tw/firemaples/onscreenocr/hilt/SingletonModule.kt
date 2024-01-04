package tw.firemaples.onscreenocr.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.floatings.manager.StateNavigatorImpl

@Module
@InstallIn(SingletonComponent::class)
interface SingletonModule {

    @Binds
    fun bindStateNavigator(stateNavigatorImpl: StateNavigatorImpl): StateNavigator
}
