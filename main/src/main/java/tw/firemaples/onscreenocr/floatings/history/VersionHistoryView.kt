package tw.firemaples.onscreenocr.floatings.history

import android.content.Context
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.ViewVersionHistoryBinding
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.getThemedLayoutInflater
import tw.firemaples.onscreenocr.utils.setAutoDarkMode

class VersionHistoryView(context: Context) : DialogView(context) {
    private val viewModel: VersionHistoryViewModel by lazy { VersionHistoryViewModel(viewScope) }

    private val binding = ViewVersionHistoryBinding.inflate(context.getThemedLayoutInflater())

    private val logger: Logger by lazy { Logger(this::class) }

    init {
        setTitle(context.getString(R.string.title_version_history))
        setDialogType(DialogType.CONFIRM_CANCEL)
        setButtonOkText(context.getString(R.string.text_open_in_browser))
        setButtonCancelText(context.getString(R.string.text_close))
        onButtonOkClicked = {
            viewModel.versionHistoryUrl.value?.also { url ->
                Utils.openBrowser(url)
            }
        }
        setContentView(binding.root)
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        binding.root.setAutoDarkMode()

        viewModel.versionHistoryUrl.observe(lifecycleOwner) {
            binding.root.loadUrl(it)
        }

        viewModel.load()
    }
}
