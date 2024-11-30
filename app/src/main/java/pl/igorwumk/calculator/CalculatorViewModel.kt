package pl.igorwumk.calculator

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    val expression = mutableStateOf("0")

    fun append(char: String) {
        expression.value += char
    }
}