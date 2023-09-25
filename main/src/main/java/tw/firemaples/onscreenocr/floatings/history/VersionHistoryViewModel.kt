package tw.firemaples.onscreenocr.floatings.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.utils.Utils

class VersionHistoryViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {

    private val _versionHistoryUrl = MutableLiveData<String>()
    val versionHistoryUrl: LiveData<String> = _versionHistoryUrl

    fun load() {
        viewScope.launch {
            _versionHistoryUrl.value = Utils.context.getString(R.string.version_history_url)
        }
    }
}
