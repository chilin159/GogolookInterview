package com.example.gogolookinterview.view

import android.content.Context
import android.database.MatrixCursor
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.BaseColumns
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gogolookinterview.R
import com.example.gogolookinterview.repository.SearchRepository
import com.example.gogolookinterview.room.AppDatabase
import com.example.gogolookinterview.room.SearchHistory
import com.example.gogolookinterview.utils.getViewModel
import com.example.gogolookinterview.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        getViewModel { MainViewModel(application, SearchRepository())}
    }
    private lateinit var searchPagingAdapter: SearchPagingAdapter
    private var searchJob: Job? = null
    private val searchHistoryDao by lazy {
        AppDatabase.getInstance(this).searchHistoryDao()
    }
    private var currentQuery: String? = null
    private val cursorColumnName = "Name"
    private val searchSuggestions = mutableListOf<String>()
    private val searchSuggestionsAdapter by lazy {
        val from = arrayOf(cursorColumnName)
        val to = intArrayOf(android.R.id.text1)
        SimpleCursorAdapter(
            this@MainActivity,
            android.R.layout.simple_list_item_1,
            null,
            from,
            to,
            0
        )
    }

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
        searchView.setup()
        initSearchSuggestionsFromDao()
    }

    private fun RecyclerView.setup() {
        layoutManager = GridLayoutManager(context, DisplayLayout.List.spanCount)
        adapter = SearchPagingAdapter().also {
            searchPagingAdapter = it
        }
        addItemDecoration(SearchItemDecoration())
    }

    private fun SearchView.setup() {
        setIconifiedByDefault(false)
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = submitQuery(query)
            override fun onQueryTextChange(newText: String) = if (newText.isEmpty()) {
                submitQuery()
            } else {
                getSuggestion(newText)
                false
            }
        })
        queryHint = "Search image"
        suggestionsAdapter = searchSuggestionsAdapter
        setOnSuggestionListener(object: SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val selectedQuery = searchSuggestionsAdapter.cursor.getString(1)
                submitQuery(selectedQuery)
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }
        })
    }

    private fun search(query: String? = null) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.getSearchListFlow(query).collectLatest { pagingData ->
                searchPagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun submitQuery(query: String? = null): Boolean {
        if (currentQuery == query) return false
        currentQuery = query
        search(query)
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(searchView.windowToken, 0)
        searchView.clearFocus()
        query.takeUnless { it.isNullOrBlank() }?.let {
            appendSearchSuggestionIfNeed(it)
        }
        return true
    }

    private fun getSuggestion(text: String) {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, cursorColumnName))
        for (i in searchSuggestions.indices) {
            if (searchSuggestions[i].lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                cursor.addRow(arrayOf<Any>(i, searchSuggestions[i]))
            }
        }
        searchSuggestionsAdapter.changeCursor(cursor)
    }

    private fun initSearchSuggestionsFromDao() {
        lifecycleScope.launch(Dispatchers.IO) {
            searchSuggestions.addAll(searchHistoryDao.getAll().map { it.searchHistory })
        }
    }

    private fun appendSearchSuggestionIfNeed(query: String) {
        if (searchSuggestions.contains(query)) return
        lifecycleScope.launch(Dispatchers.IO) {
            searchHistoryDao.insertAll(SearchHistory(null, query))
        }
        searchSuggestions.add(query)
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