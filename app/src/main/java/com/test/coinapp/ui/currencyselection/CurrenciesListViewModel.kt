package com.test.coinapp.ui.currencyselection

import android.app.Application
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.test.coinapp.R
import com.test.coinapp.core.SingleEmitLiveData
import com.test.coinapp.data.CurrencyAsset
import com.test.coinapp.domain.CurrenciesSelectionInteractorProtocol
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

class CurrenciesListViewModel(
    application: Application,
    val interactor: CurrenciesSelectionInteractorProtocol
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

    override val needNavigateToRates: LiveData<out NavDirections>
        get() = navigateToRates

    override val onNeedRepeatRequestDialogData: LiveData<String>
        get() = onNeedRepeatRequestDialog

    override val onNeedShutDownData: LiveData<Unit>
        get() = onNeedShutDown

    init {
        setupLiveData()
        subscribeAll()
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

    private fun subscribeAll() {
        subscribeForChannel(interactor.dataIsPending, this::onDataPending)
        subscribeForChannel(interactor.onCurrenciesListData, this::onCurrenciesListReceived)
        subscribeForChannel(interactor.onDataReceiveError, this::onCurrenciesListFailed)
        subscribeForChannel(interactor.onNeedShutDown, this::postShutDown)
        subscribeForChannel(interactor.onNeedShowCurrencyDetails, this::onNeedShowRates)
    }

    private fun <T> subscribeForChannel(channel: ReceiveChannel<T>, onReceive: (T)->Unit) {
        val initialContext = viewModelScope.coroutineContext
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val result = channel.receive()
                withContext(initialContext) {
                    onReceive(result)
                }
            }
        }
    }

    override fun initWorkFlow() {
        interactor.initWorkFlow(viewModelScope)
    }

    override fun itemSelect(currencyCode: String) {
        interactor.onCurrencySelected(currencyCode)
    }

    private fun onNeedShowRates(currencyCode: String) {
        val action = CurrencySelectionFragmentDirections.actionToCurrencyInfo(currencyCode)
        navigateToRates.postValue(action)

    }

    override fun onCleared() {
        interactor.cancelAll()
        super.onCleared()
    }

    private fun onDataPending(state: Boolean) {
        dataPending.postValue(state)
    }

    private fun onCurrenciesListReceived(list: List<CurrencyAsset>) {
        val viewDataList = list.asSequence().mapNotNull { convertToViewData(it) }.sortedBy { it.currencyCode }.toList()
        if (viewDataList.isEmpty())
            interactor.onViewPresentationFailed()
        else
            listViewData.postValue(viewDataList)
    }

    private fun convertToViewData(item: CurrencyAsset) : CurrenciesListItemData? {
        return CurrenciesListItemData(
            currencyCode = item.assetId?.trim() ?: return null,
            currencyName = item.name?.trim() ?: EMPTY_SIGN,
            isCrypto = item.typeIsCrypto == 1
        )
    }

    private fun onCurrenciesListFailed(errorCode: Int?) {
        onNeedRepeatRequestDialog.postValue(
            getApplication<Application>().getString(R.string.request_repeat_dialog_message)
        )
    }

    override fun repeatCurrenciesRequestConfirmed() {
        interactor.repeatRequestConfirmed(viewModelScope)
    }

    override fun repeatCurrenciesRequestDeclined() {
        interactor.repeatRequestDeclined()
    }

    private fun postShutDown(value: Unit) {
        onNeedShutDown.postValue(value)
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

    companion object {
        private const val EMPTY_SIGN = "-"
    }
}