package pl.igorwumk.calculator

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlin.math.max

class CalculatorViewModel : ViewModel() {
    private var commaPresentInNumber = false
    var faultState = false
    // mutableStateOf() makes UI subscribe to changes of value
    val expression = mutableStateOf("0")

    // C++ functions
    private external fun addJNI(leftValue: Double, rightValue: Double): Double
    private external fun subtractJNI(leftValue: Double, rightValue: Double): Double
    private external fun multiplyJNI(leftValue: Double, rightValue: Double): Double
    private external fun divideJNI(leftValue: Double, rightValue: Double): Double

    // arithmetic methods (can replace C++ calls if needed)
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

    // appends character to end of expression
    fun append(char: String) {
        // do not accept input if fault condition arised
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

    // removes last character from expression
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

    // evaluates expression - holder for helper functions
    fun evaluate() {
        // only evaluate if not in a fault state
        if(!faultState) {
            parseMultiplicationDivision()
            parseAdditionSubtraction()
            updateCommaStatus()
        }
    }

    // updates information on comma presence after processing
    private fun updateCommaStatus() {
        // update value in relation to presence of comma in processed expression
        commaPresentInNumber = '.' in expression.value
    }

    // clears screen and faults
    fun clear() {
        // resets state of calculator: number and flags
        expression.value = "0"
        commaPresentInNumber = false
        faultState = false
    }

    // returns number that is left of operator at expression.value[index]
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

    // returns number that is right of operator at expression.value[index]
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

    // process multiplications and divisions in expression
    private fun parseMultiplicationDivision() {
        var index = 0
        while(index < expression.value.length) {
            // parse if multiply/divide detected
            if(expression.value[index] in "*/") {
                val leftValue = getNumberLeftOfOperator(index)
                val rightValue = getNumberRightOfOperator(index)
                // building string that is to be replaced
                var replaceString = "$leftValue${expression.value[index]}$rightValue"
                // remove redundant zeros in replace string
                replaceString = removeTrailingZeros(replaceString)
                // enter fault state if trying to divide by zero
                if(expression.value[index] == '/' && rightValue == 0.0) {
                    expression.value = "can't divide by zero"
                    faultState = true
                    return
                }
                // evaluate expression
                when(expression.value[index]) {
                    '*' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(multiply(leftValue, rightValue).toString()))
                    '/' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(divide(leftValue, rightValue).toString()))
                }
                // shift index back to point it at the right place (it's alright if we go back a little too much, but not beyond the beginning)
                index = max(0, index - removeTrailingZeros(leftValue.toString()).length)
            }
            index++
        }
        // remove trailing zeros from result
        expression.value = removeTrailingZeros(expression.value)
    }

    // process additions and subtractions in expression
    private fun parseAdditionSubtraction() {
        var index = 0
        while(index < expression.value.length) {
            // parse if add/subtract detected
            if(expression.value[index] in "+-") {
                val leftValue = getNumberLeftOfOperator(index)
                val rightValue = getNumberRightOfOperator(index)
                // building string that is to be replaced
                var replaceString = "$leftValue${expression.value[index]}$rightValue"
                // remove redundant zeros in replace string
                replaceString = removeTrailingZeros(replaceString)
                // evaluate expression
                when(expression.value[index]) {
                    '+' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(add(leftValue, rightValue).toString()))
                    '-' -> expression.value = expression.value.replaceFirst(replaceString, removeTrailingZeros(subtract(leftValue, rightValue).toString()))
                }
                // shift index back to point it at the right place
                index = max(0, index - removeTrailingZeros(leftValue.toString()).length)
            }
            index++
        }
        // remove trailing zeros from result
        expression.value = removeTrailingZeros(expression.value)
    }

    // returns expression with redundant zeros removed
    private fun removeTrailingZeros(replaceString: String): String {
        var returnString = replaceString
        var index = 0
        while(index < returnString.length) {
            // activate when comma detected
            if(returnString[index] == '.') {
                var backLength = 0
                val startIndex = index
                var endIndex = index
                var skip = false
                // when not at end of return string
                while(index < returnString.length) {
                    endIndex = index
                    // exit loop if operand detected -> we have zeros to remove
                    if(returnString[index] in "+-*/") {
                        break
                    }
                    // non-zero digit detected -> no removing
                    if(returnString[index] in "123456789") {
                        skip = true
                        break
                    }
                    backLength++
                    index++
                }
                // if detected zeros to remove
                if(!skip) {
                    // delete set range of characters, or to the end if we're at the end of return string
                    returnString = if(index != returnString.length) {
                        returnString.replaceRange(startIndex, endIndex, "")
                    } else {
                        returnString.substring(0, startIndex)
                    }
                    // shift back index
                    index -= backLength
                }
            }
            index++
        }

        return returnString
    }
}