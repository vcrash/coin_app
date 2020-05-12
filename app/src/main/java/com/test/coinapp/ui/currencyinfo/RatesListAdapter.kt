package com.test.coinapp.ui.currencyinfo

import com.test.coinapp.R
import com.test.coinapp.databinding.ItemRateBinding
import com.test.coinapp.ui.base.AbstractRecyclerViewAdapter

class RatesListAdapter(
    itemsData: List<CurrencyRatesListItemData>
) : AbstractRecyclerViewAdapter<CurrencyRatesListItemData, RatesListViewHolder>(
    itemsData,
    onGetItemLayoutId = resId@{ return@resId R.layout.item_rate },
    onGetViewHolder = holder@{ view, _ ->
        return@holder RatesListViewHolder(ItemRateBinding.bind(view))
    },
    onGetData = onGetData@{ offset, dataSource ->
        return@onGetData if (dataSource.isNotEmpty())
            dataSource[offset]
        else
            null
    }
)