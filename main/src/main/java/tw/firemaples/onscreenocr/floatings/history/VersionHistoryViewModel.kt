package tw.firemaples.onscreenocr.floatings.history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.repo.GeneralRepository
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

class VersionHistoryViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {

    private val _recordList = MutableLiveData<List<GeneralRepository.Record>>()
    val recordList: LiveData<List<GeneralRepository.Record>> = _recordList

    private val context: Context by lazy { Utils.context }
    private val logger: Logger by lazy { Logger(VersionHistoryViewModel::class) }

    private val repo: GeneralRepository by lazy { GeneralRepository() }

    fun load() {
        viewScope.launch {
            _recordList.value = repo.getVersionHistory().first()
        }
    }
}
