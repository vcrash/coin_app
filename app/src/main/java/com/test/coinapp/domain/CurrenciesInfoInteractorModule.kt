package com.test.coinapp.domain

import dagger.Module
import dagger.Provides

@Module
class CurrenciesInfoInteractorModule {

    @Provides
    fun provideInteractor(interactor: CurrenciesInfoInteractor) : CurrenciesInfoInteractorProtocol {
        return interactor
    }
}