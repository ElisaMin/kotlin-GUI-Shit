package me.heizi.swing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.heizi.utills.*
import me.heizi.utills.PlatformTools.ADB.BootableMode.*
import me.heizi.utills.PlatformTools.platformTool
import java.io.IOException
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JDialog
import kotlin.concurrent.thread

inline infix fun Boolean.True(block: (Boolean)->Unit):Boolean = this.also { if (it) block(this) }
inline infix fun Boolean.False(block:(Boolean)->Unit):Boolean = this.also { if (!it) block(this) }
fun String.toHtml(): String = """<html>
    <body><div>${this.replace("\n","<br />")}</div>
</body></html>""".trimIndent()

fun <T> T.println() : T {
    kotlin.io.println(toString())
    return this
}

fun main(args: Array<String>) {
    Frame {
        Panel {
            val check1= JCheckBox("shit")
            check1.addChangeListener {
                println("changeListener:on changing")
                println("button:${it.source}\nis enable:${check1.isEnabled}\nis selected:${check1.isSelected}\n")
            }
            add(check1)
            val combobox = JComboBox<String>()
            combobox.run {

                isEditable = true
                arrayOf("","system","vendor","boot","vbmeta","dtbo","super").iterator().forEach {
                    addItem(it)
                }
            }
            add(combobox)
            Button {
                combobox.editor.item.println()
            }
        }
    }
}

fun waitForDeviceADB(dosth:CommandResult.()->Unit){
    var alterDialog:JDialog? = null
    var result : CommandResult? = null

    GlobalScope.launch (Dispatchers.IO) {
        alterDialog = TextDialog { "等待安卓设备中" }


    }
    GlobalScope.launch (Dispatchers.Default) {
        while (true) {
            result?.let {
                log(this.toString())
            }
            delay(1000)
            if ( result!=null) {
                result
                    ?.whenSuccess {
                        dosth()
                    }   ?.whenFailed {
                        log("wait-for-device 失败")
                    }
                alterDialog?.isVisible = false
                break
            }
            if (alterDialog != null) {
                if (!alterDialog?.isVisible!!) {
                    result = CommandResult(114514)
                    break
                }
            }

        }
    }


    thread { result = platformTool adb "wait-for-device" }



}

fun waitForDeviceFastboot(dosth:()->Unit){
    var dialog: JDialog? = null
    GlobalScope.launch {
        launch(Dispatchers.IO){
            dialog = TextDialog(
                title = "等待设备",
                show = true
            ){
                "等待设备中，当检测到单个fastboot设备后，软件会自动跳转到下一个步骤。请将手机重启到fastboot再启动本软件，并且检查驱动是否成功安装到你的电脑上。" +
                        "<br />相关视频：如何给电脑安装fastboot驱动 <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"
            }
        }

        launch(Dispatchers.Default) {
            try {
                loop@ while (true) {
                    if (dialog != null){if (!dialog!!.isVisible) {
                        println("窗口关闭")
                        break@loop
                    }}
                    val (b, s) = platformTool fastboot "devices"
                    if (b) {
                        when (s!!.lines().size) {
                            0 -> throw IOException("未知错误")
                            1 -> log("未检测到设备")
                            2 -> {
                                dialog?.isVisible = false
                                dosth()
                                break@loop
                            }
                            else -> println("多台设备，请拔掉电脑的电源线然后静静去世（雾）（不会真有人知道现在要干嘛吧 不会吧）。")
                        }
                    } else {
                        "cannot run fastboot devices ,error=9,$s \n请尝试下载完整包。".run {
                            throw IOException(this)
                        }
                    }
                    Thread.sleep(1024)
                }
            } catch (e: IOException) {
                TextDialog {
                    e.toString()
                }
                e.printStackTrace()
            }
        }
    }

}
