package pl.igorwumk.calculator

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlin.math.max

class CalculatorViewModel : ViewModel() {
    private var commaPresentInNumber = false
    var faultState = false
    val expression = mutableStateOf("0")

    private external fun addJNI(leftValue: Double, rightValue: Double): Double
    private external fun subtractJNI(leftValue: Double, rightValue: Double): Double
    private external fun multiplyJNI(leftValue: Double, rightValue: Double): Double
    private external fun divideJNI(leftValue: Double, rightValue: Double): Double

    private fun add(leftValue: Double, rightValue: Double): Double {
        return addJNI(leftValue, rightValue)
    }

    private fun subtract(leftValue: Double, rightValue: Double): Double {
        return subtractJNI(leftValue, rightValue)
    }

    private fun multiply(leftValue: Double, rightValue: Double): Double {
        return multiplyJNI(leftValue, rightValue)
    }

    private fun divide(leftValue: Double, rightValue: Double): Double {
        return divideJNI(leftValue, rightValue)
    }

    fun append(char: String) {
        if(faultState) {
            return
        }
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
        if(expression.value != "0" && !faultState) {
            //replace digit with 0 if it's the only digit
            if(expression.value.length == 1) {
                expression.value = "0"
            }
            else {
                //reset comma present status when removing it
                if(expression.value.last() == '.') {
                    commaPresentInNumber = false
                }
                //set comma present status if number before removed operator has one
                if(expression.value.last() in "+-*/") {
                    if('.' in removeTrailingZeros(getNumberLeftOfOperator(expression.value.length - 1).toString())) {
                        commaPresentInNumber = true
                    }
                }
                //remove character
                expression.value = expression.value.dropLast(1)
            }
        }
    }

    fun evaluate() {
        if(!faultState) {
            parseMultiplicationDivision()
            parseAdditionSubtraction()
            updateCommaStatus()
        }
    }

    private fun updateCommaStatus() {
        commaPresentInNumber = '.' in expression.value
    }

    fun clear() {
        expression.value = "0"
        commaPresentInNumber = false
        faultState = false
    }

    private fun getNumberLeftOfOperator(index: Int) : Double {
        var startIndex = index - 1
        while(startIndex >= 0) {
            if(expression.value[startIndex] in "+-*/") {
                break
            }
            startIndex--
        }
        return expression.value.substring(max(startIndex, 0), index).toDouble()
    }

    private fun getNumberRightOfOperator(index: Int) : Double {
        var endIndex = index + 1
        while(endIndex < expression.value.length) {
            if(expression.value[endIndex] in "+-*/") {
                break
            }
            endIndex++
        }

        return when(endIndex) {
            expression.value.length -> expression.value.substring(index + 1).toDouble()
            else -> expression.value.substring(index + 1, endIndex).toDouble()
        }
    }

    private fun parseMultiplicationDivision() {
        var index = 0
        while(index < expression.value.length) {
            if(expression.value[index] in "*/") {
                val leftValue = getNumberLeftOfOperator(index)
                val rightValue = getNumberRightOfOperator(index)
                var replaceString = "$leftValue${expression.value[index]}$rightValue"
                replaceString = removeTrailingZeros(replaceString)
                if(expression.value[index] == '/' && rightValue == 0.0) {
                    expression.value = "can't divide by zero"
                    faultState = true
                    return
                }
                when(expression.value[index]) {
                    '*' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(multiply(leftValue, rightValue).toString()))
                    '/' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(divide(leftValue, rightValue).toString()))
                }
                index = max(0, index - removeTrailingZeros(leftValue.toString()).length)
            }
            index++
        }

        expression.value = removeTrailingZeros(expression.value)
    }

    private fun parseAdditionSubtraction() {
        var index = 0
        while(index < expression.value.length) {
            if(expression.value[index] in "+-") {
                val leftValue = getNumberLeftOfOperator(index)
                val rightValue = getNumberRightOfOperator(index)
                var replaceString = "$leftValue${expression.value[index]}$rightValue"
                replaceString = removeTrailingZeros(replaceString)
                when(expression.value[index]) {
                    '+' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(add(leftValue, rightValue).toString()))
                    '-' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(subtract(leftValue, rightValue).toString()))
                }
                index = max(0, index - removeTrailingZeros(leftValue.toString()).length)
            }
            index++
        }

        expression.value = removeTrailingZeros(expression.value)
    }

    private fun removeTrailingZeros(replaceString: String): String {
        var returnString = replaceString
        var index = 0
        while(index < returnString.length) {
            if(returnString[index] == '.') {
                var backLength = 0
                val startIndex = index
                var endIndex = index
                var skip = false
                while(index < returnString.length) {
                    endIndex = index
                    if(returnString[index] in "+-*/") {
                        break
                    }
                    if(returnString[index] in "123456789") {
                        skip = true
                        break
                    }
                    backLength++
                    index++
                }
                if(!skip) {
                    returnString = if(index != returnString.length) {
                        returnString.replaceRange(startIndex, endIndex, "")
                    } else {
                        returnString.substring(0, startIndex)
                    }
                    index -= backLength
                }
            }
            index++
        }

        return returnString
    }
}