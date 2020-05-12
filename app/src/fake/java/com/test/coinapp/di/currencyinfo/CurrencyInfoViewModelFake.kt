package com.test.coinapp.di.currencyinfo

import android.app.Application
import androidx.lifecycle.*
import com.test.coinapp.R
import com.test.coinapp.core.SingleEmitLiveData
import com.test.coinapp.domain.CurrenciesInfoInteractorProtocol
import com.test.coinapp.ui.currencyinfo.CurrencyInfoViewModelProtocol
import com.test.coinapp.ui.currencyinfo.CurrencyRatesListItemData
import kotlinx.coroutines.*
import java.util.*

class CurrencyInfoViewModelFake(
    application: Application,
    override val currencyCode: String,
    val interactor: CurrenciesInfoInteractorProtocol
) : AndroidViewModel(application),
    CurrencyInfoViewModelProtocol {
    private val dataPending = MutableLiveData<Boolean>()
    private val listViewData = MutableLiveData<List<CurrencyRatesListItemData>>()
    private val searchStringData = MutableLiveData("")
    private val filteredList = MediatorLiveData<List<CurrencyRatesListItemData>>()
    private val progressState = MediatorLiveData<Boolean>()
    private val screenTitle = MutableLiveData("")
    private val onNeedRepeatRequestDialog = SingleEmitLiveData<String>()
    private val navigateBackToCurrenciesList = SingleEmitLiveData<Unit>()

    override val listData: LiveData<List<CurrencyRatesListItemData>>
        get() = listViewData

    override val progressData: LiveData<Boolean>
        get() = progressState

    override val screenTitleData: LiveData<String>
        get() = screenTitle

    override val onNeedRepeatRequestDialogData: LiveData<String>
        get() = onNeedRepeatRequestDialog

    override fun repeatRatesRequestConfirmed() {
    }

    override fun repeatRatesRequestDeclined() {
    }

    override val onNeedReturnToCurrenciesSelection: LiveData<Unit>
        get() = navigateBackToCurrenciesList

    init {
        setupLiveData()
    }

    private fun setupLiveData() {
        progressState.addSource(dataPending) { progressState.postValue(it) }
        filteredList.addSource(searchStringData) {
            applyFilter()
        }
        filteredList.addSource(listViewData) {
            applyFilter()
        }
    }

    override fun getData() {
        val ctx = getApplication<Application>().applicationContext
        screenTitle.postValue(ctx.getString(R.string.currency_details_title, currencyCode))
        listViewData.value = getDummyList()
        dataPending.postValue(false)
    }

    override fun filterSearch(searchString: String) {
        searchStringData.postValue(searchString)
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

    private fun applyFilterAsync(scope: CoroutineScope, list: List<CurrencyRatesListItemData>, searchString: String?) : Deferred<List<CurrencyRatesListItemData>> {
        return scope.async {
            val query = searchString?.trim()?.toLowerCase(Locale.getDefault()) ?: ""
            if (query.isBlank()) return@async list
            val substringIndex = contains@{ string: String ->
                return@contains string.toLowerCase(Locale.getDefault()).indexOf(query)
            }
            return@async list
                .asSequence()
                .map {
                    Pair(it, substringIndex(it.currencyCode))
                }
                .filter { it.second >= 0  }
                .sortedByDescending {
                    if (it.second == 0) 1 else 0
                }.map { it.first }
                .toList()
        }
    }

    private fun getDummyList() : List<CurrencyRatesListItemData> {
        return listOf(
            CurrencyRatesListItemData(
                currencyCode = "USD",
                rate = "6992.52"
            ),
            CurrencyRatesListItemData(
                currencyCode = "EUR",
                rate = "6458.058"
            )
        )
    }
}