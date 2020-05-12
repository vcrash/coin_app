package com.test.coinapp.domain

import com.test.coinapp.data.CurrenciesRepositoryProtocol
import com.test.coinapp.data.Rates
import com.test.coinapp.di.currencyinfo.CurrencyInfoFragmentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.lang.Error
import javax.inject.Inject
import javax.inject.Named

class CurrenciesInfoInteractor
    @Inject constructor(
        private val repository: CurrenciesRepositoryProtocol,
        @Named(CurrencyInfoFragmentComponent.BIND_CURRENCY_CODE)
        private val baseCurrencyCode: String)
    : CurrenciesInfoInteractorProtocol {
    private var dataRequestJob: Job? = null
    private val ratesListChannel = Channel<Rates>(Channel.CONFLATED)
    private val onDataReceiveErrorChannel = Channel<Int?>(Channel.CONFLATED)
    private val onNeedReturnToCurrenciesSelectionChannel = Channel<Unit>(Channel.CONFLATED)
    private val dataIsPendingChannel = Channel<Boolean>(Channel.CONFLATED)
    private val channels = setOf(
        ratesListChannel,
        onDataReceiveErrorChannel,
        onNeedReturnToCurrenciesSelectionChannel,
        dataIsPendingChannel
    )

    override val ratesListData: ReceiveChannel<Rates>
        get() = ratesListChannel

    override val onDataReceiveError: ReceiveChannel<Int?>
        get() = onDataReceiveErrorChannel

    override val onNeedReturnToCurrenciesSelection: ReceiveChannel<Unit>
        get() = onNeedReturnToCurrenciesSelectionChannel

    override fun cancelAll() {
        channels.forEach { it.cancel() }
        dataRequestJob?.cancel()
    }

    override fun getRates(scope: CoroutineScope) {
        requestRates(scope, baseCurrencyCode)
    }

    override val dataIsPending: ReceiveChannel<Boolean>
        get() = dataIsPendingChannel

    private fun requestRates(scope: CoroutineScope, baseCurrencyCode: String) {
        dataRequestJob?.cancel()
        dataRequestJob = scope.launch {
            dataIsPendingChannel.offer(true, channels)
            repository.getRatesFor(scope, baseCurrencyCode, onSuccess = { result ->
                validatedResult(result)?.also {
                    ratesListChannel.offer(it, channels)
                } ?: publishRequestError(null)
                dataIsPendingChannel.offer(false, channels)
        }, onFailed = {
                publishRequestError(it)
                dataIsPendingChannel.offer(false, channels)
            })
        }
    }

    private fun publishRequestError(code: Int?) {
        onDataReceiveErrorChannel.offer(code, channels)
    }

    override fun repeatRequestConfirmed(scope: CoroutineScope) {
        requestRates(scope, baseCurrencyCode)
    }

    override fun repeatRequestDeclined() {
        onNeedReturnToCurrenciesSelectionChannel.offer(Unit, channels)
    }

    private fun validatedResult(rates: Rates?) : Rates? {
        return if (rates?.rates == null) null else rates
    }

    private fun <E : Any?> Channel<E>.offer(element: E, channels: Set<Channel<out Any?>>) : Boolean {
        if (!channels.contains(this)) throw Error("Channel must be in channels set")
        return try {
            offer(element)
        } catch (e: ClosedReceiveChannelException) {
            false
        } catch (e: ClosedSendChannelException) {
            false
        }
    }
}