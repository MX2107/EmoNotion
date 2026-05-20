package com.emonotion.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.emonotion.app.R
import com.emonotion.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var currentDestinationId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        com.emonotion.app.utils.ThemeManager.applyFromStorage(this)
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Настройка навигационного графа
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            if (navHostFragment != null) {
                val navController = navHostFragment.navController
                
                // Настраиваем навигационный граф
                navController.setGraph(R.navigation.nav_graph)
                
                // Настраиваем Bottom Navigation с правильной подсветкой
                binding.bottomNavigation.setupWithNavController(navController)
                
                // Отслеживаем текущую вкладку
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    currentDestinationId = destination.id
                }
                
                // Добавляем специальную обработку для навигации
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.navigation_home -> {
                            if (currentDestinationId == R.id.navigation_home) {
                                // Если уже на главной, очищаем бэкстек
                                navController.popBackStack(R.id.navigation_home, false)
                            } else {
                                // Если не на главной, переходим на нее
                                navController.navigate(R.id.navigation_home)
                            }
                            true
                        }
                        R.id.navigation_calendar -> {
                            if (currentDestinationId == R.id.navigation_calendar) {
                                navController.popBackStack(R.id.navigation_calendar, false)
                            } else {
                                navController.navigate(R.id.navigation_calendar)
                            }
                            true
                        }
                        R.id.navigation_notes -> {
                            if (currentDestinationId == R.id.navigation_notes) {
                                navController.popBackStack(R.id.navigation_notes, false)
                            } else {
                                navController.navigate(R.id.navigation_notes)
                            }
                            true
                        }
                        R.id.navigation_profile -> {
                            if (currentDestinationId == R.id.navigation_profile) {
                                navController.popBackStack(R.id.navigation_profile, false)
                            } else {
                                navController.navigate(R.id.navigation_profile)
                            }
                            true
                        }
                        else -> false
                    }
                }
                
            } else {
                // Fallback если NavHostFragment не найден
                setupFallbackNavigation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Если есть проблемы, используем fallback
            setContentView(R.layout.activity_main)
        }
    }
    
    private fun setupFallbackNavigation() {
        // Базовая настройка если навигация не работает
        try {
            val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, navHostFragment)
                .commit()
            
            // Ждем завершения транзакции перед настройкой bottom navigation
            supportFragmentManager.executePendingTransactions()
            
            val navController = navHostFragment.navController
            
            binding.bottomNavigation.setupWithNavController(navController)
            
            // Добавляем обработку для возврата на главную при повторных нажатиях
            binding.bottomNavigation.setOnItemReselectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navController.popBackStack(R.id.navigation_home, false)
                    }
                    R.id.navigation_calendar -> {
                        navController.popBackStack(R.id.navigation_calendar, false)
                    }
                    R.id.navigation_notes -> {
                        navController.popBackStack(R.id.navigation_notes, false)
                    }
                    R.id.navigation_profile -> {
                        navController.popBackStack(R.id.navigation_profile, false)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
