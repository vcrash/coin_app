package com.test.coinapp.data

import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrenciesRepository @Inject constructor(private val coinApi: CoinApi) :
    CurrenciesRepositoryProtocol {
    private var currenciesCache: List<CurrencyAsset>? = null
    private var ratesCache = hashMapOf<String, Rates?>()
    private var exchangeRateCache = hashMapOf<ExchangeRateCacheKey, SingleRate?>()

    private data class ExchangeRateCacheKey(val baseCurrencyId: String, val targetCurrencyId: String)

    override fun getCurrencies(scope: CoroutineScope, onSuccess: (List<CurrencyAsset>?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        currenciesCache?.also {
            onSuccess(it)
            return
        }
        val pending = coinApi.getCurrenciesAsync(scope)
        waitAndPublish(scope, pending, onSuccess = {
            currenciesCache = it
            onSuccess(it)
            }, onFailed = onFailed
        )
    }

    override fun getRatesFor(scope: CoroutineScope, currencyId: String, onSuccess: (Rates?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        ratesCache[currencyId]?.also {
            onSuccess(it)
            return
        }
        val pending = coinApi.getRatesForAsync(scope, currencyId)
        waitAndPublish(scope, pending, onSuccess = {
            if (ratesCache.keys.count() > MAX_CACHE_SIZE) ratesCache.clear()
            ratesCache[currencyId] = it
            onSuccess(it)
        }, onFailed = onFailed)
    }

    override fun getExchangeRate(scope: CoroutineScope, baseCurrencyId: String, targetCurrencyId: String, onSuccess: (SingleRate?)->Unit, onFailed: (errorCode: Int?)->Unit) {
        exchangeRateCache[ExchangeRateCacheKey(baseCurrencyId, targetCurrencyId)]?.also {
            onSuccess(it)
            return
        }
        val pending = coinApi.getExchangeRateAsync(scope, baseCurrencyId, targetCurrencyId)
        waitAndPublish(scope, pending, onSuccess = {
            if (exchangeRateCache.keys.count() > MAX_CACHE_SIZE) exchangeRateCache.clear()
            exchangeRateCache[ExchangeRateCacheKey(baseCurrencyId, targetCurrencyId)] = it
            onSuccess(it)
        }, onFailed = onFailed)
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
        const val MAX_CACHE_SIZE = 5
    }
}