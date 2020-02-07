package com.vanishingjar.cllp.api.postfix.syntax.operator.function

import com.vanishingjar.cllp.api.postfix.syntax.Operand
import com.vanishingjar.cllp.api.postfix.syntax.bd

class RadiansFunction internal constructor() : FunctionOperator("rad",1) {

    override fun operate(vararg operands: Operand): Operand {
        return Operand((Math.toRadians(operands[0].value.toDouble())).bd)
    }

    override fun toString(): String = "ToRadians"
}