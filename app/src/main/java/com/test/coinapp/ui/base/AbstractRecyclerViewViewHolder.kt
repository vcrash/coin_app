package com.test.coinapp.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractRecyclerViewViewHolder<D>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindData(dataItem: D)
}