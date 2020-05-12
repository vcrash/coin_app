package com.test.coinapp.di

import com.test.coinapp.assembly.AppInjector
import com.test.coinapp.di.currencieslist.CurrencySelectionFragmentComponent
import com.test.coinapp.di.currencyinfo.CurrencyInfoFragmentComponent
import com.test.coinapp.di.repository.CurrenciesRepositoryTestModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [CurrenciesRepositoryTestModule::class])
interface AppComponent {
    fun inject(injector: AppInjector)

    fun currencyInfoFragmentComponentBuilder(): CurrencyInfoFragmentComponent.Builder

    fun currencySelectionFragmentComponentBuilder(): CurrencySelectionFragmentComponent.Builder
}