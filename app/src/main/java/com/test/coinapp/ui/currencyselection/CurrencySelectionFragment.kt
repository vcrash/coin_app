package com.test.coinapp.ui.currencyselection

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.coinapp.CoinApp
import com.test.coinapp.R
import com.test.coinapp.databinding.FragmentCurrencySelectionBinding
import kotlinx.android.synthetic.main.fragment_currency_selection.*
import javax.inject.Inject

class CurrencySelectionFragment : Fragment() {
    @set:Inject
    lateinit var viewModel: CurrenciesListViewModelProtocol

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        with (FragmentCurrencySelectionBinding.inflate(inflater, container, false)) {
            setupListView(recyclerView)
            setupSearchView(searchView)
            progress.visibility = View.VISIBLE
            return root
        }
    }

    private fun inject() {
        val application = context?.applicationContext as? CoinApp ?: return
        application.appInjector.inject(this)
    }

    private fun setupListView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        updateListView(emptyList(), recyclerView)
    }

    private fun setupSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterSearch(query ?: return false)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterSearch(newText ?: return false)
                return true
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribeForData()
        viewModel.initWorkFlow()
    }

    private fun subscribeForData() {
        with(viewModel) {
            listData.observe(viewLifecycleOwner, Observer {
                updateListView(it, recycler_view)
            })
            progressData.observe(viewLifecycleOwner, Observer {
                setProgressVisible(it)
            })
            screenTitleData.observe(viewLifecycleOwner, Observer {
                updateTitle()
            })
            onNeedRepeatRequestDialogData.observe(viewLifecycleOwner, Observer {
                showRequestRepeatDialog(it)
            })
            onNeedShutDownData.observe(viewLifecycleOwner, Observer {
                shutDown()
            })
            needNavigateToRates.observe(viewLifecycleOwner, Observer {
                navigateToRates(it)
            })
        }
    }

    private fun updateListView(listData: List<CurrenciesListItemData>, recyclerList: RecyclerView?) {
        val listView = recyclerList ?: return
        (listView.adapter as? CurrenciesListAdapter)?.setDataAndNotify(listData) ?:
            getNewAdapter(listData).also {
                listView.adapter = it
            }
        recyclerList.scrollToPosition(0)
    }

    private fun getNewAdapter(listData: List<CurrenciesListItemData>) : CurrenciesListAdapter {
        return CurrenciesListAdapter(listData, onItemSelect = this::onItemSelect)
    }

    private fun onItemSelect(code: String) {
        viewModel.itemSelect(code)
    }

    private fun navigateToRates(action: NavDirections) {
        findNavController().navigate(action)
    }

    private fun setProgressVisible(visible: Boolean) {
        progress?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        updateTitle()
    }

    private fun updateTitle() {
        activity?.title = viewModel.screenTitleData.value
    }

    private fun showRequestRepeatDialog(message: String) {
        AlertDialog.Builder(context ?: return)
            .setMessage(message)
            .setPositiveButton(R.string.request_repeat_confirm_action) { _, _ ->
                viewModel.repeatCurrenciesRequestConfirmed()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                viewModel.repeatCurrenciesRequestDeclined()
            }
            .setOnCancelListener {
                viewModel.repeatCurrenciesRequestDeclined()
            }
            .show()
    }

    private fun shutDown() {
        activity?.finish()
    }

    companion object {
        const val TAG = "CurrencySelectionFragment"
    }
}