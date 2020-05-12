package com.test.coinapp.di.repository

import com.test.coinapp.data.*
import kotlinx.coroutines.*
import javax.inject.Inject

class CurrenciesRepositoryFake @Inject constructor(private val coinApi: CoinApi) :
    CurrenciesRepositoryProtocol {

    private val currenciesFake = listOf(
        CurrencyAsset(
        "USD", "US Dollar", 0, 1.0
        ),
        CurrencyAsset(
            "EUR", "Euro", 0, 1.2
        ),
        CurrencyAsset(
            "BTC", "Bitcoin", 1, 5000.0
        )
    )

    private val ratesFake = Rates(
        assetIdBase = "BTC",
        rates = listOf(
            Rates.Rate(
                time = "2020-04-23T10:12:44.8644049Z",
                assetIdQuote = "USD",
                rate = 7108.108376026589899892381239
            ),
            Rates.Rate(
                time = "2020-04-23T10:14:46.3856385Z",
                assetIdQuote = "EUR",
                rate = 6602.2777392993262497366896922
            )
        )
    )

    private val singleRateFake = SingleRate(
        time = "2020-04-23T10:12:44.8644049Z",
        assetIdBase = "BTC",
        assetIdQuote = "USD",
        rate = 7108.108376026589899892381239
    )

    override fun getCurrencies(scope: CoroutineScope, onSuccess: (List<CurrencyAsset>?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        val pending = scope.async {
            delay(RESPONSE_DELAY)
            CoinApi.ApiResult(true, currenciesFake, 200)
        }
        waitAndPublish(scope, pending, onSuccess, onFailed)
    }

    override fun getRatesFor(scope: CoroutineScope, currencyId: String, onSuccess: (Rates?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        val pending = scope.async {
            delay(RESPONSE_DELAY)
            CoinApi.ApiResult(true, ratesFake, 200)
        }
        waitAndPublish(scope, pending, onSuccess, onFailed)
    }

    override fun getExchangeRate(scope: CoroutineScope, baseCurrencyId: String, targetCurrencyId: String, onSuccess: (SingleRate?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        val pending = scope.async {
            delay(RESPONSE_DELAY)
            CoinApi.ApiResult(true, singleRateFake, 200)
        }
        waitAndPublish(scope, pending, onSuccess, onFailed)
    }

    private fun <T> waitAndPublish(scope: CoroutineScope, pending: Deferred<CoinApi.ApiResult<T>?>, onSuccess: (T?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        val originalContext = scope.coroutineContext
        scope.launch(Dispatchers.Default) {
            val result = pending.await()
            withContext(originalContext) {
                publishResult(result, onSuccess, onFailed)
            }
        }
    }

    private fun <T> publishResult(result: CoinApi.ApiResult<T>?, onSuccess: (T?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        if (result?.success == true) {
            onSuccess(result.data)
        } else {
            onFailed(result?.errorCode)
        }
    }

    companion object {
        const val RESPONSE_DELAY = 2000L
    }
}