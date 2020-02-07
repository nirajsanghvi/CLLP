package com.vanishingjar.cllp.api.postfix.syntax.operator.function

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.bd

class FloorFunction internal constructor() : FunctionOperator("floor", 1) {

    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.floor(operands[0].value.toDouble())).bd)
    }

    override fun toString(): String = "Floor"
}