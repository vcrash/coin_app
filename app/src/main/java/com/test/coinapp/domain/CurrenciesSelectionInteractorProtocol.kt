package com.test.coinapp.domain

import com.test.coinapp.data.CurrencyAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface CurrenciesSelectionInteractorProtocol {
    val onCurrenciesListData: ReceiveChannel<List<CurrencyAsset>>

    val onDataReceiveError : ReceiveChannel<Int?>

    val onNeedShutDown : ReceiveChannel<Unit>

    val dataIsPending : ReceiveChannel<Boolean>

    val onNeedShowCurrencyDetails: ReceiveChannel<String>

    fun cancelAll()

    fun initWorkFlow(scope: CoroutineScope)

    fun repeatRequestConfirmed(scope: CoroutineScope)

    fun repeatRequestDeclined()

    fun onViewPresentationFailed()

    fun onCurrencySelected(currencyCode: String)
}