package com.example.gogolookinterview.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.gogolookinterview.R
import com.example.gogolookinterview.model.ImageHit
import com.example.gogolookinterview.paging.SearchPagingModel
import com.example.gogolookinterview.repository.SearchRepository
import com.example.gogolookinterview.room.AppDatabase
import com.example.gogolookinterview.room.SearchHistory
import com.example.gogolookinterview.utils.getViewModel
import com.example.gogolookinterview.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        getViewModel { MainViewModel(application, SearchRepository()) }
    }
    private val searchHistoryDao by lazy {
        AppDatabase.getInstance(this).searchHistoryDao()
    }
    private var currentQuery: String? = null
    private val searchSuggestions = mutableListOf<String>()
    private lateinit var searchListItems: LazyPagingItems<SearchPagingModel>
    private lateinit var lazyGridState: LazyGridState
    private val displayLayout = mutableStateOf<DisplayLayout>(DisplayLayout.List)

    sealed class DisplayLayout(val spanCount: Int) {
        object List : DisplayLayout(1)
        object Grid : DisplayLayout(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainUi()
        }
        initSearchSuggestionsFromDao()
    }

    @Composable
    private fun MainUi() {
        searchListItems = viewModel.searchListFlow.collectAsLazyPagingItems()
        val displayLayout = remember { displayLayout }
        Column {
            TopUi(displayLayout)
            PagingUi(searchListItems, displayLayout)
        }
    }

    @Composable
    private fun TopUi(displayLayout: State<DisplayLayout>) {
        Row {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                val (searchBar, searchHistoryMenu, listIcon, gridIcon) = createRefs()
                val searchBarModifier = Modifier
                    .wrapContentWidth()
                    .constrainAs(searchBar) {
                        start.linkTo(parent.start)
                        end.linkTo(listIcon.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                val searchHistoryMenuModifier = Modifier
                    .constrainAs(searchHistoryMenu) {
                        top.linkTo(searchBar.bottom)
                        start.linkTo(searchBar.start)
                        end.linkTo(searchBar.end)
                        width = Dimension.fillToConstraints
                    }
                val listIconModifier = Modifier
                    .constrainAs(listIcon) {
                        start.linkTo(searchBar.end)
                        end.linkTo(gridIcon.start, 8.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .size(24.dp)
                    .padding(4.dp)
                    .clickable {
                        changeDisplayLayout(DisplayLayout.List)
                    }
                val gridIconModifier = Modifier
                    .constrainAs(gridIcon) {
                        start.linkTo(listIcon.end)
                        end.linkTo(parent.end, 8.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .size(24.dp)
                    .clickable {
                        changeDisplayLayout(DisplayLayout.Grid)
                    }

                SearchUi(searchBarModifier, searchHistoryMenuModifier)

                Image(painter = painterResource(R.drawable.ic_list),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        colorResource(
                            getIconColor(displayLayout.value == DisplayLayout.List)
                        )
                    ),
                    modifier = listIconModifier
                )

                Image(painter = painterResource(R.drawable.ic_grid),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        colorResource(
                            getIconColor(displayLayout.value == DisplayLayout.Grid)
                        )
                    ),
                    modifier = gridIconModifier
                )
            }
        }
    }

    @Composable
    private fun SearchUi(searchBarModifier: Modifier, searchHistoryMenuModifier: Modifier) {
        val searchTextValue = remember { mutableStateOf(TextFieldValue("")) }
        val searchHistoryExpanded = remember { mutableStateOf(false) }
        val suggestions = remember { mutableStateListOf("") }
        SearchView(state = searchTextValue,
            modifier = searchBarModifier,
            onKeyboardActionSearch = {
                submitQuery(searchTextValue.value.text)
                searchHistoryExpanded.value = false
            },
            onTextClear = {
                submitQuery()
            },
            onTextChanged = {
                suggestions.clear()
                suggestions.addAll(getSuggestion(it).takeUnless { it.isEmpty() }
                    ?: searchSuggestions)
                if (suggestions.isNotEmpty()) {
                    searchHistoryExpanded.value = true
                }
            }
        )

        DropdownMenu(
            expanded = searchHistoryExpanded.value,
            onDismissRequest = { searchHistoryExpanded.value = false },
            properties = PopupProperties(focusable = false),
            modifier = searchHistoryMenuModifier.fillMaxWidth()
        ) {
            suggestions.forEach {
                DropdownMenuItem(onClick = {
                    searchTextValue.value = TextFieldValue(it)
                }) {
                    Text(text = it)
                }
            }
        }
    }

    @Composable
    private fun PagingUi(
        searchListItems: LazyPagingItems<SearchPagingModel>,
        displayLayout: State<DisplayLayout>
    ) {
        Column {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                val (searchList, refreshLoadingBar, appendLoadingBar) = createRefs()
                val searchListModifier = Modifier.constrainAs(searchList) {
                    top.linkTo(parent.top, 8.dp)
                    bottom.linkTo(
                        if (searchListItems.loadState.append is LoadState.Loading) {
                            appendLoadingBar.top
                        } else {
                            parent.bottom
                        },
                        8.dp
                    )
                    height = Dimension.fillToConstraints
                }
                val refreshLoadingBarModifier = Modifier
                    .size(48.dp)
                    .constrainAs(refreshLoadingBar) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                val appendLoadingBarModifier = Modifier
                    .size(24.dp)
                    .constrainAs(appendLoadingBar) {
                        top.linkTo(searchList.bottom, 4.dp)
                        bottom.linkTo(parent.bottom, 4.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }

                SearchList(
                    modifier = searchListModifier,
                    searchListItems,
                    displayLayout
                )
                with(searchListItems.loadState) {
                    when {
                        refresh is LoadState.Loading -> CircularProgressIndicator(
                            modifier = refreshLoadingBarModifier,
                            color = colorResource(R.color.purple_500),
                            strokeWidth = 4.dp
                        )
                        append is LoadState.Loading -> CircularProgressIndicator(
                            modifier = appendLoadingBarModifier,
                            color = colorResource(R.color.purple_500),
                            strokeWidth = 4.dp
                        )
                        append is LoadState.Error -> Unit
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchList(
        modifier: Modifier,
        searchListItems: LazyPagingItems<SearchPagingModel>,
        displayLayout: State<DisplayLayout>
    ) {
        lazyGridState = rememberLazyGridState()
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Fixed(displayLayout.value.spanCount),
            contentPadding = PaddingValues(horizontal = if (displayLayout.value == DisplayLayout.List) 0.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(searchListItems.itemCount) { index ->
                (searchListItems[index] as? SearchPagingModel.SearchPhotoUi)?.let {
                    SearchPhotoUi(it)
                }
            }
        }
    }

    @Preview
    @Composable
    private fun PreviewTopUi() {
        TopUi(remember { displayLayout })
    }

    @Preview
    @Composable
    private fun PreviewSearchUi() {
        val pagingList = listOf(
            SearchPagingModel.SearchPhotoUi(
                ImageHit(
                    id = 0,
                    type = "type",
                    tags = "tags",
                    imageURL = "",
                    imageWidth = 400,
                    imageHeight = 300,
                    previewURL = "",
                    previewHeight = 400,
                    previewWidth = 300,
                    user = "user name",
                    userImageURL = ""
                )
            ),
            SearchPagingModel.SearchPhotoUi(
                ImageHit(
                    id = 0,
                    type = "type",
                    tags = "tags",
                    imageURL = "",
                    imageWidth = 400,
                    imageHeight = 300,
                    previewURL = "",
                    previewHeight = 400,
                    previewWidth = 300,
                    user = "user name2",
                    userImageURL = ""
                )
            )
        )
        val searchListItems = flowOf<PagingData<SearchPagingModel>>(
            PagingData.from(
                pagingList
            )
        ).collectAsLazyPagingItems()
        PagingUi(searchListItems, displayLayout)
    }

    private fun search(query: String? = null) {
        viewModel.setQuery(query)
        searchListItems.refresh()
        lifecycleScope.launch {
            lazyGridState.scrollToItem(0)
        }
    }

    private fun submitQuery(query: String? = null): Boolean {
        if (currentQuery == query) return false
        currentQuery = query
        search(query)
        query.takeUnless { it.isNullOrBlank() }?.let {
            appendSearchSuggestionIfNeed(it)
        }
        return true
    }

    private fun getSuggestion(text: String): List<String> {
        val suggestions = mutableListOf<String>()
        for (i in searchSuggestions.indices) {
            if (searchSuggestions[i].lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                suggestions.add(searchSuggestions[i])
            }
        }
        return suggestions
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

    private fun changeDisplayLayout(layout: DisplayLayout) {
        displayLayout.value = layout
    }

    private fun getIconColor(isEnabled: Boolean) = if (isEnabled) {
        R.color.purple_500
    } else {
        R.color.grey
    }
}