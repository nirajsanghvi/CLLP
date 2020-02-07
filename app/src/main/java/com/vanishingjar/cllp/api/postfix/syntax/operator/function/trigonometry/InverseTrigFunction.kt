package com.vanishingjar.cllp.api.postfix.syntax.operator.function.trigonometry

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.bd
import com.vanishingjar.cllp.api.postfix.syntax.operator.function.FunctionOperator

abstract class InverseTrigFunction(private val term: String, private val shorthand: String) : FunctionOperator("a$shorthand", 1){
    
    companion object {
        private val prefixes = arrayOf(
                "inv", "a", "arc"
        )
    }
    
    override fun isStringEquivalent(string: String): Boolean {
        return when (string.toLowerCase().replace(shorthand.toLowerCase(), "")){
            in prefixes -> true
            else -> false
        }
    }

    override fun toString(): String = "Inverse$term"
}

class InverseSineFunction : InverseTrigFunction("Sine", "sin") {
    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.asin(operands[0].value.toDouble())).bd)
    }
}

class InverseCosineFunction : InverseTrigFunction("Cosine", "cos") {
    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.acos(operands[0].value.toDouble())).bd)
    }
}

class InverseTangentFunction : InverseTrigFunction("Tangent", "tan") {
    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.atan(operands[0].value.toDouble())).bd)
    }
}