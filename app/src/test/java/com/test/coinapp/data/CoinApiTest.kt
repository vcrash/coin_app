package com.test.coinapp.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test


class CoinApiTest {
    private val coinApi = CoinApi()


    @Test
    fun getCurrenciesAsync() {
        runBlocking {
            val result = coinApi.getCurrenciesAsync(this).await()
            assertThat(result).isNotNull()
            assertThat(result?.data).isNotNull()
            assertThat(result?.success).isTrue()
        }
    }

    @Test
    fun getRatesAsync() {
        runBlocking {
            coinApi.getRatesForAsync(this, TEST_BASE_CURRENCY_ID).await().also {
                assertThat(it).isNotNull()
                assertThat(it?.data).isNotNull()
                assertThat(it?.data?.rates).isNotEmpty()
                assertThat(it?.success).isTrue()
            }
        }
    }

    @Test
    fun getSingleRateAsync() {
        runBlocking {
            coinApi.getExchangeRateAsync(this, TEST_BASE_CURRENCY_ID, TEST_TARGET_CURRENCY_ID).await().also {
                assertThat(it).isNotNull()
                assertThat(it?.data).isNotNull()
                assertThat(it?.success).isTrue()
            }
        }
    }

    companion object {
        const val TEST_BASE_CURRENCY_ID = "BTC"
        const val TEST_TARGET_CURRENCY_ID = "USD"
    }
}