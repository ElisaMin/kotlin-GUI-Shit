package me.heizi.swing


import kotlinx.coroutines.*
import me.heizi.utills.CommandResult
import me.heizi.utills.PlatformTools
import me.heizi.utills.platformTool
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import javax.swing.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
}

fun waitForDeviceFastboot(dosth:()->Unit){
    GlobalScope.launch {
        val dialog = async{
            return@async TextDialog(
                title = "�ȴ��豸",
                show = true
            ){
                "�ȴ��豸�У�����⵽����fastboot�豸��������Զ���ת����һ�����衣�뽫�ֻ�������fastboot����������������Ҽ�������Ƿ�ɹ���װ����ĵ����ϡ�" +
                        "<br />�����Ƶ����θ����԰�װfastboot���� <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"
            }
        }.await()

        withContext(Dispatchers.Default) {
            try {
                loop@ while (true) {
                    if (!dialog.isVisible) {
                        break@loop
                    }
                    val (b, s) = platformTool fastboot "devices"
                    if (b) {
                        when (s!!.lines().size) {
                            0 -> {
                                throw IOException("δ֪����")
                            }
                            1 -> {
                                println("δ��⵽�豸")
                            }
                            2 -> {
                                dialog.isVisible = false
                                dosth()
                            }
                            else -> {
                                println("��̨�豸")
                            }
                        }
                    } else {
                        "cannot run fastboot devices ,error=9,$s \n�볢��������������".run {

                            throw IOException(this)
                        }
                    }
                    Thread.sleep(1024)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}


suspend fun PlatformTools.Fastboot.waitForDevice( ) :Boolean {
    var dialog:JDialog? = null
    var rr = false
    fun dialogVisible(boolean: Boolean) {
        GlobalScope.launch(Dispatchers.IO) { dialog!!.isVisible = boolean }
    }
     suspend fun getbbbbbb():Boolean =  withContext(Dispatchers.IO){
        try {
            loop@ while (true) {
                if (dialog!=null) if (!dialog!!.isVisible) break@loop
                val (b, s) = platformTool fastboot "devices"
                if (b) {
                    when (s!!.lines().size) {
                        0 -> {
                            throw IOException("δ֪����")
                        }
                        1 -> {
                            println("δ��⵽�豸")
                        }
                        2 -> {
                            dialogVisible(false)
                            rr = true
                            return@withContext true
                        }
                        else -> {
                            println("��̨�豸")
                        }
                    }
                } else {
                    "cannot run fastboot devices ,error=9,$s \n�볢��������������".run {

                        throw IOException(this)
                    }
                }
                Thread.sleep(1024)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dialogVisible(false)
        }

        false
    }

     GlobalScope.launch(Dispatchers.IO) {
        dialog = Dialog {
            title = "�ȴ��豸��"
            Label {
                "�ȴ��豸�У�����⵽����fastboot�豸��������Զ���ת����һ�����衣�뽫�ֻ�������fastboot����������������Ҽ�������Ƿ�ɹ���װ����ĵ����ϡ�" +
                        "<br />�����Ƶ����θ����԰�װfastboot���� <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"
            }
            isVisible = true
        }
    }
    return getbbbbbb()
}



//fun Window.getFile():
fun Window.getFile(): String? = getFile(this)

fun getFile(parent:Component = frame!! ):String? = JFileChooser().apply {
    isMultiSelectionEnabled = false
    showOpenDialog(parent)
    fileSelectionMode = JFileChooser.FILES_ONLY
}.selectedFile?.path.println()



fun showDialogAsResult(commandResult: CommandResult):Dialog = Dialog {
    val (b,s) = commandResult
    title = "ִ�н��"
    Label { "ִ��"+ if (b){ "�ɹ���" }else{ "ʧ�ܣ�" } +"\n $s"}
    setSize(300,300)
}
infix fun CommandResult.showResultAsDialog(boolean: Boolean) : Dialog = showDialogAsResult(this)

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
private const val dialogDefaultTitle="��ʾ"
private val dialogDefaultSetting : JDialog.()->Unit = {
    setSize(300,200)
    setLocationRelativeTo(null)
}
fun Dialog(

    frame:Frame = me.heizi.swing.frame!!,
    title:String = dialogDefaultTitle,
    modal :Boolean = false,
    show:Boolean =true,
    apply: JDialog.() -> Unit
):JDialog = JDialog(frame,title,modal).apply(apply).apply(dialogDefaultSetting).apply { this.isVisible = show }
//fun Dialog(frame: Frame,title: String,apply: JDialog.() -> Unit):JDialog = JDialog(frame,title).apply(apply).apply(apply)
//fun Dialog(frame: Frame,apply: JDialog.() -> Unit):JDialog = JDialog(frame).apply(dialogDefaultSetting).apply(apply)
//fun Dialog(apply: JDialog.() -> Unit):JDialog =JDialog(frame!!).apply(dialogDefaultSetting).apply(apply)

//class TextDialog(block:TextDialog.()->String){
//    var frame = me.heizi.swing.frame!!
//    var title = dialogDefaultTitle
//    var labelSetting : JLabel.()->Unit = {}
//    var dialog: Dialog = Dialog (frame = frame,title = title) {
//        this.Label{block()}.run(labelSetting)
//    }
//}
fun TextDialog(
    frame: Frame =me.heizi.swing.frame!!,
    title: String = dialogDefaultTitle,
    labelSetting:(JLabel.()->Unit)? = null,
    show:Boolean =true,
    getString: ()->String
):JDialog = Dialog(
    frame = frame,
    title =  title,
    show = show
) {
    labelSetting?.let { set -> Label(getString).apply(set) }
    isVisible = show
}

/**
 * ���Panel
 */
fun Container.Panel(
    constraint: Any? = null,
    layoutManager: LayoutManager?=null,
    apply: JPanel.() -> Unit
):JPanel =
    if (layoutManager == null) {
        JPanel()
    }else{
        JPanel(layoutManager)
    }.apply(apply).also { if (constraint ==null) add(it) else add(it,constraint) }


/**
 * ��ӳ��ò���
 */
fun JPanel.Button(name:String = "��ť",apply: MouseAdapter.(MouseEvent?) -> Unit) :JButton = JButton(name).apply{
    addMouseListener(object : MouseAdapter(){
        override fun mouseClicked(e: MouseEvent?) {
            apply(e)
        }
    })
}.also{add(it)}

fun Container.Label(getString: () -> String) = JLabel(getString().toHtml()).also { add(it) }

inline infix fun Boolean.True(block: (Boolean)->Unit):Boolean = this.also { if (it) block(this) }
inline infix fun Boolean.False(block:(Boolean)->Unit):Boolean = this.also { if (!it) block(this) }
fun String.toHtml(): String = """<html>
    <body><div>${this.replace("\n","<br />")}</div>
</body></html>""".trimIndent().println()

fun <T> T.println() : T {
    kotlin.io.println(toString())
    return this
}

