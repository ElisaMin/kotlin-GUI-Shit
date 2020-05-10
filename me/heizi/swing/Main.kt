package me.heizi.swing

import kotlinx.coroutines.runBlocking
import me.heizi.utills.CommandExecutor.execute
import me.heizi.utills.fastboot
import me.heizi.utills.log
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Container
import java.awt.FlowLayout
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
        title = "HeiziTool"
        var parent:Container? = null
        //主框架
        Panel(layoutManager = BorderLayout()) {
            //card view
            parent =
            Panel(constraint = BorderLayout.CENTER, layoutManager = cardLayout ) {
                // card 1
//                Panel(title = "欢迎使用Heizi工具箱") { Label { welcome } }
                //card 2
                Panel(title = "Fastboot tools") {

                }
            }

            Panel(
                constraint = BorderLayout.SOUTH
            ){

                Button("adb工具") {
                    log("无")
                }

                Button("fastboot工具"){
                    cardLayout.next(parent!!)
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

