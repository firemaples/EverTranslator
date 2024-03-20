package tw.firemaples.onscreenocr.floatings.compose.screencircling

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ScreenCirclingModule {
    @Binds
    fun bindScreenCirclingViewModel(impl: ScreenCirclingViewModelImpl): ScreenCirclingViewModel
}
