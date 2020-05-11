package me.heizi.swing


import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import me.heizi.utills.CommandResult
import me.heizi.utills.PlatformTools
import me.heizi.utills.log
import me.heizi.utills.platformTool
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import javax.swing.*
import javax.swing.border.TitledBorder
import kotlin.system.exitProcess





//fun Window.getFile(): String? = getFile(this)
fun getFile(parent:Component = frame!!,dialogTitle:String = "选择文件" ):String? = JFileChooser().apply {
    this.dialogTitle = dialogTitle
    isMultiSelectionEnabled = false
    showOpenDialog(parent)
    fileSelectionMode = JFileChooser.FILES_ONLY
}.selectedFile?.path.println()



fun showDialogAsResult(commandResult: CommandResult,title: String = "执行结果"):Dialog = Dialog {
    val (b,s) = commandResult
    Label { "执行"+ if (b){ "成功：" }else{ "失败：" } +"\n $s"}
    setSize(300,300)
}
infix fun CommandResult.showResultAsDialog(boolean: Boolean) : Dialog = showDialogAsResult(this)

fun Window.exitOnClosing(code:Int) {addWindowListener(object :WindowAdapter(){ override fun windowClosing(e: WindowEvent?) { exitProcess(code)} })}
infix fun Window.exitOnClosing(boolean: Boolean) {addWindowListener(object :WindowAdapter(){ override fun windowClosing(e: WindowEvent?) { if (boolean)exitProcess(0)  } })}

/**
 * add frame
 */
fun Frame(isSystemStyle:Boolean = true,exitOnClose:Boolean=true,size:Pair<Int,Int> = Pair(500,500),show: Boolean = true,apply:JFrame.()->Unit) : JFrame = JFrame().apply {

    if (isSystemStyle) UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    if (exitOnClose)   defaultCloseOperation = JFrame.EXIT_ON_CLOSE ;exitOnClosing(true)

    setLocationRelativeTo(null)
    setSize(size.first,size.second)

}.apply(apply).apply { isVisible = show }

/**
 * Dialog
 */
private const val dialogDefaultTitle="提示"
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
    val a = Label { getString() }
    if (labelSetting != null) {
        a.apply(labelSetting)
    }
    isVisible = show
}

/**
 * 添加Panel
 */
fun Container.Panel(
    title:String?=null,
    constraint: Any? = null,
    layoutManager: LayoutManager?=null,
    apply: JPanel.() -> Unit
):JPanel =
    if (layoutManager == null) {
        JPanel()
    }else{
        JPanel(layoutManager)
    }.also {
        constraint?.let {cons ->
            add(it,cons)
        } ?:add(it)


        title?.let {s ->
            it.setTitle(title)
        }

    }.apply(apply)

fun Container.CardPanel(
    title:String?=null,
    name: String,
    layoutManager: LayoutManager?=null,
    apply: JPanel.() -> Unit
):JPanel =
    if (layoutManager == null) {
        JPanel()
    }else{
        JPanel(layoutManager)
    }.also {
        add(name,it)
        title?.let {s ->
            it.setTitle(title)
        }
    }.apply(apply)

/**
 * Panel title
 */
fun JPanel.setTitle(string: String): Unit {
    this.border = BorderFactory.createTitledBorder(string)
}
fun JPanel.getTitle():String?{
    return if (border !== null){
        val b = border as TitledBorder
        b.title
    }else{
        null
    }
}
fun JPanel.referencePreferredSize(dimension: Dimension,changeHeight:Boolean = false,changeWidth: Boolean = false){

    fun getN(boolean: Boolean,int: Int):Int =if (boolean) (int-40)/2 else int-40

    preferredSize = Dimension(getN(changeWidth,dimension.width),getN(changeHeight,dimension.height))

}
fun JPanel.referencePreferredSize(dimension: Dimension,width:Int=0,height:Int=0){
    this.preferredSize = Dimension(dimension.width+width,dimension.height+height)
}
/**
 * 添加常用部件
 */
fun JPanel.Button(name:String = "按钮",apply: JButton.(MouseEvent?) -> Unit) :JButton = JButton(name).apply{
    addMouseListener(object : MouseAdapter(){
        override fun mouseClicked(e: MouseEvent?) {
            apply(e)
        }
    })
}.also{add(it)}

fun Container.Label(getString: () -> String) = JLabel(getString().toHtml()).also { add(it) }

