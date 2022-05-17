package com.example.gogolookinterview.view

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gogolookinterview.R
import com.example.gogolookinterview.repository.SearchRepository
import com.example.gogolookinterview.utils.getViewModel
import com.example.gogolookinterview.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        getViewModel { MainViewModel(application, SearchRepository())}
    }
    private lateinit var searchPagingAdapter: SearchPagingAdapter
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
    }

    private fun RecyclerView.setup() {
        layoutManager = GridLayoutManager(context, DisplayLayout.List.spanCount)
        adapter = SearchPagingAdapter().also {
            searchPagingAdapter = it
        }
        addItemDecoration(SearchItemDecoration())
    }

    private fun search() {
        lifecycleScope.launch {
            viewModel.getSearchListFlow().collectLatest { pagingData ->
                searchPagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun changeDisplayLayout(displayLayout: DisplayLayout) {
        (recyclerView.layoutManager as GridLayoutManager).spanCount = displayLayout.spanCount
        (recyclerView.adapter as SearchPagingAdapter).onSpanCountChange()
        listIcon.setColorFilter(
            ContextCompat.getColor(this,
                if (displayLayout is DisplayLayout.List) R.color.black
                else R.color.grey)
            ,PorterDuff.Mode.SRC_IN)
        gridIcon.setEnableState(displayLayout is DisplayLayout.Grid)
    }

    private fun ImageView.setEnableState(isEnabled: Boolean) {
        setColorFilter(
            ContextCompat.getColor(this@MainActivity,
                if (isEnabled) R.color.black
                else R.color.grey)
            ,PorterDuff.Mode.SRC_IN)
    }
}