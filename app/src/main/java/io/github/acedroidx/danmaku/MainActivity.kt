package io.github.acedroidx.danmaku

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.data.ServiceRepository
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.StartPage
import io.github.acedroidx.danmaku.ui.home.HomeCompose
import io.github.acedroidx.danmaku.ui.log.LogCompose
import io.github.acedroidx.danmaku.ui.profile.ProfilesCompose
import io.github.acedroidx.danmaku.ui.settings.SettingsCompose
import io.github.acedroidx.danmaku.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var mService: DanmakuService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.d("MainActivity", "onServiceConnected")
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("MainActivity", "onServiceDisconnected")
            mBound = false
        }
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var serviceRepository: ServiceRepository

    @SuppressLint("ResourceType")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        Intent(this, DanmakuService::class.java).also { intent ->
            this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val items = listOf(
                StartPage.HOME,
                StartPage.PROFILE,
                StartPage.LOG,
                StartPage.SETTING,
            )
            var showBottomBar by remember { mutableStateOf(true) }
            var screenName by remember { mutableStateOf(StartPage.HOME.displayName) }
            AppTheme {
                Scaffold(topBar = {
                    TopAppBar(title = { Text(screenName) })
                }) { innerPadding ->
                    Box(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController,
                            startDestination = StartPage.HOME.route,
                        ) {
                            composable(StartPage.HOME.route) {
                                ScrollWarper(onScroll = {
                                    showBottomBar = it
                                }) { HomeCompose.MyComposable(mainViewModel) }
                            }
                            composable(StartPage.PROFILE.route) {
                                ProfilesCompose.MyComposable(mainViewModel)
                            }
                            composable(StartPage.LOG.route) {
                                ScrollWarper(onScroll = {
                                    showBottomBar = it
                                }) { LogCompose.LogView(vm = mainViewModel) }
                            }
                            composable(StartPage.SETTING.route) {
                                ScrollWarper(onScroll = {
                                    showBottomBar = it
                                }) { SettingsCompose.SettingsView() }
                            }
                        }
                        AnimatedVisibility(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            visible = showBottomBar,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            NavigationBar {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                items.forEach { screen ->
                                    NavigationBarItem(icon = {
                                        Icon(
                                            painterResource(id = screen.icon),
                                            contentDescription = null
                                        )
                                    },
                                        label = { Text(screen.displayName) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            screenName = screen.displayName
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
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }


        /*
                // https://stackoverflow.com/questions/51929290/is-it-possible-to-set-startdestination-conditionally-using-android-navigation-ar
                val navController =
                    binding.navHostFragmentActivityMain.getFragment<NavHostFragment>().navController
                val inflater = navController.navInflater
                val graph = inflater.inflate(R.navigation.mobile_navigation2)
                lifecycleScope.launch {
                    val pageId = settingsRepository.getSettings().startPage.id
                    Log.d("MainActivity.onCreate", pageId.toString())
                    graph.setStartDestination(pageId)
                    navController.graph = graph
                }
                navController.addOnDestinationChangedListener { controller, destination, arguments ->
                    Log.d("MainActivity.onCreate", "onDestinationChanged: " + destination.id)
                    val page = StartPage.findById(destination.id)
                    if (page == null) {
                        Log.d("MainActivity.onCreate", "pageId==null")
                        return@addOnDestinationChangedListener
                    }
                    lifecycleScope.launch {
                        settingsRepository.setSettingByKey(SettingsKey.START_PAGE.value, page.str)
                    }
                }

                setContentView(binding.root)

                val navView: BottomNavigationView = binding.navView

                // Passing each menu ID as a set of Ids because each
                // menu should be considered as top level destinations.
                val appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.navigation_home,
                        R.id.navigation_profiles,
                        R.id.navigation_log,
                        R.id.navigation_notifications
                    )
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
         */
    }

    @Composable
    fun ScrollWarper(onScroll: (showBottomBar: Boolean) -> Unit, content: @Composable () -> Unit) {
        Box(
            Modifier
                .verticalScroll(rememberScrollState())
                .scrollable(orientation = Orientation.Vertical,
                    state = rememberScrollableState { delta ->
                        onScroll(delta > 0)
                        0f
                    })
        ) { content() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
        this.unbindService(connection)
        mBound = false
    }
}