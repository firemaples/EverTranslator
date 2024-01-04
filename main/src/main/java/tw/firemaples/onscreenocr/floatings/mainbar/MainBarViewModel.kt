package tw.firemaples.onscreenocr.floatings.mainbar

import javax.inject.Inject

interface MainBarViewModel {
    fun onMenuItemClicked(key: String)
    fun onSelectClicked()
    fun onTranslateClicked()
    fun onCloseClicked()
    fun onMenuButtonClicked()
    fun onAttachedToScreen()
    fun saveLastPosition(x: Int, y: Int)

}

class MainBarViewModelImpl @Inject constructor(

): MainBarViewModel {
    override fun onMenuItemClicked(key: String) {

    }

    override fun onSelectClicked() {

    }

    override fun onTranslateClicked() {

    }

    override fun onCloseClicked() {

    }

    override fun onMenuButtonClicked() {

    }

    override fun onAttachedToScreen() {

    }

    override fun saveLastPosition(x: Int, y: Int) {

    }
}
