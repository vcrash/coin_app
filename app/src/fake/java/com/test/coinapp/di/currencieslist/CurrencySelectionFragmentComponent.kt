package com.test.coinapp.di.currencieslist

import android.app.Application
import com.test.coinapp.domain.CurrenciesSelectionInteractorModule
import com.test.coinapp.ui.currencyselection.CurrencySelectionFragment
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [
    CurrencySelectionViewModelTestModule::class,
    CurrenciesSelectionInteractorModule::class
])
interface CurrencySelectionFragmentComponent {
    fun inject(fragment: CurrencySelectionFragment)

    @Subcomponent.Builder
    interface Builder {
        fun build() : CurrencySelectionFragmentComponent

        @BindsInstance
        fun fragment(
            @Named(BIND_NAME_FRAGMENT) fragment: CurrencySelectionFragment
        ) : Builder

        @BindsInstance
        fun application(
            @Named(BIND_NAME_APPLICATION) application: Application
        ) : Builder
    }

    companion object {
        const val BIND_NAME_FRAGMENT = "Fragment"
        const val BIND_NAME_APPLICATION = "Application"
    }
}