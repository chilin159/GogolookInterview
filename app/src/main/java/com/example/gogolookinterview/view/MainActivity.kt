package com.example.gogolookinterview.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.setup()
        search()
    }

    private fun RecyclerView.setup() {
        adapter = SearchPagingAdapter().also {
            searchPagingAdapter = it
        }
    }

    private fun search() {
        lifecycleScope.launch {
            viewModel.getSearchListFlow().collectLatest { pagingData ->
                searchPagingAdapter.submitData(pagingData)
            }
        }
    }
}