package com.vanishingjar.cllp.api.postfix.syntax.operator.function.trigonometry

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.bd
import com.vanishingjar.cllp.api.postfix.syntax.operator.function.FunctionOperator

class TangentFunction internal constructor() : FunctionOperator("tan", 1) {

    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.tan(operands[0].value.toDouble())).bd)
    }

    override fun toString(): String = "Tangent"
}