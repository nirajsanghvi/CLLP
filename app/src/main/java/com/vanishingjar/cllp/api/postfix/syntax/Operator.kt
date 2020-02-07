package com.vanishingjar.cllp.api.postfix.syntax

import com.vanishingjar.cllp.api.postfix.exception.NotAnOperatorException
import com.vanishingjar.cllp.api.postfix.syntax.operator.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

abstract class Operator(val numberOfParameters: Int, val symbol: String) : ExpressionEntity(true) {

    companion object {
        
        val PRECISION = 100
        val CONTEXT = MathContext(PRECISION, RoundingMode.HALF_UP)
        
        val PLUS = PlusOperator()
        val MINUS = MinusOperator()
        val TIMES = MultiplyOperator()
        val DIVIDE = DivideOperator()
        val POWER = PowerOperator()
        val OPEN_BRACKET = OpenBracketOperator()
        val CLOSE_BRACKET = CloseBracketOperator()
        val MODULUS = ModulusOperator()

        val OPERATORS: Array<Operator> = arrayOf(
                PLUS, MINUS, TIMES, DIVIDE, POWER, OPEN_BRACKET, CLOSE_BRACKET, MODULUS
        )

        fun isOperator(string: String): Boolean = OPERATORS.any { it.isStringEquivalent(string) }

        fun parse(string: String): Operator {
            try {
                return OPERATORS.first { operator -> operator.isStringEquivalent(string) }
            } catch (e: NoSuchElementException) {
                throw NotAnOperatorException("Operator Parse Error: $string is not an operator.")
            }
        }
        
        fun toBigDecimal(n: Number) = BigDecimal(n.toDouble(), CONTEXT)
        fun toBigDecimal(s: String) = BigDecimal(s, CONTEXT)
    }

    abstract fun operate(vararg operands: Operand): Operand

    abstract fun getPrecedence(): Int

    open fun isStringEquivalent(string: String): Boolean = string == symbol

    fun precedes(op: Operator) = getPrecedence() <= op.getPrecedence()

    fun precedesStack(stack: Stack<Operator>): Boolean {
        if (stack.empty()) return false
        return precedes(stack.peek())
    }

}

val Double.bd: BigDecimal
    get() = Operator.toBigDecimal(this)

val Int.bd: BigDecimal
    get() = Operator.toBigDecimal(this)

val Long.bd: BigDecimal
    get() = Operator.toBigDecimal(this)

val String.bd: BigDecimal
    get() = Operator.toBigDecimal(this.toDouble())