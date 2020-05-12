package com.test.coinapp.domain

import com.google.common.truth.Truth.assertThat
import com.test.coinapp.data.CoinApi
import com.test.coinapp.data.CurrenciesRepository
import com.test.coinapp.data.CurrencyAsset
import com.test.coinapp.di.repository.CurrenciesRepositoryFake
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.junit.Assert
import org.junit.Test
import kotlin.system.measureTimeMillis

class CurrenciesSelectionInteractorTest {
    private val interactorFakeRepo = CurrenciesSelectionInteractor(
        CurrenciesRepositoryFake(
            CoinApi()
        )
    )

    private val interactorRealRepo = CurrenciesSelectionInteractor(
        CurrenciesRepository(
            CoinApi()
        )
    )

    @Test
    fun initWorkFlowFake() {
        initWorkFlow(interactorFakeRepo, timeout = CurrenciesRepositoryFake.RESPONSE_DELAY + 2000L)
    }

    @Test
    fun initWorkFlowReal() {
        initWorkFlow(interactorRealRepo, 10000)
    }

    private fun initWorkFlow(interactor: CurrenciesSelectionInteractor, timeout: Long) {
        runBlocking {
            withTimeout(timeout) {
                val onWaiting = { isPending: Boolean ->
                    println("Data pending: $isPending")
                }
                val onComplete = { result: List<CurrencyAsset>? ->
                    println("Result: $result")
                    interactor.repeatRequestDeclined()
                }
                val onShutdown = { _: Unit ->
                    println("Shut down")
                    interactor.cancelAll()
                }
                subscribeForChannel(this, interactor.onCurrenciesListData, onComplete)
                subscribeForChannel(this, interactor.dataIsPending, onWaiting)
                subscribeForChannel(this, interactor.onNeedShutDown, onShutdown)
                println("CurrenciesSelectionInteractor: Init workflow")
                val blockingTime = measureTimeMillis {
                    interactor.initWorkFlow(this)
                }
                assertThat(blockingTime).isLessThan(100)
                println("Blocked during $blockingTime ms")
            }
        }
    }

    @Test
    fun cancelAllTestFake() {
        cancelAllTest(interactorFakeRepo, CurrenciesRepositoryFake.RESPONSE_DELAY + 2000L)
    }

    @Test
    fun cancelAllTestReal() {
        cancelAllTest(interactorRealRepo, 10000)
    }

    private fun cancelAllTest(interactor: CurrenciesSelectionInteractor, timeout: Long) {
        runBlocking {
            withTimeout(timeout) {
                interactor.initWorkFlow(this)
                val onComplete = { result: List<CurrencyAsset>? ->
                    Assert.fail("Result should never be returned after cancel")
                    throw CancellationException()
                }
                subscribeForChannel(this, interactor.onCurrenciesListData, onComplete)
                interactor.cancelAll()
            }
        }
    }

    @Test
    fun rootJobCancelTestFake() {
        rootJobCancelTest(interactorFakeRepo, CurrenciesRepositoryFake.RESPONSE_DELAY + 2000L)
    }

    @Test
    fun rootJobCancelTestReal() {
        rootJobCancelTest(interactorRealRepo, 10000)
    }

    private fun rootJobCancelTest(interactor: CurrenciesSelectionInteractor, timeout: Long) {
        runBlocking {
            withTimeout(timeout) {
                val onComplete = { result: List<CurrencyAsset>? ->
                    Assert.fail("Result should never be returned after cancel")
                    throw CancellationException()
                }
                subscribeForChannel(this, interactor.onCurrenciesListData, onComplete)
                launch {
                    interactor.initWorkFlow(this)
                    this.cancel()
                }
                delay(CurrenciesRepositoryFake.RESPONSE_DELAY + 1000)
                interactor.cancelAll()
            }
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
}