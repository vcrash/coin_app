package com.test.coinapp.data

import com.google.common.truth.Truth.assertThat
import com.test.coinapp.di.repository.CurrenciesRepositoryFake
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class RepositoryFakeTest {
    private val repository = CurrenciesRepositoryFake(CoinApi())

    @Test
    fun testCallback() {
        runBlocking {
            repository.getCurrencies(this, onSuccess = {
                assertThat(it).isNotEmpty()
            }, onFailed = {
                Assert.fail("Request failed with code $it")
            })
        }
    }
}