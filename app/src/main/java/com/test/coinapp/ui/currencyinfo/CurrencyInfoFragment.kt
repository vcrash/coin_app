package com.test.coinapp.ui.currencyinfo

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.coinapp.CoinApp
import com.test.coinapp.R
import com.test.coinapp.databinding.FragmentCurrencyInfoBinding
import kotlinx.android.synthetic.main.fragment_currency_info.*
import javax.inject.Inject

class CurrencyInfoFragment : Fragment() {
    @set:Inject
    lateinit var viewModel: CurrencyInfoViewModelProtocol
    private val args: CurrencyInfoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
    }

    private fun inject() {
        val application = context?.applicationContext as? CoinApp ?: return
        application.appInjector.inject(this, args.currencyCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        with (FragmentCurrencyInfoBinding.inflate(inflater, container, false)) {
            setupListView(recyclerView)
            setupSearchView(searchView)
            progress.visibility = View.VISIBLE
            return root
        }
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
        viewModel.getData()
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
            onNeedReturnToCurrenciesSelection.observe(viewLifecycleOwner, Observer {
                navigateBack()
            })
        }
    }

    private fun updateListView(listData: List<CurrencyRatesListItemData>, recyclerList: RecyclerView?) {
        val listView = recyclerList ?: return
        if (listData.isEmpty()) {
            listView.visibility = View.INVISIBLE
            search_view?.visibility = View.INVISIBLE
            empty_list_label?.visibility = View.VISIBLE
        } else {
            listView.visibility = View.VISIBLE
            search_view?.visibility = View.VISIBLE
            empty_list_label?.visibility = View.INVISIBLE
        }
        (listView.adapter as? RatesListAdapter)?.setDataAndNotify(listData) ?:
        getNewAdapter(listData).also {
            listView.adapter = it
        }
        recyclerList.scrollToPosition(0)
    }

    private fun getNewAdapter(listData: List<CurrencyRatesListItemData>) : RatesListAdapter {
        return RatesListAdapter(listData)
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
                viewModel.repeatRatesRequestConfirmed()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                viewModel.repeatRatesRequestDeclined()
            }
            .setOnCancelListener {
                viewModel.repeatRatesRequestDeclined()
            }
            .show()
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    companion object {
        const val TAG = "CurrencyInfoFragment"
    }
}