package com.test.coinapp.di.repository


import com.test.coinapp.data.CurrenciesRepository
import com.test.coinapp.data.CurrenciesRepositoryProtocol
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CurrenciesRepositoryModule {
    @Singleton
    @Provides
    fun providesRepository(repository: CurrenciesRepository) : CurrenciesRepositoryProtocol {
        return repository
    }

}