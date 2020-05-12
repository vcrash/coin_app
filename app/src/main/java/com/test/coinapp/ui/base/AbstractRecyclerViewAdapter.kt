package com.test.coinapp.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractRecyclerViewAdapter<D, VH : AbstractRecyclerViewViewHolder<D>>(
    itemsData: List<D>,
    private val onGetItemLayoutId: (position: Int)->Int,
    private val onGetCustomViewForLayoutId: ((type: Int)->View)? = null,
    private val onGetViewHolder: (itemView: View, viewType: Int)->VH,
    private val onGetData: (forPosition: Int, dataSource: List<D>)->D?
) : RecyclerView.Adapter<VH>() {
    protected val dataSource = mutableListOf<D>()
    private var itemsCount: Int

    init {
        updateData(itemsData)
        itemsCount = dataSource.size
    }

    private fun updateData(dataUpdate: List<D>) {
        dataSource.clear()
        dataSource.addAll(dataUpdate)
        itemsCount = dataSource.size
    }

    fun setDataAndNotify(dataUpdate: List<D>) {
        updateData(dataUpdate)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemsCount
    }

    final override fun getItemViewType(position: Int): Int {
        return onGetItemLayoutId(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = onGetCustomViewForLayoutId?.invoke(viewType) ?:
            LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return getNewHolder(view, viewType)
    }

    open fun getNewHolder(view: View, viewType: Int) : VH {
        return onGetViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindData(onGetData(position, dataSource) ?: return)
    }
}