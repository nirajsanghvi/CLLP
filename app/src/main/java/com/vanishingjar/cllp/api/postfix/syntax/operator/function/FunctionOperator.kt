package com.vanishingjar.cllp.api.postfix.syntax.operator.function

import com.vanishingjar.cllp.api.postfix.exception.InvalidSyntaxException
import com.vanishingjar.cllp.api.postfix.syntax.Operator
import com.vanishingjar.cllp.api.postfix.syntax.operator.function.trigonometry.*

abstract class FunctionOperator internal constructor(symbol: String, numOfParams: Int) : Operator(numOfParams, symbol) {
    
    companion object {
        
        val SINE = SineFunction()
        val INVERSESINE = InverseSineFunction()
        val COSINE = CosineFunction()
        val INVERSECOSINE = InverseCosineFunction()
        val TANGENT = TangentFunction()
        val INVERSETAN = InverseTangentFunction()
        val TODEGREES = DegreesFunction()
        val TORADIANS = RadiansFunction()
        val CEILING = CeilingFunction()
        val FLOOR = FloorFunction()
        val SQRT = SquareRootFunction()
        val ROUND = RoundFunction()
        val ABSOLUTE = AbsoluteFunction()
        
        val OPERATORS = arrayOf(
                SINE, COSINE, TANGENT, TODEGREES, TORADIANS, CEILING, FLOOR, SQRT, ROUND,
                INVERSESINE, INVERSECOSINE, INVERSETAN, ABSOLUTE
        )

        fun isOperator(str: String): Boolean = OPERATORS.any{ it.isStringEquivalent(str)}
        
        fun parse(string: String): FunctionOperator {
            try {
                return OPERATORS.first { op -> op.isStringEquivalent(string) }
            } catch (e: NoSuchElementException){
                throw InvalidSyntaxException(string + " is not a function.")
            } 
        }

    }

    override fun getPrecedence(): Int = Int.MAX_VALUE
}