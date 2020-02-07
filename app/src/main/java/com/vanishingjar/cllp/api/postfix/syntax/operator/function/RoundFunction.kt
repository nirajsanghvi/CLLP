package com.vanishingjar.cllp.api.postfix.syntax.operator.function

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.bd

class RoundFunction internal constructor() : FunctionOperator("round", 1) {

    override fun operate(vararg operands: Operand): Operand {
        return Operand(Math.round(operands[0].value.toDouble()).bd)
    }

    override fun toString(): String = "Round"
}