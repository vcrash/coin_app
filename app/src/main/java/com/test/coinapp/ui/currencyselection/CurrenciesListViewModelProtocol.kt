package com.test.coinapp.ui.currencyselection

import androidx.lifecycle.LiveData
import androidx.navigation.NavDirections

interface CurrenciesListViewModelProtocol {
    fun initWorkFlow()
    val listData: LiveData<List<CurrenciesListItemData>>
    val progressData: LiveData<Boolean>
    val screenTitleData: LiveData<String>
    fun filterSearch(searchString: String)
    fun itemSelect(currencyCode: String)
    val needNavigateToRates: LiveData<out NavDirections>
    val onNeedRepeatRequestDialogData: LiveData<String>
    fun repeatCurrenciesRequestConfirmed()
    fun repeatCurrenciesRequestDeclined()
    val onNeedShutDownData: LiveData<Unit>
}