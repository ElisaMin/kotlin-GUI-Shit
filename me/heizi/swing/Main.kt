package me.heizi.swing

import kotlinx.coroutines.runBlocking
import me.heizi.utills.fastboot
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.JFrame
import kotlin.concurrent.thread

const val welcome = "<h1>欢迎来到Heizi工具</h1>\n" +
        "<h3>点击下方(或任何未知)的按钮选择模式</h3>\n" +
        "关于：黑字制作 \n" +
        "教程：无，速速离开。\n" +
        "风险：高，但只要指令执行的没有问题，你出了任何问题与我无关。 \n" +
        "(我想Java和Kotlin不用授权吧？)"

var frame: JFrame? = null
fun main(args: Array<String>) = runBlocking {
    val cardLayout = CardLayout(20,20)

    frame = Frame {
        main@Panel(
            layoutManager = BorderLayout()
        ) {
            center@Panel(
                constraint = BorderLayout.CENTER,
                layoutManager = cardLayout
            ) {
                Panel { Label { welcome } }
            }

            south@Panel(
                constraint = BorderLayout.SOUTH
            ){
                Button("adb工具") {

                }
                Button("fastboot工具") {

                }

                Button("刷Boot") {



                    println("shit")
                    waitForDeviceFastboot{
                        getFile()?.let {file ->
                            fastboot flash_ab Pair("boot",file) showResultAsDialog true
                        }
                    }
                }
            }
        }
    }
}

