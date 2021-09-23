package tw.firemaples.onscreenocr.floatings.screenCircling

import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.repo.GeneralRepository

class ScreenCirclingViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {
    private val generalRepo = GeneralRepository()

    private val _lastSelectedArea = MutableLiveData<Rect?>()
    val lastSelectedArea: LiveData<Rect?> = _lastSelectedArea

    fun load() {
        viewScope.launch {
            if (generalRepo.isRememberLastSelection().first()) {
                val lastSelection = generalRepo.getLastRememberedSelectionArea().first()
                if (lastSelection != null) {
                    _lastSelectedArea.value = lastSelection
                }
            }
        }
    }
}
