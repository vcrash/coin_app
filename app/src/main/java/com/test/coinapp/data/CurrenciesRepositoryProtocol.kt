package com.test.coinapp.data

import kotlinx.coroutines.CoroutineScope

interface CurrenciesRepositoryProtocol {

    fun getCurrencies(scope: CoroutineScope, onSuccess: (List<CurrencyAsset>?)->Unit, onFailed: (errorCode: Int?)->Unit)

    fun getRatesFor(scope: CoroutineScope, currencyId: String, onSuccess: (Rates?)->Unit, onFailed: (errorCode: Int?)->Unit)

    fun getExchangeRate(scope: CoroutineScope, baseCurrencyId: String, targetCurrencyId: String, onSuccess: (SingleRate?)->Unit, onFailed: (errorCode: Int?)->Unit)
}