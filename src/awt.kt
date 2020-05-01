
import lib.*
import lib.PlatformTools.af
import lib.PlatformTools.checkFastbootDevice
import lib.PlatformTools.fastboot
import lib.PlatformTools.fastbootBoot
import lib.PlatformTools.fastbootFlash
import java.awt.*
import java.io.IOException
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JLabel
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


//测试



//常量？
val noteString="等待设备中，当检测到单个fastboot设备后，软件会自动跳转到下一个步骤。请将手机重启到fastboot再启动本软件，并且检查驱动是否成功安装到你的电脑上。" +
        "<br />相关视频：如何给电脑安装fastboot驱动 <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"

var isVisabel=false
val icon = Toolkit.getDefaultToolkit().getImage(".\\lib\\icon.png")
fun main(args: Array<String>) {
    main()
}
fun main() {

    Dialog(Frame().apply {
        iconImage = icon
    }).apply{
        this.setIconImage(icon)
        title = "等待设备中"
        addWindowListener(WindowListener())
        add(JLabel("""<html>
            <div>$noteString</div>
        </html>""".trimIndent()))
        setBounds(200,200,300,200)
    }.also {
        it.isVisible =true
        try {
            with(checkFastbootDevice()){
                isVisabel = second
            }
        }catch (e:IOException){
            Dialog(Frame()).apply {
                add(JLabel().apply {
                    text="""<html>
                          <div>错误：Fastboot启动失败:</div>
                          <div>${e.toString()}</div>
                        </html>""".trimIndent()
                })
                addWindowListener(WindowListener())
                setBounds(200,200,300,130)
                isVisible = true
            }
            it.isVisible =false
        }

    }.isVisible = false


    log("主框架")
    Frame {
        Panel(FlowLayout()){

            subPanel(GridLayout(2, 3)) {
                with(fastboot("getvar unlocked")){
                    when{
                        isSuccess("yes") -> addLabel("已解锁")
                        isSuccess("no") -> addLabel("未解锁")
                        !success -> "执行失败".println()
                        else -> "未知原因".println()
                    }
                }
                with(fastboot("getvar current-slot")){
                    when{
                        isSuccess("a") -> addLabel("当前激活分区：A")
                        isSuccess("b") -> addLabel("当前激活分区：B")
                        !success -> "执行失败".println()
                        else -> "未知原因".println()
                    }
                }
                addButton("刷boot a和b") { flashPressListener() }
                addButton("启动boot镜像") { printCommandResult(fastbootBoot()) }
                mapOf<String, String>(Pair(
                        "重启", "reboot"
                ), Pair(
                        "清空数据", "-w"
                )).forEach { k, v ->
                    addButtonDoFastboot(k, v)
                }
            }
            //slot flasher
            arrayOf("a","b").iterator().forEach {slot ->
                add(Panel(GridLayout(7,1)).apply {
                    addButtonDoFastboot("激活${slot}系统槽","--set-active=${slot}")
                    arrayOf("dtbo","system","vendor","vbmeta","boot").iterator().forEach {partition ->
                        var partitions =  "${partition}_${slot}"
                        addButton("选择文件刷入'${partitions}'分区"){
                            printCommandResult(fastbootFlash(partitions))
                        }
                    }
                })
            }


        }.also {
            add(it)
        }.isVisible =isVisabel
    }.apply {
        title="黑字的刷Boot工具"
        setBounds(100,100,300,420)
        iconImage = icon
        isVisible =isVisabel
    }.addWindowListener(WindowListener())

}
fun Frame.Panel(doSTH:Panel.()->Unit): Panel {return Panel().apply(doSTH).also { this.add(it) }}
fun Frame.Panel(layoutManager: LayoutManager,doSTH:Panel.()->Unit): Panel {return Panel(layoutManager).apply(doSTH).also { this.add(it) }}
fun Panel.subPanel(doSTH:Panel.()->Unit): Panel = this.apply{add(Panel().apply(doSTH))}
fun Panel.subPanel(layoutManager: LayoutManager,doSTH:Panel.()->Unit): Panel = this.apply{add(Panel(layoutManager).apply(doSTH))}
fun Frame(setting:Frame.()->Unit):Frame = Frame().apply(setting)
fun Panel.addLabel(string: String):JLabel = JLabel(string).also { add(it) }

