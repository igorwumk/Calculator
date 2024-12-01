package pl.igorwumk.calculator

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    private var commaPresentInNumber = false
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
            commaPresentInNumber = false
        }
        if(char in ".") {
            // incoming comma
            // process only if comma not present in number
            if(!commaPresentInNumber) {
                if(expression.value.last() !in "0123456789") {
                    // add 0 at end of exp if no digit before
                    expression.value += "0"
                }
                expression.value += char
                commaPresentInNumber = true
            }
        }
    }

    fun removeLastCharacter() {
        if(expression.value != "0") {
            //replace digit with 0 if it's the only digit
            if(expression.value.length == 1) {
                expression.value = "0"
            }
            else {
                //reset comma present status when removing it
                if(expression.value.last() == '.') {
                    commaPresentInNumber = false
                }
                //remove character
                expression.value = expression.value.dropLast(1)
            }
        }
    }

    fun evaluate() {
        TODO("Not yet implemented")
    }

    fun clear() {
        expression.value = "0"
        commaPresentInNumber = false
    }
}