package pl.igorwumk.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.igorwumk.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                val viewModel = CalculatorViewModel()
                CalculatorUI(
                    viewModel = viewModel
                )
            }
        }
        this.window.statusBarColor = this.resources.getColor(R.color.white)
    }

    companion object {
        init {
            System.loadLibrary("calculator")
        }
    }
}
