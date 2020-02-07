package com.vanishingjar.cllp.api.postfix.syntax

import java.math.BigDecimal

class Operand(var value: BigDecimal) : ExpressionEntity(false){
    
    companion object {
        fun parse(string: String): Operand{
            return Operand(string.bd)
        }
    }
    
    override fun toString(): String {
        return value.toString()
    }

    fun negate() {
        this.value = value.negate()
    }

}