package tw.firemaples.onscreenocr.floatings.compose.menu

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MenuModule {
    @Binds
    fun bindMenuViewModel(menuViewModelImpl: MenuViewModelImpl): MenuViewModel
}
