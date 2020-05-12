package com.test.coinapp.di.repository

import com.test.coinapp.data.CurrenciesRepositoryProtocol
import dagger.Module
import dagger.Provides

@Module
class CurrenciesRepositoryTestModule {
    @Provides
    fun providesRepository(repository: CurrenciesRepositoryFake) : CurrenciesRepositoryProtocol {
        return repository
    }
}