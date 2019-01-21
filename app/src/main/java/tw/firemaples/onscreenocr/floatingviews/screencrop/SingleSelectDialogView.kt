package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import tw.firemaples.onscreenocr.R

class SingleSelectDialogView(context: Context,
                             private val _title: String,
                             private val selections: Array<String>,
                             private val callback: (Int) -> Unit) :
        InfoDialogView(context) {
    override fun getButtonMode(): Int = MODE_CLOSE

    override fun getContentLayoutId(): Int = R.layout.view_single_select_dialog

    override fun getTitle(): String = _title

    override fun setViews(rootView: View?) {
        super.setViews(rootView)
        rootView?.findViewById<RecyclerView>(R.id.lv_list)?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.divider_horizontal)?.also {
                    setDrawable(it)
                }
            })
            adapter = ListAdapter(context, selections) {
                detachFromWindow()
                callback(it)
            }
        }
    }

    private class ListAdapter(private val context: Context,
                              private val selections: Array<String>,
                              private val callback: (Int) -> Unit) :
            RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
        private class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): MyViewHolder =
                MyViewHolder(LayoutInflater.from(context)
                        .inflate(R.layout.item_single_select_dialog, viewGroup, false) as TextView)

        override fun getItemCount(): Int = selections.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.textView.apply {
                text = selections[position]
                setOnClickListener {
                    callback(position)
                }
            }
        }
    }
}