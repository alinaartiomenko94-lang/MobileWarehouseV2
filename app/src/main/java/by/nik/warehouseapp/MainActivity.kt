package by.nik.warehouseapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import by.nik.warehouseapp.features.returns.ui.ReturnListComposeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        // Точка входа: сразу открываем модуль "Возвраты"
        startActivity(Intent(this, ReturnListComposeActivity::class.java))
    }
}