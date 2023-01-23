package tw.firemaples.onscreenocr.floatings.recognizedTextEditor

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.utils.UIUtils

class RecognizedTextEditorViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {
    private val _viewMaxHeight = MutableLiveData<Int>()
    val viewMaxHeight: LiveData<Int> = _viewMaxHeight

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    private val _review = MutableLiveData<Bitmap?>()
    val review: LiveData<Bitmap?> = _review

    fun init(text: String, review: Bitmap?) {
        viewScope.launch {
            _viewMaxHeight.value = (UIUtils.screenSize[1] * 0.3).toInt()
            _text.value = text
            _review.value = review
        }
    }
}
