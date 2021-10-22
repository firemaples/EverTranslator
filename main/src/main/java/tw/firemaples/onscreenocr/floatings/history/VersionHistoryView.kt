package tw.firemaples.onscreenocr.floatings.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.*
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.repo.GeneralRepository

class VersionHistoryView(context: Context) : DialogView(context) {
    private val viewModel: VersionHistoryViewModel by lazy { VersionHistoryViewModel(viewScope) }

    private val diffUtil = object : DiffUtil.ItemCallback<GeneralRepository.Record>() {
        override fun areItemsTheSame(
            oldItem: GeneralRepository.Record,
            newItem: GeneralRepository.Record
        ): Boolean =
            oldItem.version == newItem.version

        override fun areContentsTheSame(
            oldItem: GeneralRepository.Record,
            newItem: GeneralRepository.Record
        ): Boolean =
            oldItem == newItem
    }
    private val adapter: HistoryAdapter by lazy { HistoryAdapter(context, diffUtil) }

    init {
        setTitle(context.getString(R.string.title_version_history))
        setDialogType(DialogType.CONFIRM_ONLY)
        prepareContentView()
    }

    private fun prepareContentView() {
        val view = View.inflate(context, R.layout.view_version_history, null)
        setContentView(view)

        val rv = view as RecyclerView
        rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@VersionHistoryView.adapter
        }
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()

        viewModel.recordList.observe(lifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.load()
    }

    private class HistoryAdapter(
        val context: Context,
        diffCallback: DiffUtil.ItemCallback<GeneralRepository.Record>
    ) :
        ListAdapter<GeneralRepository.Record, HistoryAdapter.ViewHolder>(diffCallback) {
        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvVersion: TextView = itemView.findViewById(R.id.tv_version)
            val tvDesc: TextView = itemView.findViewById(R.id.tv_desc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.item_version_history, parent, false)
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            with(holder) {
                tvVersion.text = item.version
                tvDesc.text = item.desc
            }
        }
    }
}
