package com.test.coinapp.domain

import com.test.coinapp.data.CurrenciesRepositoryProtocol
import com.test.coinapp.data.CurrencyAsset
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import java.lang.Error
import javax.inject.Inject

class CurrenciesSelectionInteractor
    @Inject constructor(private val repository: CurrenciesRepositoryProtocol)
    : CurrenciesSelectionInteractorProtocol {
    private var dataRequestJob: Job? = null
    private val currenciesListChannel = Channel<List<CurrencyAsset>>(Channel.CONFLATED)
    private val onDataReceiveErrorChannel = Channel<Int?>(Channel.CONFLATED)
    private val onNeedShutdownChannel = Channel<Unit>(Channel.CONFLATED)
    private val dataIsPendingChannel = Channel<Boolean>(Channel.CONFLATED)
    private val onNeedShowCurrencyDetailsChannel = Channel<String>(Channel.CONFLATED)
    private val channels = setOf(
        currenciesListChannel,
        onDataReceiveErrorChannel,
        onNeedShutdownChannel,
        dataIsPendingChannel,
        onNeedShowCurrencyDetailsChannel
    )

    override val onCurrenciesListData: ReceiveChannel<List<CurrencyAsset>>
        get() = currenciesListChannel

    override val onDataReceiveError : ReceiveChannel<Int?>
        get() = onDataReceiveErrorChannel

    override val onNeedShutDown: ReceiveChannel<Unit>
        get() = onNeedShutdownChannel

    override val dataIsPending: ReceiveChannel<Boolean>
        get() = dataIsPendingChannel

    override val onNeedShowCurrencyDetails: ReceiveChannel<String>
        get() = onNeedShowCurrencyDetailsChannel

    override fun cancelAll() {
        channels.forEach { it.cancel() }
        dataRequestJob?.cancel()
    }

    override fun initWorkFlow(scope: CoroutineScope) {
        requestCurrenciesList(scope)
    }

    private fun requestCurrenciesList(scope: CoroutineScope) {
        dataRequestJob?.cancel()
        dataRequestJob = scope.launch {
            dataIsPendingChannel.offer(true, channels)
            repository.getCurrencies(
                scope,
                onSuccess = { result ->
                    validatedResult(result)?.also {
                        currenciesListChannel.offer(it, channels)
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

    override fun onViewPresentationFailed() {
        publishRequestError(null)
    }

    override fun onCurrencySelected(currencyCode: String) {
        onNeedShowCurrencyDetailsChannel.offer(currencyCode, channels)
    }

    override fun repeatRequestConfirmed(scope: CoroutineScope) {
        requestCurrenciesList(scope)
    }

    override fun repeatRequestDeclined() {
        onNeedShutdownChannel.offer(Unit, channels)
    }

    /**
     * @return - возвращает значение входного параметра, если валидация прошла успешно
     * или null в противном случае
     */
    private fun validatedResult(currencies: List<CurrencyAsset>?) : List<CurrencyAsset>? {
        return if (currencies.isNullOrEmpty()) null else currencies
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