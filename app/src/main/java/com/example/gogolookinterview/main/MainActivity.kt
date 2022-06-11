package com.example.gogolookinterview.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gogolookinterview.R
import com.example.gogolookinterview.home.HomeScreen
import com.example.gogolookinterview.theme.AppTheme


class MainActivity : AppCompatActivity() {
    sealed class Screen(
        val route: String,
        val title: String,
        @DrawableRes val selectedIconRes: Int,
        @DrawableRes val normalIconRes: Int
    ) {
        object Home : Screen(
            "home",
            "home",
            R.drawable.ic_consultation_selected,
            R.drawable.ic_consultation_normal
        )

        object Second : Screen(
            "second",
            "second",
            R.drawable.ic_chatroom_selected,
            R.drawable.ic_chatroom_normal
        )
    }

    private val items = listOf(
        Screen.Home,
        Screen.Second,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BottomNav()
        }
    }

    @Composable
    private fun BottomNav() {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomBar(navController) }
        ) { innerPadding ->
            NavigationSetup(navController, innerPadding)
        }
    }

    @Composable
    private fun BottomBar(navController: NavHostController) {
        BottomNavigation(
            backgroundColor = AppTheme.colors.background
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavItem(selected, screen) {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.NavItem(selected: Boolean, screen: Screen, clickAction: () -> Unit) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painterResource(
                        id = if (selected)
                            screen.selectedIconRes
                        else
                            screen.normalIconRes
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            },
            label = { Text(screen.title) },
            selected = selected,
            selectedContentColor = AppTheme.colors.textPrimary,
            unselectedContentColor = AppTheme.colors.textSecondary,
            onClick = clickAction
        )
    }

    @Composable
    private fun NavigationSetup(navController : NavHostController, innerPadding: PaddingValues) {
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Second.route) { SecondUi() }
        }
    }

    @Preview
    @Composable
    private fun PreviewBottomBar() {
        val navController = rememberNavController()
        BottomBar(navController)
    }

    @Preview
    @Composable
    private fun PreviewSelectedNavItem() {
        BottomNavigation {
            items.forEach { screen ->
                NavItem(selected = true, screen) {}
            }
        }
    }

    @Composable
    private fun SecondUi() {
        Text("Second screen")
    }
}