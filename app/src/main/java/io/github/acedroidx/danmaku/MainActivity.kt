package io.github.acedroidx.danmaku

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.data.settings.SettingsKey
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.databinding.ActivityMainBinding
import io.github.acedroidx.danmaku.model.StartPage
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

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
                R.id.navigation_home, R.id.navigation_log, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}