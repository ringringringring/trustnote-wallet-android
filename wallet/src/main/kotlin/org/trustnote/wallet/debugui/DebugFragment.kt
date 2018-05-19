package org.trustnote.wallet.debugui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.trustnote.wallet.R
import org.trustnote.wallet.uiframework.FragmentBase
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.webkit.ValueCallback
import org.trustnote.wallet.js.JSApi
import org.trustnote.wallet.biz.init.CreateWalletActivity

class DebugFragment : FragmentBase() {
    override fun getLayoutId(): Int {
        return R.layout.debug_main
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun initFragment(view: View) {
        updateUI(view)
    }

    private fun updateUI(view: View) {
        val recyclerView: RecyclerView = view
                .findViewById<RecyclerView>(R.id.debug_list)
        //recyclerView.addItemDecoration(SimpleDividerItemDecoration(view.context))
        recyclerView.layoutManager = LinearLayoutManager(activity)
        var data: Array<DebugItemData> = arrayOf(
                DebugItemData(2, "Test New seed create UI"),
                DebugItemData(1, "Test mnomonic JS")
        )

        val adapter = DebugItemsAdapter(data)
        recyclerView.adapter = adapter
    }

}


class SimpleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var mData: DebugItemData? = null
    private var mTitleTextView = itemView.findViewById<TextView>(R.id.title)

    init {
        itemView.setOnClickListener { _ ->
            when (mData?.id) {
                1 -> JSApi().mnemonic(ValueCallback {
                    //TODO: show toast or log.
                })
                2 -> {
                    CreateWalletActivity.startMe()
                }
            }
        }
    }

    fun bindData(data: DebugItemData) {
        mData = data
        mTitleTextView.text = data.title
    }

}

data class DebugItemData(val id: Int, val title: String)


class DebugItemsAdapter(datas: Array<DebugItemData>) : RecyclerView.Adapter<SimpleHolder>() {
    private var mDataItems: Array<DebugItemData> = datas

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleHolder {
        //TODO: try to use dagger to get the context.
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent?.context);
        return SimpleHolder(layoutInflater.inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: SimpleHolder?, position: Int) {
        holder?.bindData(mDataItems[position]);
    }

    override fun getItemCount(): Int {
        return mDataItems.size
    }
}