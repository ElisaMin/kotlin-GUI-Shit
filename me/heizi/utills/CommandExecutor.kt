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

internal val platformTool = PlatformTools
internal val adb = PlatformTools.ADB
internal val fastboot = PlatformTools.Fastboot

fun main(args: Array<String>) {
    var dir:String="/sdcard"

    while (true){
        PlatformTools.ADB shell {"ls ${dir}"}
        dir += Scanner(System.`in`).next()
    }
}
object PlatformTools{
    var  adbSource  = ".\\lib\\adb.exe"
    var fastbootSource  = ".\\lib\\fastboot.exe"
    private fun getSource(boolean: Boolean):String = if(boolean) adbSource else fastbootSource

    //防冗余
//    private fun doCommand(isADB:Boolean,lists: Array<ArrayList<String>>?=null,list: ArrayList<String>? = null,string: String?) : CommandResult =
//    when {
//        list == null-> run(list,)
//        else -> CommandResult(-114514)
//    }
    private fun doCommand(isADB: Boolean,list: ArrayList<String>):CommandResult = run(arrayOf(list),false, getSource(isADB))
    private fun doCommand(isADB: Boolean,lists: Array<ArrayList<String>>):CommandResult = run(lists,false, getSource(isADB))
    private fun doCommand(isADB: Boolean,string: String):CommandResult = run(string,getSource(isADB),isGBK = false )

    //adb and fastboot
    infix fun adb(list: ArrayList<String>):CommandResult =              doCommand(true,list)
    infix fun adb(lists: Array<ArrayList<String>>):CommandResult =      doCommand(true,lists)
    infix fun adb(string: String):CommandResult =                       doCommand(true,string)

    infix fun fastboot (list: ArrayList<String>):CommandResult =        doCommand(false,list)
    infix fun fastboot(lists: Array<ArrayList<String>>):CommandResult = doCommand(false,lists)
    infix fun fastboot(string: String):CommandResult =                  doCommand(false,string)

    object Fastboot{

        val isBootloaderUnlocked:Boolean? get() {
            val r = fastboot getvar "unlocked"
            log(r.message!!)
            return when{
                r.isSuccess("yes") -> true
                r.isSuccess("no") -> false
                else -> null
            }
        }

        //partition
        infix fun flash (pair: Pair<String,String>):CommandResult = platformTool fastboot "flash ${pair.first} ${pair.second}"
        infix fun flash_ab(pair: Pair<String, String>):CommandResult = platformTool fastboot arrayOf(arrayListOf("flash",pair.first+"_a",pair.second), arrayListOf("flash",pair.first+"_b",pair.second))
        infix fun erase (partition:String):CommandResult = platformTool fastboot "erase $partition"

        //slot
        infix fun switchSlotAB(isSlotA: Boolean):CommandResult = platformTool fastboot "--set-active=${if (isSlotA) "a" else "b"}"
        infix fun setSlot(slot: String):CommandResult = platformTool fastboot "--set-active=$slot"

        //boot
        infix fun boot(path: String):CommandResult = platformTool fastboot arrayListOf("boot",path)


        infix fun getvar (name:String):CommandResult = platformTool fastboot "getvar $name"
        infix fun reboot (isBootloader:Boolean):CommandResult = fastboot(if (isBootloader)"reboot" else "reboot bootloader")
        class DeviceListener (start :Boolean = false) {
            private val mythread: Thread = thread(start = false){
                result = deviceListener()
            }
            init {
                if (start) mythread.start()
            }
            lateinit var result: CommandResult

            fun start() = mythread.start()
            fun stop() = mythread.interrupt()
        }
        fun deviceListener(): CommandResult {
            while (true){
                val (b, s) = platformTool fastboot "devices"
                if (b) {
                    when (s!!.lines().size) {
                        0 -> return CommandResult(-2,"未知错误")
                        1 -> println("未检测到设备")
                        2 -> return CommandResult(0,"检测到设备")
                        else -> return CommandResult(2,"多台设备")
                    }
                } else return CommandResult(233,"或许是找不到文件\n$s") //                    "cannot run fastboot devices ,error=9,$s \n请尝试下载完整包。"
                Thread.sleep(1024)
            }
            return CommandResult(-114514)
        }
    }

    object ADB{
        val state:String get() {val (b,s) = adb("get-state");return if (b) s!! else "null"}
        val serialno:String get() {val (b,s) = adb("get-serialno");return if (b) s!! else "null"}

        infix fun setVerity(isEnable:Boolean):CommandResult = platformTool adb if (isEnable) "enable-verity" else "disable-verity"
        fun shell (list: ArrayList<String>, isRoot:Boolean):CommandResult = adb (if (isRoot) arrayListOf("su","-c").apply { addAll(list) } else list)
        infix fun root(isRoot: Boolean):CommandResult = adb(arrayListOf(if (isRoot)"root" else "unroot"))
        infix fun sideload(path:String):CommandResult = adb(arrayListOf("wait-for-sideload",path))
        infix fun remount (isReboot:Boolean) : CommandResult = adb(arrayListOf("remount"))
        infix fun pull (dirs: Pair<String,String?>):CommandResult = adb(arrayListOf("pull","${dirs.first}","${dirs.second}"))
        infix fun pull (dir: String):CommandResult = adb(arrayListOf("pull","${dir}"))
        infix fun push (dirs: Pair<String,String> ):CommandResult = adb(arrayListOf("push","${dirs.first}","${dirs.second}"))
        infix fun server (status :Boolean):CommandResult = platformTool adb if (status)"start-server" else "kill-server"
        infix fun reconnect (status:Boolean?):CommandResult= platformTool adb arrayListOf("reconnect", when (status) { null -> "";true ->"device"; false -> "offline" } )
        enum class BootableMode(index:Int) {
            Android(0),
            Recovery(1),
            Sideload(2),
            Bootloader(3),
            SideloadAutoReboot(4)
        }

        infix fun reboot(mode: BootableMode):CommandResult {
            return adb(arrayListOf("reboot",when(mode){
                BootableMode.Android -> ""
                BootableMode.Recovery -> "recovery"
                BootableMode.Sideload -> "sideload"
                BootableMode.Bootloader->"bootloader"
                BootableMode.SideloadAutoReboot -> "sideload-auto-reboot"
            }))
        }
        infix fun shell (shellCommand:()->String):CommandResult{ return platformTool adb arrayListOf("shell",shellCommand()) }
    }
}

object CommandExecutor {

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

class CommandResult(code:Int){

    /************************************************
     *                 我也不知道要写啥                *
     ************************************************/

    //子构造函数
    constructor(code:Int,message:String) : this(code) { this.message = message }
    //执行代码
    val code = code
    //如果不是0则不成功
    var isSuccess = (code == 0)
    //信息
    var message :String? = null
    //判断是否包含SubString 如果包含返回true
    fun isSuccess(string: String) : Boolean = (if (message==null)  false else  (message!! find string))
    //Result:[0,null]
    override fun toString(): String = "Result:[$code,$message]"

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