fun Panel.addButton(label:String,function :(Button)->Unit) = this.apply {
    add(Button(label).apply{
        addMouseListener(clickMouseListener {
            function(this)
        })
    })
}
fun Panel.addButtonDoFastboot(label:String,command:String) = this.apply {
    add(Button(label).apply{
        addMouseListener(clickMouseListener {
            printCommandResult(fastboot(command))
        })
    })
}


fun Dialog.addCloseListener()= this.apply { addWindowListener(
        closeListener {
            isVisible = false
        }
) }

fun mkaDialog(arrayList: ArrayList<String>) : Dialog {
    return Dialog(Frame()).apply {
        setBounds(20,20,300,300)
        add(JLabel().apply {
            text = toHtml(arrayList)
        })
        isVisible = true
        addCloseListener()
    }
}

fun toHtml(arraylist: ArrayList<String>): String {
    return StringBuilder().apply {
        append("<html>\n")
        for (i in arraylist){
            append("<div>${i.replace("\n","<br />")}</div>")
        }
        append("\n</html>")
    }.toString()
}

fun toHtml(string: String): String {
    return """<html>
        <div>${string.replace("\n","<br />")} \n</div>
    </html>""".trimIndent()
}
fun log(string: String): Unit {
    println("\n------------------\n" +
            "${Calendar.getInstance().time} : \n$string\n" +
            "------------------\n")
}

fun printCommandResult(result: CMDUtils.CommandResult): Unit {
    with(result){
        if (success){
            mkaDialog(arrayListOf("执行成功：","$msg"))
        }else{
            mkaDialog(arrayListOf("执行失败：", "$msg"))
        }
    }
}
fun printCommandResult(result: CMDUtils.CommandResult, function : (Dialog,CMDUtils.CommandResult)->Unit ): Unit {
    with(result){
        if (success){
            function(mkaDialog(arrayListOf("执行成功：","$msg")),this)
        }else{
            function(mkaDialog(arrayListOf("执行失败：", "$msg")),this)
        }
    }
}
fun printCommandResult(result: CMDUtils.CommandResult,whatBoolean: Boolean, function : (Dialog,CMDUtils.CommandResult)->Unit ): Unit {
    with(result){
        if (success){
            if (whatBoolean){ function(mkaDialog(arrayListOf("执行成功：","$msg")),this) }else{mkaDialog(arrayListOf("执行成功：", "$msg"))}
        }else{
            if (!whatBoolean){ function(mkaDialog(arrayListOf("执行失败：", "$msg")),this) }else{mkaDialog(arrayListOf("执行失败：", "$msg"))}
        }
    }
}
fun printCommandResult(result: CMDUtils.CommandResult,functionTrue : (Dialog,CMDUtils.CommandResult)->Unit , functionFalse : (Dialog,CMDUtils.CommandResult)->Unit ): Unit {
    with(result){
        if (success){
            functionTrue(mkaDialog(arrayListOf("执行成功：","$msg")),this)
        }else{
            functionFalse(mkaDialog(arrayListOf("执行失败：", "$msg")),this)
        }

    }
}

fun flashPressListener() {
    var file =""
    var result = ""

    fun flashBoot(isSlotA: Boolean): CMDUtils.CommandResult {
        return af(listOf("flash","boot_${if(isSlotA) "a" else "b" }".apply { println("pts: $this") },"$file".apply { println("file: $this") }),false)
    }
    FileDialog(Frame(),"选择boot文件",FileDialog.LOAD).apply {
        addWindowListener(WindowListener())
        isVisible = true
    }.let{
        file =it.directory+it.file
    }
    var loadingDialog:Dialog = Dialog(Frame()).addCloseListener().apply { add(Label("刷入中")) }
    printCommandResult(flashBoot(true), { dialog, commandResult ->
        dialog.isVisible = false
        result = commandResult.msg!!

        printCommandResult(flashBoot(false).apply {
            msg = "${result}\n${msg}"
        })
        loadingDialog.isVisible =false
    }, { _, _ ->
        loadingDialog.isVisible =false
    })
}