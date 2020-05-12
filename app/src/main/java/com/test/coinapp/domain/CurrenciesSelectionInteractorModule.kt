package com.test.coinapp.domain

import dagger.Module
import dagger.Provides

@Module
class CurrenciesSelectionInteractorModule {
    @Provides
    fun provideInteractor(interactor: CurrenciesSelectionInteractor) : CurrenciesSelectionInteractorProtocol {
        return interactor
    }
}