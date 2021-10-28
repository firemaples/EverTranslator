package tw.firemaples.onscreenocr.floatings.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.recyclerview.widget.*
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.utils.*

class MenuView(context: Context, private val checkable: Boolean) : FloatingView(context) {
    override val layoutId: Int
        get() = R.layout.floating_menu
    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT
    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    private val viewRoot: RelativeLayout = rootView.findViewById(R.id.viewRoot)
    private val viewMenuWrapper: View = rootView.findViewById(R.id.view_menuWrapper)
    private val rvMenu: RecyclerView = rootView.findViewById(R.id.rv_menu)

    private var anchorView: View? = null

    var marginDp = 0f
    private val layoutMargin
        get() = marginDp.dpToPx()

    private val diffCallback = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem
    }
    private val adapter: MenuListAdapter by lazy {
        MenuListAdapter(context, diffCallback, checkable) { key ->
            onItemChecked(key)
        }
    }

    private var selectedKey: String? = null
    private val itemList: MutableList<Item> = mutableListOf()

    var onItemSelected: ((menuView: MenuView, key: String) -> Unit)? = null

    init {
        setViews()
    }

    private fun setViews() {
        with(rvMenu) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@MenuView.adapter
        }

        viewRoot.clickOnce { detachFromScreen() }
    }

    fun setAnchor(view: View) {
        this.anchorView = view
    }

    fun updateData(itemList: Map<String, String>, selectedKey: String? = null) {
        this.selectedKey = selectedKey
        this.itemList.clear()
        this.itemList.addAll(itemList.map { Item(it.key, it.value, it.key == selectedKey) })

        adapter.submitList(this.itemList)
    }

    private fun onItemChecked(key: String) {
        this.selectedKey = key
        val newList = this.itemList.map { it.copy(checked = it.key == selectedKey) }
        adapter.submitList(newList)

        this.onItemSelected?.invoke(this, key)
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()

        viewMenuWrapper.visibility = View.INVISIBLE

        val anchorView = anchorView ?: return

        viewMenuWrapper.onViewPrepared {

            val (leftMargin, topMargin) = UIUtils.countViewPosition(
                anchorRect = anchorView.getViewRect(),
                parentRect = viewRoot.getViewRect(),
                itemWidth = viewMenuWrapper.width,
                itemHeight = viewMenuWrapper.height,
                layoutMargin = layoutMargin
            )

            val layoutParams = (viewMenuWrapper.layoutParams as RelativeLayout.LayoutParams).apply {
                this.leftMargin = leftMargin
                this.topMargin = topMargin
            }

            viewRoot.updateViewLayout(viewMenuWrapper, layoutParams)

            viewRoot.post {
                viewMenuWrapper.visibility = View.VISIBLE
            }
        }
    }

    private class MenuListAdapter(
        private val context: Context,
        diffCallback: DiffUtil.ItemCallback<Item>,
        private val checkable: Boolean,
        private val onItemChecked: (key: String) -> Unit,
    ) :
        ListAdapter<Item, MenuListAdapter.ViewHolder>(diffCallback) {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val button: Button = itemView as Button
            val radioButton: RadioButton? = itemView as? RadioButton
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(context).inflate(
                    if (checkable) R.layout.item_menu_checkable else R.layout.item_menu,
                    parent, false
                )
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            holder.button.text = item.name
            holder.radioButton?.isChecked = item.checked
            holder.itemView.clickOnce {
                onItemChecked.invoke(item.key)
            }
        }
    }

    data class Item(val key: String, val name: String, val checked: Boolean)
}
