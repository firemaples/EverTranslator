package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.util.TypedValue
import android.widget.FrameLayout
import com.chibatching.kotpref.livedata.asLiveData
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.ViewFontSizeAdjusterBinding
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.getThemedLayoutInflater

class FontSizeAdjuster(context: Context) : DialogView(context) {

    private val binding = ViewFontSizeAdjusterBinding.inflate(context.getThemedLayoutInflater())

    init {
        setTitle(context.getString(R.string.title_result_window_font_size_adjuster))
        setDialogType(DialogType.CONFIRM_CANCEL)
        setContentView(
            binding.root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        setViews()
    }

    private fun setViews() {
        val lastValue = AppPref.resultWindowFontSize

        AppPref.asLiveData(AppPref::resultWindowFontSize).observe(lifecycleOwner) {
            binding.textPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
        }

        binding.fontSize.value = lastValue

        binding.fontSize.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            AppPref.resultWindowFontSize = value
        }

        onButtonCancelClicked = {
            AppPref.resultWindowFontSize = lastValue
        }
    }
}
