package com.test.coinapp.ui.currencyselection

import com.test.coinapp.databinding.ItemCurrencyBinding
import com.test.coinapp.ui.base.AbstractRecyclerViewViewHolder

class CurrenciesListViewHolder(
    private val viewBinding: ItemCurrencyBinding,
    private val onItemClick: (code: String)->Unit
) : AbstractRecyclerViewViewHolder<CurrenciesListItemData>(viewBinding.root) {
    override fun bindData(dataItem: CurrenciesListItemData) {
        viewBinding.apply {
            currencyCode.text = dataItem.currencyCode
            currencyDescr.text = dataItem.currencyName
            root.setOnClickListener { onItemClick(dataItem.currencyCode) }
            currencyCode.requestLayout()
            currencyDescr.requestLayout()
//            root.requestLayout()
        }
    }
}