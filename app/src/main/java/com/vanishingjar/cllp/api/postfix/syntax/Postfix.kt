package com.vanishingjar.cllp.api.postfix.syntax

import com.vanishingjar.cllp.api.postfix.exception.InvalidSyntaxException
import com.vanishingjar.cllp.api.postfix.syntax.operator.CloseBracketOperator
import com.vanishingjar.cllp.api.postfix.syntax.operator.OpenBracketOperator
import com.vanishingjar.cllp.api.postfix.syntax.operator.UnaryNegateOperator
import com.vanishingjar.cllp.api.postfix.syntax.operator.function.FunctionOperator
import java.util.*
import kotlin.collections.ArrayList

//Adapted from https://github.com/kieferlam/postfix

object Postfix {

    fun from(infix: String): Expression {
        return toPostfix(scan(infix))
    }

    fun scan(infix: String): Expression {
        var expression = Expression()
        var fix = infix.replace(" ", "")
        while (fix.isNotEmpty()) {
            //  scanString appends the operand/operator to expression (ArrayList)
            //  returns the left over string
            fix = scanString(fix, expression)
        }

        expression.add(Operator.CLOSE_BRACKET)
        return expression
    }
    
    fun toPostfix(infix: ArrayList<ExpressionEntity>): Expression {
        var stack = Stack<Operator>()
        var postfix = Expression()
        for (e in infix) {
            if (e.isOperator) {
                when (e) {
                    is CloseBracketOperator -> {
                        while (!stack.empty()) {
                            var pop = stack.pop()
                            if (pop is OpenBracketOperator) {
                                break
                            } else {
                                postfix.add(pop)
                            }
                        }
                    }
                    is OpenBracketOperator -> stack.push(e)
                    is FunctionOperator -> stack.push(e)
                    else -> {
                        while ((e as Operator).precedesStack(stack)) {
                            postfix.add(stack.pop())
                        }
                        stack.push(e)
                    }
                }
            } else {
                postfix.add(e)
            }
        }
        while (!stack.empty()) {
            postfix.add(stack.pop())
        }
        return postfix
    }

    private fun scanString(infix: String, postfix: ArrayList<ExpressionEntity>): String {
        for ((i, c) in infix.withIndex()) {
            var firstPart = infix.substring(0, i)
            
            if (c.isOperator()) {
                if (i < 1) {
                    /**
                     * Determine whether the minus symbol is a unary negate operator or a minus operator.
                     * Check the entity before it, if it's an operand, it's most likely not unary negate.
                     */
                    if((postfix.isEmpty() || (postfix.last() !is Operand && postfix.last() !is CloseBracketOperator)) && Operator.MINUS.isStringEquivalent(c.toString())){
                        postfix.add(UnaryNegateOperator())
                    }else{
                        postfix.add(Operator.parse(c.toString()))
                    }
                    return infix.substring(1)
                } else if (Operator.OPEN_BRACKET.isStringEquivalent(c.toString())) {

                    /**
                     * If substring before the bracket is a valid function name, search functions
                     * else if the substring is number, assume shorthand for multiplcation. E.g. 5(2) -> 5*2
                     */

                    if (firstPart.contains(Regex("\\d+"))) { //first part of expression has digit
                        if (isNumber(firstPart)) {
                            postfix.add(Operand.parse(firstPart))
                            return "*" + infix.substring(i)
                        } else {
                            throw InvalidSyntaxException("Function names cannot contain digits.")
                        }
                    }else{
                        if(FunctionOperator.isOperator(firstPart)){
                            postfix.add(FunctionOperator.parse(firstPart))
                            return infix.substring(i)
                        }else{
                            throw InvalidSyntaxException("$firstPart is not a function.")
                        }
                    }
                    
                } else {
                    postfix.add(Operand.parse(infix.substring(0, i)))
                    return infix.substring(i)
                }
            }
        }
        postfix.add(Operand.parse(infix))
        return ""
    }
    
    

    private fun isNumber(str: String): Boolean {
        return try {
            str.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun evaluate(postfix: ArrayList<ExpressionEntity>): Operand {
        var stack = Stack<ExpressionEntity>()
        for (p in postfix) {
            if (p.isOperator) {
                if (p is FunctionOperator) {
                    if (stack.size < p.numberOfParameters) throw InvalidSyntaxException("Not enough parameters.")
                    var params = ArrayList<Operand>()
                    for (i in 0 until p.numberOfParameters) {
                        if (stack.peek() !is Operand) throw InvalidSyntaxException("Parameter must be an operand.")
                        params.add(stack.pop() as Operand)
                    }
                    stack.push(p.operate(*params.toTypedArray().reversedArray()))
                } else {
                    if (stack.size < (p as Operator).numberOfParameters) throw InvalidSyntaxException("Operator $p requires ${p.numberOfParameters} arguments!")
                    var params = ArrayList<Operand>()
                    for(i in 0 until p.numberOfParameters) {
                        if (stack.peek() !is Operand) throw InvalidSyntaxException("Parameter must be an operand.")
                        params.add(stack.pop() as Operand)
                    }
                    stack.push(p.operate(*params.toTypedArray().reversedArray()))
                }
            } else {
                stack.push(p)
            }
        }
        if (stack.size != 1) {
            println(postfix.joinToString(" "))
            throw InvalidSyntaxException("Could not evaluate expression to a single value.")
        }
        if (stack.peek() !is Operand) throw InvalidSyntaxException("Evaluated to operator. (Should evaluate to operand)")
        return stack.pop() as Operand
    }

}

fun Char.isOperator() = Operator.isOperator(this.toString())