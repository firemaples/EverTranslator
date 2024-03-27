package tw.firemaples.onscreenocr.floatings.compose.resultview

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ResultViewModule {
    @Binds
    fun bindResultViewModel(resultViewModelImpl: ResultViewModelImpl): ResultViewModel
}
