package com.example.gogolookinterview.home

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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.gogolookinterview.App
import com.example.gogolookinterview.R
import com.example.gogolookinterview.model.ImageHit
import com.example.gogolookinterview.paging.SearchPagingModel
import com.example.gogolookinterview.repository.SearchRepository
import com.example.gogolookinterview.room.AppDatabase
import com.example.gogolookinterview.utils.getViewModel
import com.example.gogolookinterview.view.SearchView
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.*

private lateinit var lazyGridState: LazyGridState

data class UiState(
    val displayLayout: MutableState<HomeViewModel.DisplayLayout>,
    val searchSuggestions: MutableList<String>
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = getViewModel {
        HomeViewModel(
            SearchRepository(),
            AppDatabase.getInstance(App.context).searchHistoryDao()
        )
    }
) {
    val uiState = viewModel.uiState
    val searchListItems = viewModel.searchListFlow.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    Column {
        TopUi(uiState,
            submitQueryAction = { query ->
                viewModel.submitQuery(query, searchListItems)
                scope.launch {
                    lazyGridState.scrollToItem(0)
                }
            },
            changeDisplayLayoutAction = {
                viewModel.changeDisplayLayout(it)
            })
        PagingUi(searchListItems, uiState)
    }
}

@Composable
private fun TopUi(
    uiState: UiState,
    submitQueryAction: (query: String?) -> Unit,
    changeDisplayLayoutAction: (displayLayout: HomeViewModel.DisplayLayout) -> Unit
) {
    val displayLayout = uiState.displayLayout
    val searchSuggestions = uiState.searchSuggestions
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
                    changeDisplayLayoutAction.invoke(HomeViewModel.DisplayLayout.List)
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
                    changeDisplayLayoutAction.invoke(HomeViewModel.DisplayLayout.Grid)
                }

            SearchUi(
                searchBarModifier,
                searchHistoryMenuModifier,
                searchSuggestions,
                submitQueryAction
            )

            Image(
                painter = painterResource(R.drawable.ic_list),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    colorResource(
                        getIconColor(displayLayout.value == HomeViewModel.DisplayLayout.List)
                    )
                ),
                modifier = listIconModifier
            )

            Image(
                painter = painterResource(R.drawable.ic_grid),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    colorResource(
                        getIconColor(displayLayout.value == HomeViewModel.DisplayLayout.Grid)
                    )
                ),
                modifier = gridIconModifier
            )
        }
    }
}

@Composable
private fun SearchUi(
    searchBarModifier: Modifier,
    searchHistoryMenuModifier: Modifier,
    searchSuggestions: List<String>,
    submitQueryAction: (query: String?) -> Unit
) {
    val searchTextValue = remember { mutableStateOf(TextFieldValue("")) }
    val searchHistoryExpanded = remember { mutableStateOf(false) }
    val suggestions = remember { mutableStateListOf("") }

    SearchView(state = searchTextValue,
        modifier = searchBarModifier,
        onKeyboardActionSearch = {
            submitQueryAction.invoke(searchTextValue.value.text)
            searchHistoryExpanded.value = false
        },
        onTextClear = {
            submitQueryAction.invoke(null)
        },
        onTextChanged = {
            suggestions.clear()
            suggestions.addAll(searchSuggestions.getSuggestion(it).takeUnless { it.isEmpty() }
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
    uiState: UiState
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
                uiState
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
    uiState: UiState
) {
    val displayLayout = uiState.displayLayout
    lazyGridState = rememberLazyGridState()
    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(displayLayout.value.spanCount),
        contentPadding = PaddingValues(horizontal = if (displayLayout.value == HomeViewModel.DisplayLayout.List) 0.dp else 8.dp),
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

@Preview(showBackground = true)
@Composable
private fun PreviewTopUi() {
    TopUi(previewUiState(),
        {}, {})
}

@Preview(showBackground = true)
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
    PagingUi(
        searchListItems,
        previewUiState()
    )
}


private fun List<String>.getSuggestion(text: String): List<String> {
    val suggestions = mutableListOf<String>()
    for (i in indices) {
        if (get(i).lowercase(Locale.getDefault())
                .contains(text.lowercase(Locale.getDefault()))
        ) {
            suggestions.add(get(i))
        }
    }
    return suggestions
}

private fun getIconColor(isEnabled: Boolean) = if (isEnabled) {
    R.color.purple_500
} else {
    R.color.grey
}

@Composable
private fun previewUiState() = UiState(
    remember {
        mutableStateOf(HomeViewModel.DisplayLayout.List)
    },
    mutableListOf()
)