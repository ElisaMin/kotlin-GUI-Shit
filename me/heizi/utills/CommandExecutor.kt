package me.heizi.utills

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.Boolean 
import me.heizi.utills.CommandExecutor.run
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread



//fun main(args: Array<String>) {
//    var dir:String="/sdcard"
//
//    while (true){
//        PlatformTools.ADB shell {"ls ${dir}"}
//        dir += Scanner(System.`in`).next()
//    }
//}

object CommandExecutor {

    var dateFormat:String? = null
        get() {
            return SimpleDateFormat("HH-mm-ss").format(Date())
        }

    infix fun Any.log(string: String) {
        string.lines().iterator().forEach {
            println("+ | ${dateFormat} | ${ this::class.java.simpleName}: $it ")
        }
    }
    fun String.log():String = apply { log(this) }
    infix fun String.find(string: String): Boolean = this match "[\\n]*.*[\\n]*.*${string}[\\n]*.*[\\n]*.*"
    infix fun String.match(string: String):Boolean = this.matches(string.toRegex())
    fun String.println():String = apply { println(this);this }


    private fun getCharset(isGBK: Boolean):String = if (isGBK) "GBK" else "UTF-8"

    /**
     * if u wanna execute this command like CMD doing
        * set me = heizi
     * it use like
        run(listOf（"set","me=heizi"）)
        //or
        run("set me=heizi",true)
     */
    fun run(list: List<String>,isGBK: Boolean = true):CommandResult = execute(list, getCharset(isGBK))
    fun run(command: String,isSplit: Boolean = false,isGBK: Boolean = true): CommandResult = execute(if (isSplit) {command.split(" ")} else {arrayListOf(command)}, getCharset(isGBK))

    /*
    use like this
        // adb reboot bootloader
        val command1= Pair("path/to/adb.exe",listOf("reboot","bootloader"))
        // fastboot oem unlock
        val command2= Pair("path/to/fastboot.exe",listOf("oem","unlock"))
        //just run this command
        run(arrayOf(command1,command2))
     */
    fun run(array: Array<ArrayList<String>>, isGBK:Boolean = true): CommandResult = run(array, getCharset(isGBK),null)
    fun run(arrayList: ArrayList<Pair<String,List<String>>>,charsetName: String):CommandResult{
        var resultCode = -114514
        val resultMessage = StringBuilder()
        for (it in arrayList) {
            if (!execute(ArrayList<String>().apply {add(it.first);addAll(it.second) },charsetName).apply {
                        resultMessage.append("$message${if (arrayList.size == 1) "" else "\n"}")
                        resultCode = code
                    }.isSuccess){ break }
        }
        return CommandResult(resultCode,resultMessage.toString())
    }

    /**
     * runable is a file witch can run on the bash or cmd, e.g ADB.exe
     * when u need something like
        * `adb push file /data/local/tmp/file && adb shell chmod 755 /data/local/tmp/file && adb shell /data/local/tmp/file`
     * we can do like
         val file = "/data/local/tmp/file"
         val command1= listOf("push","file",file)
         val command2= listOf("shell","chmod","755",file)
         val command3= listOf("shell",file)
         val array = arrayOf(command1,command2,command3)
         val result = run(array,true,"./path/to/adb")
         println(result.msg!!)
     */

    fun run(
        string: String,
        runnable: String,
        isGBK: Boolean = false,
        isSplit: Boolean = true
    ) :CommandResult =
        if (isSplit)
            execute(arrayListOf(runnable).apply{
                    addAll(string.split(" "))
                }, getCharset(isGBK)
            )
        else
            execute(listOf(runnable,string), getCharset(isGBK))

    fun run(strings:Array<String>,isGBK: Boolean,runnable: String?=null):CommandResult {
        val arrayList = ArrayList<ArrayList<String>>()
        for (s in strings) {
            arrayList.add(arrayListOf(s))
        }
        return  this.run( arrayList.toArray() as Array<ArrayList<String>> ,isGBK,runnable)
    }

    fun run(lists: Array<ArrayList<String>>,isGBK: Boolean,runnable: String?=null): CommandResult = run(lists, getCharset(isGBK),runnable)

    fun run(lists: Array<ArrayList<String>>,charsetName:String,runnable: String?=null): CommandResult {
        var resultCode = -114514
        val resultMessage = StringBuilder()
        val header= if (runnable == null ) { arrayListOf("cmd","/c") }else{ arrayListOf("$runnable") }

        for (list in lists) {
            var result = execute(ArrayList<String>().apply {addAll(header);addAll(list) },charsetName).apply {
                resultMessage.append("$message${if (lists.size < 2) "" else "\n"}")
                resultCode = code
            }
            if (!result.isSuccess){
                break
            }
        }

        return CommandResult(resultCode,resultMessage.toString())
    }

    /**
     * 执行代码行
     * 返回：command result对象
     * 参数list： 一组指令
     * charsetName: 字符集名字 一般为UTF-8或GBK
     * 实例  ：execute(listOf("cmd","echo","hello world"),"GBK")
     * 返回值：CommandResult:[0,hello world]
     */
    fun execute(list: List<String>, charsetName: String): CommandResult {
        //log
        "".println()
        log("new command is running")
        log(list.toString())

        //方法内"全局"变量
        var resultMessage = ""
        var resultCode = -1

        //runtime
        try {
            //使用processBuilder 设置错误流 开始后返回一个Process对象
            ProcessBuilder(list).redirectErrorStream(true).start().
            apply{
                //新建一个InputStream 传入参数 Process.inputStream和charsetName 用ins作为变量新建作用域函数
                InputStreamReader(inputStream,charsetName).also {ins ->
                    //新建一个缓冲区读取器 传入上面的InputStream
                    BufferedReader(ins).apply {
                        //读到里面的text并打印
                        resultMessage = readText().log()
                    }.close() //关闭BufferReader
                }.close() //关闭inputStream
                resultCode = exitValue() //获取process.exitValue
            }.destroy() //摧毁 ProcessBuilder
        }catch (e:IOException){
            //IOException 一般为执行错误
            //E 一般为： cannot run echo,error=1,no such file or directory
            e.message?.run{
                if ( this find "error=" ) {
                    //["cannot run xxxx","114514,message"]
                    resultCode = this.split("error=")[1].split(",").apply {
                        //["114514","message"]
                        resultMessage = when (size) {
                            0->  {
                                resultCode = 114514
                                "null"
                            }
                            1 ->  "null"
                            2 -> this[1]
                            else ->{ toString().replace("[","").replace("]","") }
                        }
                    }[0].also { log() }.toInt()
                } else  {
                    resultMessage = this
                    resultCode = 2333
                }
            }
        }catch (e:Exception){ with(e){
            resultCode = 2333
            resultMessage = message!!
            printStackTrace()
        }
        }finally {
            log(resultCode.toString()+"\n"+resultMessage)

            return if (resultCode == 114514) {
                CommandResult(114514)
            } else{

                CommandResult(resultCode,resultMessage)
            }
        }
    }
}
