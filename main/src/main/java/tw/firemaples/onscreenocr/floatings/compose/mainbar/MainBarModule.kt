package tw.firemaples.onscreenocr.floatings.compose.mainbar

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MainBarModule {
    @Binds
    fun bindMainBarViewModel(mainBarViewModelImpl: MainBarViewModelImpl): MainBarViewModel
}
