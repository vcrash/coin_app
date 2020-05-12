package com.test.coinapp.ui.currencyinfo

import android.app.Application
import androidx.lifecycle.*
import com.test.coinapp.R
import com.test.coinapp.core.SingleEmitLiveData
import com.test.coinapp.data.Rates
import com.test.coinapp.domain.CurrenciesInfoInteractorProtocol
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

class CurrencyInfoViewModel(
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
        get() = filteredList

    override val progressData: LiveData<Boolean>
        get() = progressState

    override val screenTitleData: LiveData<String>
        get() = screenTitle

    override val onNeedRepeatRequestDialogData: LiveData<String>
        get() = onNeedRepeatRequestDialog

    override val onNeedReturnToCurrenciesSelection: LiveData<Unit>
        get() = navigateBackToCurrenciesList

    init {
        setupLiveData()
        subscribeAll()
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

    private fun subscribeAll() {
        viewModelScope.launch {
            subscribeForChannel(interactor.dataIsPending, this@CurrencyInfoViewModel ::onDataPending)
            subscribeForChannel(interactor.ratesListData, this@CurrencyInfoViewModel ::onListDataReceived)
            subscribeForChannel(interactor.onDataReceiveError, this@CurrencyInfoViewModel ::onDataGetListFailed)
            subscribeForChannel(interactor.onNeedReturnToCurrenciesSelection, this@CurrencyInfoViewModel :: postFallBack)
        }
    }

    private fun <T> subscribeForChannel(channel: ReceiveChannel<T>, onReceive: (T)->Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val result = channel.receive()
                viewModelScope.launch {
                    onReceive(result)
                }
            }
        }
    }

    override fun getData() {
        val ctx = getApplication<Application>().applicationContext
        screenTitle.postValue(ctx.getString(R.string.currency_details_title, currencyCode))
        interactor.getRates(viewModelScope)
    }

    override fun onCleared() {
        interactor.cancelAll()
        super.onCleared()
    }

    private fun onDataPending(state: Boolean) {
        dataPending.postValue(state)
    }

    private fun onListDataReceived(rates: Rates) {
        val viewDataConverted = convertToViewData(rates)
        listViewData.value = viewDataConverted
    }

    private fun convertToViewData(rates: Rates?) : List<CurrencyRatesListItemData> {
        val rateFormat = getApplication<Application>().getString(R.string.currency_rate_value_format)
        return rates?.rates?.mapNotNull {
            CurrencyRatesListItemData(
                currencyCode = it.assetIdQuote?.trim() ?: return@mapNotNull null,
                rate = it.rate?.let { rateValue ->
                    String.format(rateFormat, rateValue)
                } ?: EMPTY_RATE
            )
        } ?: emptyList()
    }

    private fun onDataGetListFailed(errorCode: Int?) {
        onNeedRepeatRequestDialog.postValue(
            getApplication<Application>().getString(R.string.request_repeat_dialog_message)
        )
    }

    override fun repeatRatesRequestConfirmed() {
        interactor.repeatRequestConfirmed(viewModelScope)
    }

    override fun repeatRatesRequestDeclined() {
        interactor.repeatRequestDeclined()
    }

    private fun postFallBack(value: Unit) {
        navigateBackToCurrenciesList.postValue(Unit)
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

    companion object {
        const val EMPTY_RATE = "-"
    }
}