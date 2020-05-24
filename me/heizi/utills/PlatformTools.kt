package me.heizi.utills

import me.heizi.utills.CommandExecutor.log
import kotlin.concurrent.thread



object PlatformTools{



    internal val platformTool = PlatformTools
    internal val adb = PlatformTools.ADB
    internal val fastboot = PlatformTools.Fastboot
    var  adbSource  = ".\\lib\\adb.exe"
    var fastbootSource  = ".\\lib\\fastboot.exe"
    private fun getSource(boolean: Boolean):String = if(boolean) adbSource else fastbootSource

    //防冗余
//    private fun doCommand(isADB:Boolean,lists: Array<ArrayList<String>>?=null,list: ArrayList<String>? = null,string: String?) : CommandResult =
//    when {
//        list == null-> run(list,)
//        else -> CommandResult(-114514)
//    }
    private fun doCommand(isADB: Boolean,list: ArrayList<String>):CommandResult =
        CommandExecutor.run(arrayOf(list), false, getSource(isADB))
    private fun doCommand(isADB: Boolean,lists: Array<ArrayList<String>>):CommandResult =
        CommandExecutor.run(lists, false, getSource(isADB))
    private fun doCommand(isADB: Boolean,string: String):CommandResult =
        CommandExecutor.run(string, getSource(isADB), isGBK = false)

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

        //flash
        private fun getArrayListForFlash(pair: Pair<String, String>, isA: Boolean?=null, isAVBRemove: Boolean = false): ArrayList<String> =
            arrayListOf<String>().apply {
                if (isAVBRemove) {
                    add("--disable-verity")
                    add("--disable-verification")
                }
                add("flash")

                if (isA!=null) add( pair.first+"_${if (isA) "a" else "b"}" )
                else add(pair.first)

                add(pair.second)

                log(toString())
            }

        infix fun flash (pair: Pair<String,String>):CommandResult = platformTool fastboot getArrayListForFlash(pair)
        infix fun flash_ab(pair: Pair<String, String>):CommandResult = platformTool fastboot arrayOf(
            getArrayListForFlash(pair,true),
            getArrayListForFlash(pair,false)
        )
        //flash without avb
        infix fun removeAVB (pair: Pair<String,String>):CommandResult = platformTool fastboot getArrayListForFlash(pair,isAVBRemove = true)
        infix fun removeAVB_ab(pair: Pair<String, String>):CommandResult = platformTool fastboot arrayOf(
            getArrayListForFlash(pair,true,true),
            getArrayListForFlash(pair,false,true)
        )

        //erase
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
        //adb get-things
        val state:String get() {val (b,s) = adb("get-state");return if (b) s!! else "null"}
        val serialno:String get() {val (b,s) = adb("get-serialno");return if (b) s!! else "null"}


        // adb root/unroot
        infix fun root(isEnable: Boolean):CommandResult = adb(arrayListOf(if (isEnable)"root" else "unroot"))
        //adb disable/enable verity
        infix fun setVerity(isEnable:Boolean):CommandResult = platformTool adb if (isEnable) "enable-verity" else "disable-verity"
        //remount
        fun remount() : CommandResult = adb(arrayListOf("remount"))

        // adb shell
        fun shell (list: ArrayList<String>, isRoot:Boolean):CommandResult = adb (if (isRoot) arrayListOf("su","-c").apply { addAll(list) } else list)
        infix fun shell (shellCommand:()->String):CommandResult{ return platformTool adb arrayListOf("shell",shellCommand()) }
        // adb sideload
        infix fun sideload(path:String):CommandResult = adb(arrayListOf("wait-for-sideload",path))

        // fileManaging
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
            SideloadAutoReboot(4);
//
//            fun getString(bootable: BootableMode): String
//                = when (bootable){
//
//            }
        }

        infix fun reboot(mode: BootableMode):CommandResult =
            if (mode == BootableMode.Android){
                adb("reboot")
            }
            else {
                adb(arrayListOf("reboot", when (mode) {
                    BootableMode.Recovery -> "recovery"
                    BootableMode.Sideload -> "sideload"
                    BootableMode.Bootloader -> "bootloader"
                    BootableMode.SideloadAutoReboot -> "sideload-auto-reboot"
                    else -> ""
                }))
            }

    }
}
