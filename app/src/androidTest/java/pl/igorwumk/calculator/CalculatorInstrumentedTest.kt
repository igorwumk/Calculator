package pl.igorwumk.calculator

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CalculatorInstrumentedTest {
    private val viewModel = CalculatorViewModel()
    companion object {
        init {
            System.loadLibrary("calculator")
        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pl.igorwumk.calculator", appContext.packageName)
    }

    @Test
    fun addition_isCorrect() {
        viewModel.expression.value = "2+2"
        viewModel.evaluate()
        assertEquals((4).toString(), viewModel.expression.value)
    }

    @Test
    fun subtraction_isCorrect() {
        viewModel.expression.value = "13-8"
        viewModel.evaluate()
        assertEquals((5).toString(), viewModel.expression.value)
    }

    @Test
    fun multiplication_isCorrect() {
        viewModel.expression.value = "50*2"
        viewModel.evaluate()
        assertEquals((100).toString(), viewModel.expression.value)
    }

    @Test
    fun division_isCorrect() {
        viewModel.expression.value = "1700/100"
        viewModel.evaluate()
        assertEquals((17).toString(), viewModel.expression.value)
    }

    @Test
    fun multipleOperationsInARow() {
        viewModel.expression.value = "1+2-3+4*5-6/10"
        viewModel.evaluate()
        assertEquals((19.4).toString(), viewModel.expression.value)
    }

    @Test
    fun divisionByZero_properlyHandled() {
        viewModel.expression.value = "3/0"
        viewModel.evaluate()
        assertTrue(viewModel.faultState)
    }
}