package com.example.gogolookinterview.view

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gogolookinterview.R
import com.example.gogolookinterview.repository.SearchRepository
import com.example.gogolookinterview.utils.getViewModel
import com.example.gogolookinterview.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        getViewModel { MainViewModel(application, SearchRepository())}
    }
    private lateinit var searchPagingAdapter: SearchPagingAdapter
    private var searchJob: Job? = null
    private var currentQuery: String? = null

    sealed class DisplayLayout(val spanCount: Int) {
        object List: DisplayLayout(1)
        object Grid: DisplayLayout(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        search()
    }

    private fun initView() {
        recyclerView.setup()
        listIcon.setOnClickListener {
            changeDisplayLayout(DisplayLayout.List)
        }
        gridIcon.setOnClickListener {
            changeDisplayLayout(DisplayLayout.Grid)
        }
        with(searchView) {
            setIconifiedByDefault(false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = setQuery(query)
                override fun onQueryTextChange(newText: String) = if (newText.isEmpty()) {
                    setQuery()
                } else {
                    false
                }
            })
            queryHint = "Search image"
        }
    }

    private fun RecyclerView.setup() {
        layoutManager = GridLayoutManager(context, DisplayLayout.List.spanCount)
        adapter = SearchPagingAdapter().also {
            searchPagingAdapter = it
        }
        addItemDecoration(SearchItemDecoration())
    }

    private fun search(query: String? = null) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.getSearchListFlow(query).collectLatest { pagingData ->
                searchPagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun setQuery(query: String? = null): Boolean {
        if (currentQuery == query) return false
        currentQuery = query
        search(query)
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(searchView.windowToken, 0)
        searchView.clearFocus()
        return true
    }

    private fun changeDisplayLayout(displayLayout: DisplayLayout) {
        (recyclerView.layoutManager as GridLayoutManager).spanCount = displayLayout.spanCount
        (recyclerView.adapter as SearchPagingAdapter).onSpanCountChange()
        listIcon.setEnableState(displayLayout is DisplayLayout.List)
        gridIcon.setEnableState(displayLayout is DisplayLayout.Grid)
    }

    private fun ImageView.setEnableState(isEnabled: Boolean) {
        setColorFilter(
            ContextCompat.getColor(this@MainActivity,
                if (isEnabled) R.color.purple_500
                else R.color.grey)
            ,PorterDuff.Mode.SRC_IN)
    }
}