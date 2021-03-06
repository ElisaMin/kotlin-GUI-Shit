package me.heizi.tools.swing

import kotlinx.coroutines.runBlocking
import me.heizi.swing.*
import me.heizi.utills.*
import me.heizi.utills.CommandExecutor.log
import me.heizi.utills.PlatformTools.adb
import me.heizi.utills.PlatformTools.fastboot
import me.heizi.utills.PlatformTools.platformTool
import java.awt.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.lang.Exception
import javax.swing.*
import kotlin.concurrent.thread


const val welcome =
        "<h3>点击下方(或任何未知)的按钮选择模式</h3>\n" +
        "关于：黑字制作 \n" +
        "教程：无，速速离开。\n" +
        "风险：高，但只要指令执行的没有问题，你出了任何问题与我无关。 \n" +
        "(我想Java和Kotlin不用授权吧？)"



val icon = Toolkit.getDefaultToolkit().getImage(".\\lib\\icon.png")
fun main(args: Array<String>) = runBlocking {
    var first:Boolean=false
    thread {
        adb server false
        adb server true
    }

    val cardLayout = CardLayout()

    thread {
        val file = File(".\\config.config")
        if (!file.exists()) {
            first = true
            try {
                FileWriter(file).apply { write("first:|1|\n") }.close()
            } catch (e:Exception) {
                e.stackTrace
            }
        }
    }

    Frame {
        this.iconImage = icon
        background = Color.white
        size = Dimension(380, 300)
        fun getFrameSize(): Dimension = this.size
        title = "HeiziTool"
        var parent: Container? = null
        //主框架

        Panel(layoutManager = BorderLayout()) mainPanel@{
            //card view
            this.background =Color.white

            parent =
                Panel(constraint = BorderLayout.CENTER, layoutManager = cardLayout)
                cardview@{
                    // card 1
                    if (first) {
                        CardPanel(title = "欢迎使用Heizi工具箱", name = "welcome") {
                            Label {
                                welcome
                            }
                        }
                    }else{
                        this@Frame.size = Dimension(380, 340)
                    }




                    //card 2
                    CardPanel( name = "fastboot") PanelFastboot@{
                        background =Color.white

                        var slotACheckbox:JCheckBox? = null
                        var slotBCheckbox:JCheckBox? = null
                        var pttGetter:JComboBox<String>? = null



                        //flash

                        fun flash(ptt: String,flashab:Boolean =false,isAVBReove:Boolean=false) {

                            waitForDeviceFastboot {
                                getFile(dialogTitle = "选择${ptt}镜像").takeIf {
                                    if ( it == null) false else (it.length > 2)
                                }?.let { file ->
                                    log(file)
                                    (if (isAVBReove)
                                        if (!flashab){
                                            fastboot removeAVB  Pair(ptt, file)
                                        } else {
                                            fastboot removeAVB_ab  Pair(ptt, file)
                                        }
                                    else if (!flashab){
                                        fastboot flash Pair(ptt, file)
                                    } else {
                                        fastboot flash_ab Pair(ptt, file)
                                    }) showResultAsDialog true
                                } ?: this@runBlocking.log("没选择文件")
                            }
                        }
                        fun slotChoice (ptt:String,isAVBRemove:Boolean=false) {
                            val a = slotACheckbox!!.isSelected
                            val b = slotBCheckbox!!.isSelected
                            when{
                                a  and !b -> flash(ptt = "${ptt}_a",isAVBReove = isAVBRemove)
                                !a and  b -> flash(ptt = "${ptt}_b",isAVBReove = isAVBRemove)
                                a  and  b -> flash(ptt = ptt,flashab = true,isAVBReove = isAVBRemove)
                                else -> flash(ptt = ptt,isAVBReove = isAVBRemove)
                            }
                        }

//                        Panel("提示") {
//                            preferredSize = Dimension(340, 200)
//                            Label {
//                                """1.启动镜像使用`fastboot boot [文件]`的指令
//
//                                2.刷Boot流程：
//                                。   1) 输入boot或者点击boot按钮直接快速输入
//                                。   2) 选择分区槽_a或者_b
//                                。   3)
//
//                                """.trimIndent()
//                            }
//
//                        }

                        Panel(title = "常规操作") {
                            preferredSize = Dimension(340, 60)
                            mapOf<String, String>(
                                Pair("重启", "reboot"),
                                Pair("清除数据", "-w")
                            ).iterator().forEach {
                                val (key, value) = it
                                Button(key) {
                                    platformTool fastboot value showResultAsDialog true
                                }
                            }
                            Button("启动镜像") {
                                waitForDeviceFastboot {
                                    getFile()?.let { file ->
                                        fastboot boot file
                                    } ?: this@runBlocking.log("没选择文件")
                                }
                            }
                        }

                        Panel(title = "分区操作") panelptt@{

                            preferredSize = Dimension(340, 160)


                            //Label { "流程：1.选择是否AVB验证 | 2.输入分区或点击按钮快捷输入 | 3.选择分区槽 | 4.开刷！" }

                            //new one
                            Panel ("分区名")  name@{
                                preferredSize = Dimension(this@panelptt.preferredSize.width-40,60)

                                pttGetter = StringComboBox("输入分区名","system", "vendor", "dtbo", "vbmeta","boot",isEditable = true)
                                slotACheckbox = CheckBox("_a",true)
                                slotBCheckbox = CheckBox("_b")
                            }
                            Panel("操作：") {
                                preferredSize = Dimension(300, 60)
                                Button("选择文件刷入") {

                                    slotChoice(pttGetter!!.nowElement)
                                }
                                Button ("不保留AVB刷入") {
                                    slotChoice(pttGetter!!.nowElement,isAVBRemove = true)
                                }
                                Button("清除") {
                                    val p = pttGetter!!.nowElement
                                    val a = slotACheckbox!!.isSelected
                                    val b = slotBCheckbox!!.isSelected
                                    if (a and b ) {
                                        platformTool fastboot arrayOf(
                                            arrayListOf("erase","${p}_a"), arrayListOf("erase","${p}_b")
                                        ) showResultAsDialog true
                                    }else if (!a and !b) {
                                        fastboot erase p
                                    }else {
                                        fastboot erase "${p}_${if (a) "a" else "b"}"
                                    }
                                }
                            }
                        }

//
//                    Thread{
//                        val isUnlocked = fastboot.getvar("unlocked").isSuccess("yes")
//                        val isSlotA = fastboot.getvar("current-slot")
//                        Panel(title = "设备信息") {
//                            Label {
//                                if (isUnlocked) "检测到设备已解锁"
//                                else "设备未解锁，部分工具可能不能使用"
//                            }
//                        }
//                    }.start()

                    }
                    CardPanel(title = "ADB Tools", name = "adb") {
                        background =Color.white
                        Panel(title = "文件交互") {
                            Panel (title = "复制文件到电脑的……") {
                                Label { "手机路径:" }
                                Input {  }


                            }
                            Button("push") {
                                log("摆饰，未开启")
                            }
                        }
                        Panel(title = "shell") {
                            mapOf(
                                Pair("SU测试", "su -c echo heizi")
                            ).iterator().forEach {
                                val (name, command) = it
                                Button(name) {
                                    adb shell { command } showResultAsDialog true
                                }
                            }

                            fun setTrue(){}
                            Button ("V50020M ROOT") {


//                                waitForDeviceADB {
//
//                                    val fail : CommandResult.()->Unit = {
//                                        log("失败")
//                                        showResultAsDialog(true)
//                                    }
//
//                                    adb push Pair(".\\file\\v500n20m","/data/local/tmp/run") whenSuccess {
//                                        this + (adb shell {"chmod 755 /data/local/tmp/run && run"}) showResultAsDialog true
//
//                                    } whenFailed(fail)
//                                }
                            }
                        }
                        val rt = "重启到"
                        Panel(title = "重启") {

                            preferredSize = Dimension(300, 100)
                            mapOf(
                                Pair("重启", ""),
                                Pair("${rt}Revovery", "recovery"),
                                Pair("${rt}Bootloader", "bootloader"),
                                Pair("${rt}sideload", "sideload-auto-reboot")
                            ).iterator().forEach {
                                val (name, bootable) = it
                                Button(name) {
                                    platformTool adb "reboot $bootable" showResultAsDialog true
                                }
                            }
                        }
                    }

                }

            Panel(
                constraint = BorderLayout.SOUTH,
                title = "常用工具/分类"
            ) {
                background =Color.white
//                Button("adb工具") {
//                    this@Frame.size = Dimension(500, 500)
//                    cardLayout.show(parent, "adb")
//                }
                Button ("破解LGUP" ) {
                    var lable:JLabel? = null
                    var logString = ""

                    fun Any.lognow(string: String): Unit {
                        logString += "$string\n"
                        log(string)
                        lable?.text = logString.toHtml()
                    }

                    Dialog {
                        this.title = "破解LGUP 1.14"
                        lable = Label { "破解lgup" }
                    }
                    lognow("初始化中")
                    //省下内存
                    val programFiles86 = System.getenv("ProgramFiles(x86)")!!
                    val lgupInstallPath = "$programFiles86/LG Electronics/LGUP"
                    //造轮子
                    fun String.toFile(): File = File(this)
                    fun String.toFile(block:File.() ->Unit) = File(this).apply(block)
                    //判断成功
                    var isSuccess:Boolean = false

                    //check its installed or not.
                    if(lgupInstallPath.toFile().exists()){
                        //1. DLL
                        lognow("检测到安装目录为默认安装目录。")
                        "$lgupInstallPath/model/common/".toFile {

                            isSuccess = if (!exists()){
                                lognow("common文件夹不存在，正在创建。")
                                mkdirs()
                            }else{
                                lognow("common文件夹存在，跳过创建。")
                                true
                            }


                            if (isSuccess){

                                "files/lgup/LGUP_Common.dll".toFile {

                                    isSuccess = try {
                                        lognow("DLL文件复制中……")
                                        copyTo("$lgupInstallPath/model/common/LGUP_Common.dll".toFile(),true)
                                        true
                                    }catch (e:FileAlreadyExistsException) {
                                        lognow("请以管理员权限执行本软件。\n$e")
                                        false
                                    }
                                    catch (e:NoSuchFileException){
                                        e.printStackTrace()
                                        lognow("找不到$path。\n$e")
                                        false
                                    }catch (e:FileNotFoundException){
                                        e.printStackTrace()
                                        lognow("找不到$path。\n$e")
                                        false
                                    }catch (e:Exception){
                                        e.printStackTrace()
                                        lognow("复制失败。\n$e")
                                        false
                                    }
                                }
                            }else{
                                lognow("文件夹创建失败。")
                            }
                        }

                        //2. EXE
                        if (isSuccess){
                            lognow("DLL文件复制成功，正在复制EXE文件。")
                            "files/lgup/LGUP.exe".toFile{
                                isSuccess = try {
                                    copyTo("$lgupInstallPath/LGUP.exe".toFile(),true)
                                    true
                                }catch (e:FileAlreadyExistsException) {
                                    lognow("请以管理员权限执行本软件。\n$e")
                                    false
                                }
                                catch (e:NoSuchFileException){
                                    e.printStackTrace()
                                    lognow("找不到$path。\n$e")
                                    false
                                }catch (e:FileNotFoundException){
                                    e.printStackTrace()
                                    lognow("找不到$path。\n$e")
                                    false
                                }catch (e:Exception){
                                    e.printStackTrace()
                                    lognow("复制失败。\n$e")
                                    false
                                }
                                lognow("EXE复制${if (isSuccess) "成功" else "失败"}")
                            }
                        }
                    }else{
                        lognow("未检测到安装路径")
                    }
                    lognow("破解${if (isSuccess) "成功" else "失败"}，关闭弹窗以结束。")
                }
                Button("fastboot工具") {
                    this@Frame.size = Dimension(380, 340)
                    cardLayout.show(parent, "fastboot")
                }
                Button("帮助"){
                    Dialog (title = "帮助") dialog@{
                        Panel dialogPanel@{
                            referencePreferredSize(this@dialog.size,0,0)

                            Panel(title="Fastboot工具使用提示"){
                                referencePreferredSize(this@dialogPanel.preferredSize,-40,-40)

                                Label { """
                                    重启：让你的设备重启
                                    清除数据：清除你的设备数据
                                    启动镜像：fastboot boot img
                                
                                    分区操作：选择镜像用fastboot flash 指令刷入对应的分区，不保留avb只在刷入vbmeta分区时起作用，属于特殊情况，一般情况下使用“选择文件刷入”即可。
                                """.trimIndent() }.let{
                                    it.preferredSize = Dimension(preferredSize.width-40,120)
                                }
                            }

                            Panel (title="破解LGUP") {
                                Label{
                                    """
                                支持版本：1.14
                            """.trimIndent()
                                }
                            }
                        }
                        this@dialog.setSize(500,300)
                    } }

//                Button("刷Boot") {
//                    waitForDeviceFastboot {
//                        fun flashBoot(status: Boolean?): Unit {
//                            getFile()?.let { file ->
//                                when (status) {
//                                    true -> fastboot flash Pair("boot_a", file)
//                                    false -> fastboot flash Pair("boot_b", file)
//                                    else -> fastboot flash_ab Pair("boot", file)
//                                } showResultAsDialog true
//                            } ?: this@runBlocking.log("没选择文件")
//                        }
//                        Dialog {
//                            Button(name = "刷入A分区") {
//                                flashBoot(true)
//                            }
//                            Button(name = "刷入B分区") {
//                                flashBoot(false)
//                            }
//                            Button(name = "一起刷") {
//                                flashBoot(null)
//                            }
//                        }
//                    }
//                }
            }
        }
    }
    Unit
}
