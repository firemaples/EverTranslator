package tw.firemaples.onscreenocr.views

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatingviews.FloatingView
import tw.firemaples.onscreenocr.utils.UIUtil
import tw.firemaples.onscreenocr.utils.onViewPrepared

class MenuView(
        context: Context,
        private val selection: List<String>,
        private val ids: List<Int>,
        private val listener: OnMenuItemClickedListener
) : FloatingView(context) {

    var marginDp: Float = 4f
    private val layoutMargin: Int
        get() = UIUtil.dpToPx(context, marginDp)

    private val adapter by lazy { MenuAdapter(context, selection, ids) }

    private val viewRoot: RelativeLayout = rootView.findViewById(R.id.view_root)
    private val viewMenu: View = rootView.findViewById(R.id.view_menu)

    init {
        val listView: ListView = rootView.findViewById(R.id.lv_menu)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            detachFromWindow()
            listener.onMenuItemClicked(position, ids[position], selection[position])
        }

        viewRoot.setOnClickListener {
            detachFromWindow()
        }
    }

    fun attachToWindow(anchorRect: Rect) {
        attachToWindow()
        viewMenu.onViewPrepared {
            val margins = UIUtil.countViewPosition(anchorRect, viewMenu.width, viewMenu.height,
                    layoutMargin)
            val layoutParams = viewMenu.layoutParams as RelativeLayout.LayoutParams
            layoutParams.leftMargin = margins[0]
            layoutParams.topMargin = margins[1]

            if (layoutParams.topMargin == -1) {
                layoutParams.topMargin = anchorRect.bottom + layoutMargin
            }

            viewRoot.updateViewLayout(viewMenu, layoutParams)
            
            viewRoot.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_menu_appearance))
        }
    }

    override fun getLayoutId(): Int = R.layout.view_menu

    override fun getLayoutSize(): Int = WindowManager.LayoutParams.MATCH_PARENT

    private class MenuAdapter(val context: Context, val selection: List<String>, val ids: List<Int>) : BaseAdapter() {
        private val inflater by lazy { LayoutInflater.from(context) }

        private class ViewHolder(val view: View) {
            val textView: TextView

            init {
                view.tag = this
                textView = view.findViewById(R.id.tv_menuItem)
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder = if (convertView == null)
                ViewHolder(inflater.inflate(R.layout.item_menu, parent, false))
            else convertView.tag as ViewHolder

            holder.textView.text = selection[position]

            return holder.view
        }

        override fun getItem(position: Int): Any = selection[position]

        override fun getItemId(position: Int): Long = ids[position].toLong()

        override fun getCount(): Int = selection.size
    }

    interface OnMenuItemClickedListener {
        fun onMenuItemClicked(position: Int, id: Int, item: String)
    }
}