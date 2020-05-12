package com.test.coinapp.di.currencyinfo

import android.app.Application
import com.test.coinapp.domain.CurrenciesInfoInteractorModule
import com.test.coinapp.ui.currencyinfo.CurrencyInfoFragment
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [
    CurrencyInfoViewModelTestModule::class,
    CurrenciesInfoInteractorModule::class
])
interface CurrencyInfoFragmentComponent {
    fun inject(fragment: CurrencyInfoFragment)

    @Subcomponent.Builder
    interface Builder {
        fun build() : CurrencyInfoFragmentComponent

        @BindsInstance
        fun fragment(
            @Named(BIND_NAME_FRAGMENT) fragment: CurrencyInfoFragment
        ) : Builder

        @BindsInstance
        fun application(
            @Named(BIND_NAME_APPLICATION) application: Application
        ) : Builder

        @BindsInstance
        fun currencyCode(
            @Named(BIND_CURRENCY_CODE) currencyCode: String
        ) : Builder
    }

    companion object {
        const val BIND_NAME_FRAGMENT = "Fragment"
        const val BIND_NAME_APPLICATION = "Application"
        const val BIND_CURRENCY_CODE = "CurrencyCode"
    }
}