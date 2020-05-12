package com.test.coinapp.domain

import com.test.coinapp.data.CoinApi
import com.test.coinapp.data.Rates
import com.test.coinapp.di.repository.CurrenciesRepositoryFake
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.junit.Test

class CurrenciesInfoInteractorTest {
    private val interactor = CurrenciesInfoInteractor(
        CurrenciesRepositoryFake(
            CoinApi()
        ),
        BASE_CURRENCY
    )

    @Test(timeout = CurrenciesRepositoryFake.RESPONSE_DELAY + 2000)
    fun getRates() {
        runBlocking {
            interactor.getRates(this)
            val onWaiting = { isPending: Boolean ->
                println("Data pending: $isPending")
            }
            val dataSubscription = { rates: Rates ->
                println("Result")
                println("Currency: ${rates.assetIdBase}, rates entries: ${rates.rates?.size ?: 0}")
                interactor.cancelAll()
            }
            subscribeForChannel(this, interactor.dataIsPending, onWaiting)
            subscribeForChannel(this, interactor.ratesListData, dataSubscription)
        }
    }

    private fun <T> subscribeForChannel(scope: CoroutineScope, channel: ReceiveChannel<T>, onReceive: (T)->Unit) {
        val initialContext = scope.coroutineContext
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                val result = channel.receive()
                withContext(initialContext) {
                    onReceive(result)
                }
            }
        }
    }

    companion object {
        const val BASE_CURRENCY = "BTC"
    }
}