package com.test.coinapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.test.coinapp.data.CoinApi
import com.test.coinapp.data.CurrenciesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
            launch(Dispatchers.Default) {
                repository.getCurrencies(this, {
                    Assert.fail()
                }, {
                    Assert.fail()
                })
                val baseCurrency = "BTC"
                val targetCurrency = "USD"
                repository.getExchangeRate(this, baseCurrency, targetCurrency, {
                    Assert.fail()
                }, {
                    Assert.fail()
                })
                repository.cancelCalls()
            }
        }
    }
}