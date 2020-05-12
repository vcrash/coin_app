package com.test.coinapp.ui.currencyinfo

import androidx.lifecycle.LiveData

interface CurrencyInfoViewModelProtocol {
    val currencyCode: String
    fun getData()
    val listData: LiveData<List<CurrencyRatesListItemData>>
    fun filterSearch(searchString: String)
    val progressData: LiveData<Boolean>
    val screenTitleData: LiveData<String>
    val onNeedRepeatRequestDialogData: LiveData<String>
    fun repeatRatesRequestConfirmed()
    fun repeatRatesRequestDeclined()
    val onNeedReturnToCurrenciesSelection: LiveData<Unit>
}