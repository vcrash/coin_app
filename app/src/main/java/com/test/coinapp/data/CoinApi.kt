package com.test.coinapp.data

import kotlinx.coroutines.*
import retrofit2.Response
import javax.inject.Inject

class CoinApi @Inject constructor() {
    private val serviceProvider = ApiServiceProvider()

    data class ApiResult<DT>(
        val success: Boolean,
        val data: DT?,
        val errorCode: Int
    ) : Any() {
        companion object {
            fun <DT> from(response: Response<DT>) : ApiResult<DT> {
                return ApiResult(
                    response.isSuccessful,
                    response.body(),
                    response.code()
                )
            }

            fun <DT> from(throwable: Throwable, defaultValue: DT? = null) : ApiResult<DT> {
                return ApiResult(
                    success = false,
                    data = defaultValue,
                    errorCode = UNSPECIFIED_ERROR_CODE
                )
            }
        }
    }

    fun getCurrenciesAsync(scope: CoroutineScope) : Deferred<ApiResult<List<CurrencyAsset>>?> {
        return scope.async {
            try {
                serviceProvider.getServiceAsync()?.getCurrenciesAsync()?.let {
                    ApiResult(
                        it.isSuccessful,
                        it.body(),
                        it.code()
                    )
                }
            } catch (e: Throwable) {
                return@async ApiResult.from(e, listOf<CurrencyAsset>())
            }
        }
    }

    /**
     * @param currencyId - asset identifier. Superset of the ISO 4217 currency codes standard
     */
    fun getRatesForAsync(scope: CoroutineScope, currencyId: String) : Deferred<ApiResult<Rates>?> {
        return scope.async(Dispatchers.IO) {
            try {
                serviceProvider.getServiceAsync()
                    ?.getRatesAsync(currencyId)?.let {
                        ApiResult.from(it)
                    }
            } catch (e: Throwable) {
                return@async ApiResult.from<Rates>(e)
            }
        }
    }

    /**
     * Using ISO 4217 currency codes standard
     * @param baseCurrencyId - код валюты, которую нужно сконвертировать.
     * @param targetCurrencyId - код целевой валюты
     */
    fun getExchangeRateAsync(scope: CoroutineScope, baseCurrencyId: String, targetCurrencyId: String) : Deferred<ApiResult<SingleRate>?> {
        return scope.async(Dispatchers.IO) {
            try {
                serviceProvider.getServiceAsync()
                    ?.getExchangeRateAsync(baseCurrencyId, targetCurrencyId)?.let {
                        ApiResult.from(it)
                    }
            } catch (e: Throwable) {
                return@async ApiResult.from<SingleRate>(e)
            }
        }
    }

    companion object {
        const val UNSPECIFIED_ERROR_CODE = -1
    }
}