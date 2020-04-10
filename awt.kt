
import lib.*
import lib.PlatformTools.af
import lib.PlatformTools.checkFastbootDevice
import lib.PlatformTools.fastboot
import lib.PlatformTools.fastbootBoot
import lib.PlatformTools.fastbootFlash
import java.awt.*
import java.io.IOException
import java.util.*
import javax.swing.JLabel
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


//测试



//常量？
val noteString="等待设备中，当检测到单个fastboot设备后，软件会自动跳转到下一个步骤。请将手机重启到fastboot再启动本软件，并且检查驱动是否成功安装到你的电脑上。" +
        "<br />相关视频：如何给电脑安装fastboot驱动 <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"

var isVisabel=false

fun main(args: Array<String>) {
    main()
}
fun main() {

    Dialog(Frame()).apply{
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

    }.run {
        isVisible = false
    }
    //主框架
    log("主框架")
    Frame().apply{
        //按钮
        Panel().apply {
            this.layout =FlowLayout()
            mapOf<String?,Boolean?>(Pair(
                    "a",true
            ),Pair(
                    "b",false
            ),Pair(
                    null,null
            )).forEach { slot, v ->
                addButton(if (v == null) {
                    "选择文件刷入boot的ab分区"
                } else {
                    "选择文件刷入'boot_$slot'分区"
                }) {
                    flashPressListener(v)
                }
            }
            addButton("启动boot镜像"){ printCommandResult(fastbootBoot()) }
            mapOf<String,String>(Pair(
                    "重启","reboot"
            ),Pair(
                    "清空数据","-w"
            )).forEach { k,v ->
                addButtonDoFastboot(k,v)
            }

            arrayOf("a","b").iterator().forEach {slot ->
                add(Panel(FlowLayout()).apply {
                    addButtonDoFastboot("激活${slot}系统槽","--set-active=${slot}")
                    arrayOf("dtbo","system","vendor","vbmeta").iterator().forEach {partition ->
                        var partitions =  "${partition}_${slot}"
                        addButton("选择文件刷入'${partitions}'分区"){
                            printCommandResult(fastbootFlash(partitions))
                        }
                    }
                })
            }
        }.let {
            add(it)
            with(it){
                isVisible =isVisabel
            }
        }

    }.apply {
        log("点击事件")
        addWindowListener(WindowListener())
    }.run {
        title="黑字的刷Boot工具"
        setBounds(100,100,900,150)
        isVisible =isVisabel
    }

}

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

fun flashPressListener(whatSlot: Boolean?) {
    var file =""
    fun flashBoot(isSlotA: Boolean): CMDUtils.CommandResult {
        return af(listOf("flash","boot_${if(isSlotA) "a" else "b" }".apply { println("pts: $this") },"$file".apply { println("file: $this") }),false)
    }
    FileDialog(Frame()).apply {
        addWindowListener(WindowListener())
        isVisible = true
    }.run {
        with(file) {
            file = directory + this


            if (whatSlot == null) {
                var result by Delegates.notNull<String>()
                val loadingDialog = mkaDialog(arrayListOf("刷入中：\n", "等待几秒即可获得结果。"))
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
            } else {
                val loadingDialog = mkaDialog(arrayListOf("刷入中：\n", "等待几秒即可获得结果。"))
                printCommandResult(flashBoot(true)) { _, _ ->
                    loadingDialog.isVisible = false
                }
            }
        }
    }
}
