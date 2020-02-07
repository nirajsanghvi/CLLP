package com.vanishingjar.cllp.api.postfix.syntax.operator

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.Operator

class MultiplyOperator internal constructor() : Operator(2, "*") {

    override fun getPrecedence() = 1

    override fun operate(vararg operands: Operand): Operand {
        return Operand(operands[0].value.multiply(operands[1].value, CONTEXT))
    }

    override fun toString() = "\u00D7"
}