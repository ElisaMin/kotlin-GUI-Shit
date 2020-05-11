package me.heizi.swing

import kotlinx.coroutines.runBlocking
import me.heizi.utills.CommandExecutor.execute
import me.heizi.utills.fastboot
import me.heizi.utills.log
import me.heizi.utills.platformTool
import java.awt.*
import javax.swing.JFrame
import kotlin.concurrent.thread

const val welcome =
        "<h3>点击下方(或任何未知)的按钮选择模式</h3>\n" +
        "关于：黑字制作 \n" +
        "教程：无，速速离开。\n" +
        "风险：高，但只要指令执行的没有问题，你出了任何问题与我无关。 \n" +
        "(我想Java和Kotlin不用授权吧？)"

var frame: JFrame? = null
fun main(args: Array<String>) = runBlocking {

    val cardLayout = CardLayout(20,20)

    frame = Frame {

        size = Dimension(500,300)

        title = "HeiziTool"
        var parent:Container? = null
        //主框架
        Panel(layoutManager = BorderLayout()) {
            //card view
            parent =
            Panel(constraint = BorderLayout.CENTER, layoutManager = cardLayout ) {
                // card 1
                CardPanel(title = "欢迎使用Heizi工具箱",name = "welcome") {
                    Label {
                        welcome
                    }
                }
                //card 2
                CardPanel(title = "Fastboot tools",name = "fastboot") {
                    fun flash(ptt:String){
                        waitForDeviceFastboot {
                            getFile(dialogTitle = "选择${ptt}镜像")?.let {file ->
                                fastboot flash Pair(ptt,file) showResultAsDialog true
                            }?:this@runBlocking.log("没选择文件")
                        }
                    }
                    Panel(title = "分区操作") {
                        //make a value for reference size
                        val parent1 = this
                        //set size make it not overflow
                        preferredSize = Dimension(340,240)
                        //懒得打
                        arrayOf("a","b").iterator().forEach {slot ->
                            //slot Panel
                            Panel(title = "Slot_${slot.toUpperCase()}") {
                                referencePreferredSize(parent1.preferredSize,changeWidth = true)

                                Button("激活${slot}分区") {
                                    waitForDeviceFastboot {
                                        fastboot setSlot slot showResultAsDialog true
                                    }
                                }

                                arrayOf("system","vendor","dtbo","vbmeta").iterator().forEach { ptt->
                                    "${ptt}_${slot}".let {ptts->
                                        Button ("刷${ptts}分区") {
                                            flash(ptts)
                                        }
                                    }
                                }

                                Button("fuck the AVB $slot") {
                                    waitForDeviceFastboot {
                                        getFile(dialogTitle = "选择VBMETA_${slot.toUpperCase()}镜像")?.let {file ->
                                            platformTool fastboot " --disable-verity  --disable-verification flash $file " showResultAsDialog true
                                        }?:this@runBlocking.log("没选择文件")
                                    }
                                }
                            }
                        }
                    }

                    Panel(title = "常规操作") {
                        mapOf<String,String>(
                            Pair("重启","reboot"),
                            Pair("清除数据","-w")
                        ).iterator().forEach {
                            val (key,value) = it
                            Button(key) {
                                platformTool fastboot value showResultAsDialog true
                            }
                        }
                        Button ("启动镜像") {
                            waitForDeviceFastboot {
                                getFile()?.let {file ->
                                    fastboot boot file
                                }?:this@runBlocking.log("没选择文件")
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
                CardPanel (title = "ADB Tools",name = "adb") {
                    Panel (title = "文件交互") {

                    }
                }

            }

            Panel(
                constraint = BorderLayout.SOUTH
            ){

//                Button("adb工具") {
//                    cardLayout.show(parent,"adb")
//                }

                Button("fastboot工具"){
                    this@Frame.size = Dimension(500,500)
                    cardLayout.show(parent,"fastboot")
                }

                Button("刷Boot") {
                    waitForDeviceFastboot{
                        fun flashBoot(status: Boolean?): Unit {
                            getFile()?.let {file ->
                                when (status){
                                    true -> fastboot flash Pair("boot_a",file)
                                    false -> fastboot flash Pair("boot_b",file)
                                    else -> fastboot flash_ab Pair("boot",file)
                                } showResultAsDialog true
                            } ?:this@runBlocking.log("没选择文件")
                        }
                        Dialog {
                            Button (name = "刷入A分区") {
                                flashBoot(true)
                            }
                            Button (name = "刷入B分区") {
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

