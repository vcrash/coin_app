package com.test.coinapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test

class CurrenciesRepositoryTest {
    private val repository: CurrenciesRepository =
        CurrenciesRepository(CoinApi())

    @Test
    fun getCurrencies() {
        runBlocking {
            repository.getCurrencies(this, {
                assertThat(it).apply {
                    isNotNull()
                    isNotEmpty()
                }
            }, {
                Assert.fail()
            })
        }
    }

    @Test
    fun getRatesFor() {
        val baseCurrency = "BTC"
        runBlocking {
            repository.getRatesFor(this, baseCurrency, {
                assertThat(it).isNotNull()
                assertThat(it?.rates).isNotEmpty()
            }, {
                Assert.fail()
            })
        }
    }

    @Test
    fun getSingleRate() {
        runBlocking {
            val baseCurrency = "BTC"
            val targetCurrency = "USD"
            repository.getExchangeRate(this, baseCurrency, targetCurrency, {
                assertThat(it).isNotNull()
                assertThat(it?.assetIdBase).isEqualTo(baseCurrency)
                assertThat(it?.assetIdQuote).isEqualTo(targetCurrency)
            }, {
                Assert.fail()
            })
        }
    }

    @Test
    fun cancelAll() {
        runBlocking {
            val cancelErrorMessage = "Not cancelled after job cancel"
            launch(Dispatchers.Default) {
                repository.getCurrencies(this, {
                    Assert.fail(cancelErrorMessage)
                }, {
                    Assert.fail(cancelErrorMessage)
                })
                val baseCurrency = "BTC"
                val targetCurrency = "USD"
                repository.getExchangeRate(this, baseCurrency, targetCurrency, {
                    Assert.fail(cancelErrorMessage)
                }, {
                    Assert.fail(cancelErrorMessage)
                })
                cancel()
            }
        }
    }
}