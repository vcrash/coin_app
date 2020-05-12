package com.test.coinapp.di.currencieslist

import android.app.Application
import com.test.coinapp.core.getViewModel
import com.test.coinapp.domain.CurrenciesSelectionInteractorProtocol
import com.test.coinapp.ui.currencyselection.CurrenciesListViewModelProtocol
import com.test.coinapp.ui.currencyselection.CurrencySelectionFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class CurrencySelectionViewModelTestModule {
    @Provides
    fun provideViewModel(
        @Named(CurrencySelectionFragmentComponent.BIND_NAME_FRAGMENT)
        fragment: CurrencySelectionFragment,
        @Named(CurrencySelectionFragmentComponent.BIND_NAME_APPLICATION)
        application: Application,
        interactor: CurrenciesSelectionInteractorProtocol
    ) : CurrenciesListViewModelProtocol {
        return fragment.getViewModel creator@{
            return@creator CurrenciesListViewModelFake(application, interactor)
        }
    }
}