package me.heizi.swing


import kotlinx.coroutines.*

import me.heizi.utills.*
import me.heizi.utills.platformTool
import java.awt.*
import java.awt.event.*
import java.io.IOException
import java.lang.Exception
import java.util.*
import javax.swing.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
}


fun PlatformTools.Fastboot.waitForDevice( ) :Boolean {
    var dialog:JDialog = JDialog().apply { isVisible =false }
    var rr = false
    /**
     * 线程IO处理Dialog
     * 线程Main处理For
     */
    GlobalScope.launch(Dispatchers.IO) {
        dialog = Dialog {
            title = "等待设备中"
            Label {
                "等待设备中，当检测到单个fastboot设备后，软件会自动跳转到下一个步骤。请将手机重启到fastboot再启动本软件，并且检查驱动是否成功安装到你的电脑上。" +
                        "<br />相关视频：如何给电脑安装fastboot驱动 <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"
            }
            isVisible = true
        }
    }
    GlobalScope.launch(Dispatchers.Main) {  }


    suspend fun getrr():Boolean{
     return  withContext(Dispatchers.Main){
        var rr = false
        try {
            loop@ while (true) {
                val (b, s) = platformTool fastboot "devices"
                if (b) {
                    when (s!!.lines().size) {
                        0 -> {
                            throw IOException("未知错误")
                        }
                        1 -> {
                            println("未检测到设备")
                        }
                        2 -> {
                            dialog.isVisible = false
                            rr = true
                            break@loop
                        }
                        else -> {
                            println("多台设备")
                        }
                    }
                } else {
                    "cannot run fastboot devices ,error=9,$s \n请尝试下载完整包。".run {

                        throw IOException(this)
                    }
                }
                Thread.sleep(1024)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dialog.isVisible = false
            }
            rr
        }
    }
    return rr
}




//fun Window.getFile():
fun Window.getFile(): String = getFile(this)
fun getFile():String = getFile(frame!!)
fun getFile(parent:Component):String = JFileChooser().apply {
    isMultiSelectionEnabled = false
    showOpenDialog(parent)
    fileSelectionMode = JFileChooser.FILES_ONLY
}.selectedFile.path.println()

fun showDialogAsResult(commandResult: CommandResult):Dialog = Dialog {
    val (b,s) = commandResult
    title = "执行结果"
    Label { "执行"+ if (b){ "成功：" }else{ "失败：" } +"\n $s"}
    setSize(300,300)
}

fun Window.exitOnClosing(code:Int) {addWindowListener(object :WindowAdapter(){ override fun windowClosing(e: WindowEvent?) { exitProcess(code)} })}
infix fun Window.exitOnClosing(boolean: Boolean) {addWindowListener(object :WindowAdapter(){ override fun windowClosing(e: WindowEvent?) { if (boolean)exitProcess(0)  } })}
fun Frame(apply:JFrame.()->Unit) : JFrame = JFrame().apply {
    //default
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    exitOnClosing(true)
    setLocationRelativeTo(null)
    setSize(500,300)

}.apply(apply).apply { isVisible = true }

/**
 * Dialog
 */
private const val dialogDefaultTitle="提示"
private val dialogDefaultSetting : JDialog.()->Unit = {
    setSize(300,200)
    setLocationRelativeTo(null)
    isVisible = true
}
fun Dialog(
    frame:Frame = me.heizi.swing.frame!!,
    title:String = dialogDefaultTitle,
    modal :Boolean = false
    ,apply: JDialog.() -> Unit
):JDialog = JDialog(frame,title,modal).apply(apply).apply(apply)
//fun Dialog(frame: Frame,title: String,apply: JDialog.() -> Unit):JDialog = JDialog(frame,title).apply(apply).apply(apply)
//fun Dialog(frame: Frame,apply: JDialog.() -> Unit):JDialog = JDialog(frame).apply(dialogDefaultSetting).apply(apply)
//fun Dialog(apply: JDialog.() -> Unit):JDialog =JDialog(frame!!).apply(dialogDefaultSetting).apply(apply)

fun TextDialog(frame: Frame =me.heizi.swing.frame!! ,title: String = dialogDefaultTitle,getString: JDialog.(JLabel)->String):JDialog = Dialog(frame,title) { JLabel().apply {  text = getString(this)}}

/**
 * 添加Panel
 */

fun Window.Panel(
    constraint: Any? = null,
    layoutManager: LayoutManager?=null,
    apply: JPanel.() -> Unit
):JPanel =
    if (layoutManager == null) {
        JPanel()
    }else{
        JPanel(layoutManager)
    }.apply(apply).also { if (constraint ==null) add(it) else add(it,constraint) }




//fun Window.Panel(apply: JPanel.() -> Unit) : JPanel = JPanel().apply(apply).also { add(it) }
//fun Window.Panel(layoutManager: LayoutManager, apply: JPanel.() -> Unit) : JPanel = JPanel(layoutManager).apply(apply).also { add(it) }
//fun Window.Panel(constraint:Any , apply: JPanel.() -> Unit):JPanel =  JPanel().apply(apply).also { add(it,constraint) }
//fun Window.Panel(constraint:Any ,layoutManager: LayoutManager, apply: JPanel.() -> Unit):JPanel =  JPanel(layoutManager).apply(apply).also { add(it,constraint) }

/**
 * JPanel不生效重写
 */
fun JPanel.Panel(apply: JPanel.() -> Unit):JPanel = JPanel().apply(apply).also{this.add(it)}
fun JPanel.Panel(layoutManager: LayoutManager,apply: JPanel.() -> Unit):JPanel = JPanel(layoutManager).apply(apply).also{this.add(it)}

/**
 * 添加常用部件
 */
fun JPanel.Button(name:String,apply: MouseAdapter.(MouseEvent?) -> Unit) :JButton = JButton(name).apply{
    addMouseListener(object : MouseAdapter(){
        override fun mouseClicked(e: MouseEvent?) {
            apply(e)
        }
    })
}.also{add(it)}
fun JPanel.Button(apply: MouseAdapter.(MouseEvent?) -> Unit) :JButton = JButton().apply{
    addMouseListener(object : MouseAdapter(){
        override fun mouseClicked(e: MouseEvent?) {
            apply(e)
        }
    })
}.also{add(it)}

fun Window.Label(dosth:JLabel.()->String) : JLabel = JLabel().apply { text = dosth().toHtml() }.also { add(it) }


fun String.toHtml(): String = """<html>
    <div>${this.replace("\n","<br />")}</div>
</html>""".trimIndent().println()

fun <T> T.println() : T {
    kotlin.io.println(toString())
    return this
}

