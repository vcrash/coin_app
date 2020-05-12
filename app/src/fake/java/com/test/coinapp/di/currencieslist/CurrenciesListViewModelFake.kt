package com.test.coinapp.di.currencieslist

import android.app.Application
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.test.coinapp.R
import com.test.coinapp.core.SingleEmitLiveData
import com.test.coinapp.domain.CurrenciesSelectionInteractorProtocol
import com.test.coinapp.ui.currencyselection.CurrenciesListItemData
import com.test.coinapp.ui.currencyselection.CurrenciesListViewModelProtocol
import com.test.coinapp.ui.currencyselection.CurrencySelectionFragmentDirections
import kotlinx.coroutines.*
import java.util.*

class CurrenciesListViewModelFake(
    application: Application,
    interactor: CurrenciesSelectionInteractorProtocol
) : AndroidViewModel(application), CurrenciesListViewModelProtocol {
    private val listViewData = MutableLiveData<List<CurrenciesListItemData>>()
    private val searchStringData = MutableLiveData("")
    private val filteredList = MediatorLiveData<List<CurrenciesListItemData>>()
    private val progressState = MediatorLiveData<Boolean>()
    private val screenTitle = MediatorLiveData<String>()
    private val dataPending = MutableLiveData<Boolean>()
    private val navigateToRates = SingleEmitLiveData<CurrencySelectionFragmentDirections.ActionToCurrencyInfo>()
    private val onNeedRepeatRequestDialog = SingleEmitLiveData<String>()
    private val onNeedShutDown = SingleEmitLiveData<Unit>()

    override val listData: LiveData<List<CurrenciesListItemData>>
        get() = filteredList

    override val progressData: LiveData<Boolean>
        get() = progressState

    override val screenTitleData: LiveData<String>
        get() = screenTitle

    override fun filterSearch(searchString: String) {
        searchStringData.postValue(searchString)
    }

    override val needNavigateToRates: LiveData<out NavDirections>
        get() = navigateToRates

    override val onNeedRepeatRequestDialogData: LiveData<String>
        get() = onNeedRepeatRequestDialog

    override fun repeatCurrenciesRequestConfirmed() {
    }

    override fun repeatCurrenciesRequestDeclined() {
    }

    override fun itemSelect(currencyCode: String) {
        val action = CurrencySelectionFragmentDirections.actionToCurrencyInfo(currencyCode)
        navigateToRates.postValue(action)
    }

    override val onNeedShutDownData: LiveData<Unit>
        get() = onNeedShutDown

    init {
        setupLiveData()
    }

    private fun setupLiveData() {
        progressState.addSource(dataPending) { progressState.postValue(it) }
        screenTitle.addSource(dataPending) { screenTitle.postValue(
            getApplication<Application>().getString(
                if (it)
                    R.string.currencies_pending_title
                else
                    R.string.currency_selection_title
            )
        )
        }
        filteredList.addSource(searchStringData) {
            applyFilter()
        }
        filteredList.addSource(listViewData) {
            applyFilter()
        }
    }

    override fun initWorkFlow() {
        val ctx = getApplication<Application>().applicationContext
        screenTitle.postValue(ctx.getString(R.string.currency_selection_title))
        listViewData.value = getDummyList()
        dataPending.postValue(false)
    }

    private fun applyFilter() {
        val ctx = viewModelScope.coroutineContext
        val filtered = applyFilterAsync(viewModelScope, listViewData.value ?: emptyList(), searchStringData.value)
        viewModelScope.launch(Dispatchers.Default) {
            val result = filtered.await()
            withContext(ctx) {
                filteredList.postValue(result)
            }
        }
    }

    private fun applyFilterAsync(scope: CoroutineScope, list: List<CurrenciesListItemData>, searchString: String?) : Deferred<List<CurrenciesListItemData>> {
        return scope.async {
            val query = searchString?.trim()?.toLowerCase(Locale.getDefault()) ?: ""
            if (query.isBlank()) return@async list
            val substringIndex = contains@{ string: String ->
                return@contains string.toLowerCase(Locale.getDefault()).indexOf(query)
            }
            return@async list
                .asSequence()
                .map {
                    Triple(
                        it, substringIndex(it.currencyCode), substringIndex(it.currencyName)
                    )
                }
                .filter { it.second >= 0 || it.third >= 0 }
                .sortedByDescending {
                    when {
                        (it.second == 0) -> 2
                        (it.third == 0) -> 1
                        else -> 0
                    }
                }.map { it.first }
                .toList()
        }
    }

    private fun getDummyList() : List<CurrenciesListItemData> {
        return listOf(
            CurrenciesListItemData(
                currencyCode = "USD",
                currencyName = "US Dollar",
                isCrypto = false
            ),
            CurrenciesListItemData(
                currencyCode = "EUR",
                currencyName = "Euro",
                isCrypto = false
            ),
            CurrenciesListItemData(
                currencyCode = "BTC",
                currencyName = "Bitcoin",
                isCrypto = true
            ),
            CurrenciesListItemData(
                currencyCode = "RIPPED",
                currencyName = "RIPPED",
                isCrypto = true
            ),
            CurrenciesListItemData(
                currencyCode = "USD",
                currencyName = "US Dollar",
                isCrypto = false
            ),
            CurrenciesListItemData(
                currencyCode = "EUR",
                currencyName = "Euro",
                isCrypto = false
            ),
            CurrenciesListItemData(
                currencyCode = "BTC",
                currencyName = "Bitcoin",
                isCrypto = true
            ),
            CurrenciesListItemData(
                currencyCode = "RIPPED",
                currencyName = "RIPPED",
                isCrypto = true
            ),
            CurrenciesListItemData(
                currencyCode = "USD",
                currencyName = "US Dollar",
                isCrypto = false
            ),
            CurrenciesListItemData(
                currencyCode = "EUR",
                currencyName = "Euro",
                isCrypto = false
            ),
            CurrenciesListItemData(
                currencyCode = "BTC",
                currencyName = "Bitcoin",
                isCrypto = true
            ),
            CurrenciesListItemData(
                currencyCode = "RIPPED",
                currencyName = "RIPPED",
                isCrypto = true
            )
        )
    }
}