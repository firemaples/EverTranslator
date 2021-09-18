package tw.firemaples.onscreenocr.floatings.translationSelectPanel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.recyclerview.widget.*
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.menu.MenuView
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.setTextOrGone

class TranslationSelectPanel(context: Context) : FloatingView(context) {
    override val layoutId: Int
        get() = R.layout.floating_translation_select_panel

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    private val logger: Logger by lazy { Logger(TranslationSelectPanel::class) }

    private val viewModel: TranslationSelectPanelViewModel by lazy {
        TranslationSelectPanelViewModel(viewScope)
    }

    private val viewOutside: View = rootView.findViewById(R.id.view_outside)
    private val btClose: View = rootView.findViewById(R.id.bt_close)

    private val rvOCRLang: RecyclerView = rootView.findViewById(R.id.rv_ocrLang)
    private val tvTranslationProvider: TextView = rootView.findViewById(R.id.tv_translationProvider)
    private val rvTranslationLang: RecyclerView = rootView.findViewById(R.id.rv_translationLang)
    private val tvTranslationLangHint: TextView = rootView.findViewById(R.id.tv_translationLangHint)

    private val translationProviderMenuView: MenuView by lazy {
        MenuView(context, checkable = true).apply {
            setAnchor(tvTranslationProvider)
            onItemSelected = { view, key ->
                logger.debug("onItemSelected: $key")

                view.detachFromScreen()
                viewModel.onTranslationProviderSelected(key)
            }
        }
    }

    private lateinit var ocrLangListAdapter: LangListAdapter
    private lateinit var translationLangListAdapter: LangListAdapter
    private val listDiffCallback = object : DiffUtil.ItemCallback<LangItem>() {
        override fun areItemsTheSame(
            oldItem: LangItem, newItem: LangItem
        ): Boolean = oldItem.code == newItem.code

        override fun areContentsTheSame(
            oldItem: LangItem, newItem: LangItem
        ): Boolean = oldItem == newItem
    }

    init {
        setViews()
        loadData()
    }

    private fun setViews() {
        viewOutside.setOnClickListener { detachFromScreen() }
        btClose.setOnClickListener { detachFromScreen() }

        with(rvOCRLang) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            ocrLangListAdapter = LangListAdapter(
                context = context,
                diffCallback = listDiffCallback,
                onItemClicked = {
                    logger.debug("on OCR lang checked, $it")

                    viewModel.onOCRLangSelected(it)
                })
            adapter = ocrLangListAdapter
        }

        with(rvTranslationLang) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            translationLangListAdapter = LangListAdapter(
                context = context,
                diffCallback = listDiffCallback,
                onItemClicked = {
                    logger.debug("on translation lang checked, $it")

                    viewModel.onTranslationLangChecked(it)
                })
            adapter = translationLangListAdapter
        }

        tvTranslationProvider.setOnClickListener {
            viewModel.onTranslationProviderClicked()
        }

        viewModel.ocrLanguageList.observe(lifecycleOwner) {
            logger.debug("on ocrLanguageList changed: $it")

            ocrLangListAdapter.submitList(it)
        }

        viewModel.selectedTranslationProviderName.observe(lifecycleOwner) {
            tvTranslationProvider.text = it
        }

        viewModel.translationLangList.observe(lifecycleOwner) {
            translationLangListAdapter.submitList(it)
        }

        viewModel.displayTranslationHint.observe(lifecycleOwner) {
            tvTranslationLangHint.setTextOrGone(it)
        }

        viewModel.displayTranslateProviders.observe(lifecycleOwner) {
            translationProviderMenuView.apply {
                updateData(
                    itemList = it.map { it.key to it.displayName }.toMap(),
                    selectedKey = it.firstOrNull { it.selected }?.key
                )
            }.attachToScreen()
        }
    }

    override fun onBackButtonPressed(): Boolean {
        detachFromScreen()
        return true
    }

    private fun loadData() {
        viewModel.load()
    }

    private class LangListAdapter(
        private val context: Context,
        diffCallback: DiffUtil.ItemCallback<LangItem>,
        private val onItemClicked: (langCode: String) -> Unit,
    ) :
        ListAdapter<LangItem, LangListAdapter.ViewHolder>(diffCallback) {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: CheckedTextView = itemView as CheckedTextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_lang_list, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            holder.name.text = item.displayName
            holder.name.isChecked = item.selected
            holder.itemView.setOnClickListener { onItemClicked.invoke(item.code) }
        }
    }
}
