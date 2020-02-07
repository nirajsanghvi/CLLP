package com.vanishingjar.cllp.api.postfix.syntax.operator

import com.vanishingjar.cllp.api.postfix.exception.NonOperationalOperatorException
import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.Operator

class OpenBracketOperator internal constructor() : Operator(0, "(") {
    override fun getPrecedence() = -1

    override fun operate(vararg operands: Operand): Operand {
        throw NonOperationalOperatorException("Open bracket operator does not have an operation.")
    }

    override fun toString() =  "OpenBracket"
}