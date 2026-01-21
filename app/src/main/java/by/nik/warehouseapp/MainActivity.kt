package by.nik.warehouseapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.nik.warehouseapp.features.returns.ui.ReturnListActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Точка входа: сразу открываем модуль "Возвраты"
        startActivity(Intent(this, ReturnListActivity::class.java))
        finish()
    }
}