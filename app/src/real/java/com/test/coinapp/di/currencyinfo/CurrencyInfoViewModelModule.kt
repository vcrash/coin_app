package com.test.coinapp.di.currencyinfo

import android.app.Application
import com.test.coinapp.core.getViewModel
import com.test.coinapp.domain.CurrenciesInfoInteractorProtocol
import com.test.coinapp.ui.currencyinfo.CurrencyInfoFragment
import com.test.coinapp.ui.currencyinfo.CurrencyInfoViewModel
import com.test.coinapp.ui.currencyinfo.CurrencyInfoViewModelProtocol

import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class CurrencyInfoViewModelModule {
    @Provides
    fun provideViewModel(
        @Named(CurrencyInfoFragmentComponent.BIND_NAME_FRAGMENT)
        fragment: CurrencyInfoFragment,
        @Named(CurrencyInfoFragmentComponent.BIND_NAME_APPLICATION)
        application: Application,
        @Named(CurrencyInfoFragmentComponent.BIND_CURRENCY_CODE)
        currencyCode: String,
        interactor: CurrenciesInfoInteractorProtocol
    ) : CurrencyInfoViewModelProtocol {
        return fragment.getViewModel creator@{
            CurrencyInfoViewModel(
                application,
                currencyCode,
                interactor
            )
        }
    }
}