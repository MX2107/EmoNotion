package com.emonotion.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.emonotion.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
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
                binding.bottomNavigation.setupWithNavController(navController)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
