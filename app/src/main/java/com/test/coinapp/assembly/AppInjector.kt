package com.test.coinapp.assembly

import com.test.coinapp.CoinApp
import com.test.coinapp.di.AppComponent
import com.test.coinapp.di.DaggerAppComponent
import com.test.coinapp.ui.currencyinfo.CurrencyInfoFragment
import com.test.coinapp.ui.currencyselection.CurrencySelectionFragment

class AppInjector(val application: CoinApp) {
    private val appComponent: AppComponent = DaggerAppComponent.builder().build()

    fun inject(fragment: CurrencySelectionFragment) {
        val component = appComponent.currencySelectionFragmentComponentBuilder()
            .fragment(fragment)
            .application(application)
            .build()
        component.inject(fragment)
    }

    fun inject(fragment: CurrencyInfoFragment, currencyCode: String) {
        val component = appComponent.currencyInfoFragmentComponentBuilder()
            .fragment(fragment)
            .application(application)
            .currencyCode(currencyCode)
            .build()
        component.inject(fragment)
    }
}