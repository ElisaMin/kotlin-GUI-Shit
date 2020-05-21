package me.heizi.utills

import java.text.SimpleDateFormat
import java.util.*
import me.heizi.utills.CommandExecutor.find

class CommandResult(val code:Int){

    /************************************************
     *                 我也不知道要写啥                *
     ************************************************/


    //子构造函数
    constructor(code:Int,message:String) : this(code) { this.message = message }

    //如果不是0则不成功
    var isSuccess = (code == 0)
    //信息
    var message :String? = null
    //判断是否包含SubString 如果包含返回true
    fun isSuccess(string: String) : Boolean = (if (message==null)  false else  (message!! find string))
    //Result:[0,null]
    override fun toString(): String = "Result:[$code,${message}]"

    //暂时无用
    //fun println() {println(toString())}
    //fun equals(commandResult: CommandResult): kotlin.Boolean = ((commandResult.message == message) and (commandResult.code == code))

    //val (success,message) = commandResult
    //println(message)
    operator fun component1() = isSuccess
    operator fun component2() = message
    //暂时无用
    infix fun whenSuccess(block: CommandResult.() -> Unit): CommandResult {
        if (isSuccess) this.block()
        return this
    }
    infix fun whenFailed(block: CommandResult.() -> Unit): CommandResult {
        if (!isSuccess) this.block()
        return this
    }

    operator fun plus(commandResult: CommandResult):CommandResult{
        val msg:String? = when {
            (message != null) and (commandResult.message != null) -> "${message}\n${commandResult.message}"
            (message == null) and (commandResult.message != null) -> commandResult.message
            (message != null) and (commandResult.message == null) -> this.message
            else -> null
        }
        val value = if ((this.code == 0) and (commandResult.code == 0)){
            0
        }else{
            9
        }

        return if (msg == null) {
            CommandResult(value)
        }else{
            CommandResult(value,msg)
        }
    }


    //暂时无用
    fun doing(
        key:String?=null,
        falled: (() -> Unit)? = null,
        success:(() -> Unit)? = null
    ): Unit {
        //冗余代码块
        fun block(boolean: Boolean){
            if (boolean){
                success?.let {
                    it()
                }
            }else{
                falled?.let {
                    it()
                }
            }
        }
        //探空
        key?.let { k ->
            block(isSuccess(k))
        } ?: //如果为空
        block(isSuccess)
    }

}

