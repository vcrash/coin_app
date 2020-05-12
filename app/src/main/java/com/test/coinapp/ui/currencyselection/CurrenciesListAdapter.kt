package com.test.coinapp.ui.currencyselection

import com.test.coinapp.R
import com.test.coinapp.databinding.ItemCurrencyBinding
import com.test.coinapp.ui.base.AbstractRecyclerViewAdapter

class CurrenciesListAdapter(
    itemsData: List<CurrenciesListItemData>,
    onItemSelect: (code: String)->Unit
) : AbstractRecyclerViewAdapter<CurrenciesListItemData, CurrenciesListViewHolder>(
    itemsData,
    onGetItemLayoutId = resId@{ return@resId R.layout.item_currency },
    onGetViewHolder = holder@{ view, _ ->
        return@holder CurrenciesListViewHolder(ItemCurrencyBinding.bind(view), onItemSelect)
    },
    onGetData = onGetData@{ offset, dataSource ->
        return@onGetData if (dataSource.isNotEmpty())
            dataSource[offset]
        else
            null
    }
)