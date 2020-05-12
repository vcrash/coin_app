package com.test.coinapp.ui.currencyinfo

import com.test.coinapp.R
import com.test.coinapp.databinding.ItemRateBinding
import com.test.coinapp.ui.base.AbstractRecyclerViewViewHolder

class RatesListViewHolder(private val viewBinding: ItemRateBinding)
    : AbstractRecyclerViewViewHolder<CurrencyRatesListItemData>(viewBinding.root) {
    override fun bindData(dataItem: CurrencyRatesListItemData) {
        viewBinding.apply {
            currencyRate.text = root.context.getString(R.string.currency_rate_label_value, dataItem.rate)
            targetCurrencyLabel.text = dataItem.currencyCode
        }
    }
}