package pl.igorwumk.calculator

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    val expression = mutableStateOf("0")

    fun append(char: String) {
        if(char in "123456789") {
            if(expression.value == "0") {
                // incoming 1-9, 0 in exp -> replace 0 with incoming value
                expression.value = char
            }
            else {
                // incoming 1-9 -> add at end of exp
                expression.value += char
            }
        }
        if(char in "0" && expression.value != "0") {
            // incoming 0, no exclusive 0 in exp -> add 0 at end of exp
            expression.value += char
        }
        if(char in "+-*/") {
            // incoming operator
            if(expression.value.last() in "+-*/") {
                // remove operator on exp end if already existing
                expression.value = expression.value.dropLast(1)
            }
            // put operator at end of exp
            expression.value += char
        }
    }
}