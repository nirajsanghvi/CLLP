package com.vanishingjar.cllp.api.postfix.syntax.operator.function.trigonometry

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.bd
import com.vanishingjar.cllp.api.postfix.syntax.operator.function.FunctionOperator

class SineFunction internal constructor() : FunctionOperator("sin",1) {

    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.sin(operands[0].value.toDouble())).bd)
    }

    override fun toString(): String = "Sine"
}