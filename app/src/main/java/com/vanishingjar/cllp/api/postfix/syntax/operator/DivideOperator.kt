package com.vanishingjar.cllp.api.postfix.syntax.operator

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.Operator

class DivideOperator internal constructor() : Operator(2, "/") {

    override fun getPrecedence() = 1

    override fun operate(vararg operands: Operand): Operand {
        return Operand(operands[0].value.divide(operands[1].value, CONTEXT))
    }

    override fun toString() =  "\u00F7"
}