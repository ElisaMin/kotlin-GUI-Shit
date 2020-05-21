package me.heizi.tools.swing

import kotlinx.coroutines.runBlocking
import me.heizi.swing.*
import me.heizi.utills.*
import me.heizi.utills.CommandExecutor.log
import me.heizi.utills.PlatformTools.adb
import me.heizi.utills.PlatformTools.fastboot
import me.heizi.utills.PlatformTools.platformTool
import java.awt.*
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JTextField
import kotlin.concurrent.thread


const val welcome =
        "<h3>点击下方(或任何未知)的按钮选择模式</h3>\n" +
        "关于：黑字制作 \n" +
        "教程：无，速速离开。\n" +
        "风险：高，但只要指令执行的没有问题，你出了任何问题与我无关。 \n" +
        "(我想Java和Kotlin不用授权吧？)"
fun main(args: Array<String>) {
    start(args)
}

fun start(args: Array<String>) = runBlocking {
    thread {
        adb server false
        adb server true
    }

    val cardLayout = CardLayout()

    Frame {
        background = Color.white
        size = Dimension(700, 600)
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
//                    CardPanel(title = "欢迎使用Heizi工具箱", name = "welcome") {
//                        Label {
//                            welcome
//                        }
//                    }



                    //card 2
                    CardPanel( name = "fastboot") PanelFastboot@{
                        background =Color.white

                        var slotACheckbox:JCheckBox? = null
                        var slotBCheckbox:JCheckBox? = null
                        var pttGetter:JComboBox<String>? = null



                        //flash

                        fun flash(ptt: String,flashab:Boolean =false) {

                            waitForDeviceFastboot {
                                getFile(dialogTitle = "选择${ptt}镜像")?.let { file ->
                                    if (!flashab){
                                        fastboot flash Pair(ptt, file)
                                    } else {
                                        fastboot flash_ab Pair(ptt, file)
                                    } showResultAsDialog true

                                } ?: this@runBlocking.log("没选择文件")
                            }
                        }
                        fun slotChoice (ptt:String) {
                            val a = slotACheckbox!!.isSelected
                            val b = slotBCheckbox!!.isSelected
                            when{
                                a  and !b -> flash(ptt = "${ptt}_a")
                                !a and  b -> flash(ptt = "${ptt}_b")
                                a  and  b -> flash(ptt = ptt,flashab = true)
                                else -> flash(ptt = ptt)
                            }
                        }

                        Panel("提示") {
                            preferredSize = Dimension(340, 200)
                            Label {
                                """1.启动镜像使用`fastboot boot [文件]`的指令
                                    
                                2.刷Boot流程：
                                。   1) 输入boot或者点击boot按钮直接快速输入
                                。   2) 选择分区槽_a或者_b
                                。   3) 
                                    
                                """.trimIndent()
                            }

                        }

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

                                StringComboBox("输入分区名","system", "vendor", "dtbo", "vbmeta","boot",isEditable = true)
                                slotACheckbox = CheckBox("_a",true)
                                slotBCheckbox = CheckBox("_b")
                            }
                            Panel("操作：") {
                                preferredSize = Dimension(300, 60)
                                Button("选择文件刷入") {
                                    flash(pttGetter!!.nowElement)
                                }
                                Button ("不保留AVB刷入") {

                                }
                                Button("清除") {

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

                            Button("pull") {
                                log("摆饰，未开启")
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


                                waitForDeviceADB {

                                    val fail : CommandResult.()->Unit = {
                                        log("失败")
                                        showResultAsDialog(true)
                                    }

                                    adb push Pair(".\\file\\v500n20m","/data/local/tmp/run") whenSuccess {
                                        this + (adb shell {"chmod 755 /data/local/tmp/run && run"}) showResultAsDialog true

                                    } whenFailed(fail)
                                }
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
                Button("adb工具") {
                    this@Frame.size = Dimension(500, 500)
                    cardLayout.show(parent, "adb")
                }

                Button ("fastboot分区管理"){

                }

                Button("fastboot其他工具") {
                    this@Frame.size = Dimension(500, 500)
                    cardLayout.show(parent, "fastboot")
                }

                Button("刷Boot") {
                    waitForDeviceFastboot {
                        fun flashBoot(status: Boolean?): Unit {
                            getFile()?.let { file ->
                                when (status) {
                                    true -> fastboot flash Pair("boot_a", file)
                                    false -> fastboot flash Pair("boot_b", file)
                                    else -> fastboot flash_ab Pair("boot", file)
                                } showResultAsDialog true
                            } ?: this@runBlocking.log("没选择文件")
                        }
                        Dialog {
                            Button(name = "刷入A分区") {
                                flashBoot(true)
                            }
                            Button(name = "刷入B分区") {
                                flashBoot(false)
                            }
                            Button(name = "一起刷") {
                                flashBoot(null)
                            }
                        }
                    }
                }
            }
        }
    }
}
