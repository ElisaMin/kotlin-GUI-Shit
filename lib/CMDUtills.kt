package lib

import lib.CMDUtils.execute
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

object CMDUtils{

    fun run(string: String): CommandResult? {
        return execute(listOf("cmd","/c",string),"GBK")
    }

    fun execute(list: List<String>,charsetName:String): CommandResult {
        var process:Process? = null
        var result = ""
        var code = -1
        println(list.toString())
        try {
             process = ProcessBuilder(list).redirectErrorStream(true).start().apply {
                BufferedReader(InputStreamReader(getInputStream(),charsetName)).apply {
                    result = readText().also {
                        println(it)
                    }
                }.close()
            }.also {
                 code = it.exitValue()
                 it.destroy()
             }
            if (code !=0){
                throw IOException("error=$code,$result")
            }
        }catch (e: IOException){
            println("exception with cant find file maybe")
            with(e){
                printStackTrace()
                with(message!!.split("error=")[1].split(",")){
                    code = get(0).toInt()
                    result = get(1)
                }
                println(localizedMessage)
            }

        } catch (e: Exception){
            println("exp like unknow")
            e.printStackTrace()
        }finally {
            println(code)
            return CommandResult(code,result).apply {
                println(toString())
            }
        }
    }



    class CommandResult(code:Int){
        val code = code
        val success:Boolean = (code == 0)

        constructor(code: Int,msg:String) : this(code) { this.msg = msg }

        var msg :String?=null
        override fun toString(): String {
            return "execute ${success} [${code},${msg}]"
        }
    }
}

object PlatformTools{
    val libADB=".\\lib\\adb.exe"
    val libFastboot=".\\lib\\fastboot.exe"
    val charsetName = "UTF-8"
    fun afs(arrayList: ArrayList<String>,isADB: Boolean): CMDUtils.CommandResult {
        val stringBuilder =StringBuilder()
        var code = -114514
        for (i in arrayList){

            println("afs $i")
            var result = af(i,isADB)
            stringBuilder.append("\n"+result.msg)
            if (!result.success){
                code = result.code
                break
            }else{
                code = 0
            }
        }

        return CMDUtils.CommandResult(code,stringBuilder.toString()).also {
            println(it.toString())
        }
    }


    fun af(string: String,isADB:Boolean): CMDUtils.CommandResult {
        val arrayList = ArrayList<String>()
        arrayList.add(if (isADB)libADB else libFastboot)
        for (i in string.split(" ")){
            arrayList.add(i)
        }
        return execute(arrayList, charsetName)
    }

    fun af(list:List<String>,isADB:Boolean): CMDUtils.CommandResult {
        val arrayList = ArrayList<String>()
        arrayList.add(if (isADB)libADB else libFastboot)
        for (i in list){
            arrayList.add(i)
        }
        return execute(arrayList, charsetName)
    }
    fun checkFastbootDevice(): Pair<String,Boolean> {
        var result = ""
        var isSuccess = false
        loop@ while (true){
            var cmdResult = af("devices",false)
            var msg = cmdResult.msg!!
            if (cmdResult.success){
                var lines = msg.lines()
                when(lines.size){
                    0 -> {
                        throw IOException("未知错误")
                        System.exit(-1)
                    }
                    1->{
                        println("未检测到设备")
                    }
                    2->{
                        isSuccess = true
                        result = msg.replace("\n","").replace("fastboot","").trim()
                        break@loop
                    }
                    else->{
                        println("多台设备")
                    }
                }
            }else{
                "cannot run fastboot devices ,error=9,$msg \n请尝试下载完整包。".run {
                    throw IOException(this)
                    result = this
                }
                break@loop
            }
        }
        return Pair(result,isSuccess)
    }
}
