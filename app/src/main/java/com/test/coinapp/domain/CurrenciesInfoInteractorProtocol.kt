package com.test.coinapp.domain

import com.test.coinapp.data.Rates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface CurrenciesInfoInteractorProtocol {
    val ratesListData: ReceiveChannel<Rates>

    fun getRates(scope: CoroutineScope)

    val dataIsPending : ReceiveChannel<Boolean>

    val onDataReceiveError : ReceiveChannel<Int?>

    fun repeatRequestConfirmed(scope: CoroutineScope)

    fun repeatRequestDeclined()

    val onNeedReturnToCurrenciesSelection : ReceiveChannel<Unit>

    fun cancelAll()
}