import lib.CMDUtils
import lib.PlatformTools.af
import lib.PlatformTools.afs
import lib.PlatformTools.checkFastbootDevice
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.IOException
import java.util.*
import javax.swing.JLabel
import kotlin.collections.ArrayList


//测试



//常量？
val noteString="等待设备中，当检测到单个fastboot设备后，软件会自动跳转到下一个步骤。请将手机重启到fastboot再启动本软件，并且检查驱动是否成功安装到你的电脑上。" +
        "<br />相关视频：如何给电脑安装fastboot驱动 <br /> <a href='https://www.bilibili.com/video/BV1n64y1u7LE/'>https://www.bilibili.com/video/BV1n64y1u7LE/</a>"
var windowExitListener = object :WindowListener{
    override fun windowDeiconified(e: WindowEvent?) {}

    override fun windowClosing(e: WindowEvent?) {
        System.exit(0)
    }
    override fun windowClosed(e: WindowEvent?) {}
    override fun windowActivated(e: WindowEvent?) {}
    override fun windowDeactivated(e: WindowEvent?) {}
    override fun windowOpened(e: WindowEvent?) {}
    override fun windowIconified(e: WindowEvent?) {}

}
var isVisabel=false

fun main(args: Array<String>) {
    main()
}
fun main() {
    //主框架
    log("主框架")

    Dialog(Frame()).apply{
        title = "等待设备中"
        addWindowListener(windowExitListener)
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
                addWindowListener(windowExitListener)
                setBounds(200,200,300,130)
                isVisible = true
            }
            it.isVisible =false
        }



//        val adbEnvSetup= afs(arrayListOf("kill-server","start-server"),true)
//        isVisabel = adbEnvSetup.success
//        if (!isVisabel){
//            Dialog(Frame()).apply {
//                setBounds(200,200,300,700)
//                add(JLabel().apply {
//                    text="""<html>
//                        <style>div{padding:5rem}</style>
//                        <div>ADB启动失败，错误在：</div>
//                        <div><div>${adbEnvSetup.msg!!.replace("\n","<br />")}</div></div>
//                    </html>""".trimIndent()
//                })
//
//                addWindowListener(windowExitListener)
//                isVisible =true
//            }
//        }

    }

    Frame().apply{



        //按钮
        Panel().apply {
            class mouseListener(whatSlot: Boolean?):MouseListener{

                val whatSlot = whatSlot
                override fun mouseReleased(e: MouseEvent?) {

                }

                override fun mouseEntered(e: MouseEvent?) {

                }

                override fun mouseClicked(e: MouseEvent?) {

                }

                override fun mouseExited(e: MouseEvent?) {

                }

                var file =""


                var result = ""

                fun flashBoot(isSlotA: Boolean): CMDUtils.CommandResult {
                    return af(listOf("flash","boot_${if(isSlotA) "a" else "b" }".apply { println("pts: $this") },"$file".apply { println("file: $this") }),false)
                }


                override fun mousePressed(e: MouseEvent?) {

                    FileDialog(Frame()).apply {
                        addWindowListener(windowExitListener)
                    }.let { fd ->

                        fd.isVisible = true
                        with(fd.file) {
                            file = fd.directory + this
                            val loadingDialog = mkaDialog(arrayListOf("刷入中：\n","等待几秒即可获得结果。"))
                            //if not null
                            if (whatSlot == null) {
                                flashBoot(true).run {
                                    if (success) {
                                        result = msg!!
                                        flashBoot(false).run {
                                            result = result + "\n" + msg
                                            if (success) {
                                                mkaDialog(arrayListOf("执行成功：","$result"))
                                            }else{
                                                mkaDialog(arrayListOf("执行失败：", "$result"))
                                            }
                                        }
                                    } else {
                                        mkaDialog(arrayListOf("执行失败：", "$msg"))
                                    }
                                    loadingDialog.isVisible=false
                                }
                            } else {
                                with(flashBoot(whatSlot!!)){
                                    if (success){
                                        mkaDialog(arrayListOf("执行成功：","$msg"))
                                    }else{
                                        mkaDialog(arrayListOf("执行失败：", "$msg"))
                                    }
                                    loadingDialog.isVisible=false
                                }
                            }
                        }
                    }
                }


            }
            add(Button("选择文件刷入'boot_a'分区").apply {
                addMouseListener(mouseListener(true))
            })
            add(Button("选择文件刷入'boot_b'分区").apply {
                addMouseListener(mouseListener(false))
            })
            add(Button("选择文件刷入boot的ab分区（推荐）").apply {
                addMouseListener(mouseListener(null))
            })
        }.let {
            add(it)
            with(it){
                isVisible =isVisabel
            }
        }

    }.apply {
        log("点击事件")
        addWindowListener(object :WindowListener{
            override fun windowDeiconified(e: WindowEvent?) {}

            override fun windowClosing(e: WindowEvent?) {
                System.exit(0)
            }
            override fun windowClosed(e: WindowEvent?) {}
            override fun windowActivated(e: WindowEvent?) {}
            override fun windowDeactivated(e: WindowEvent?) {}
            override fun windowOpened(e: WindowEvent?) {}
            override fun windowIconified(e: WindowEvent?) {}

        })
    }.run {
        title="黑字的刷Boot工具"
        setBounds(100,100,500,500)
        isVisible =isVisabel
    }

}

fun mkaDialog(arrayList: ArrayList<String>) : Dialog {
    return Dialog(Frame()).apply {
        setBounds(300,300,300,300)
        add(JLabel().apply {
            text = toHtml(arrayList)
        })
        isVisible = true
        addWindowListener(object :WindowListener{    override fun windowDeiconified(e: WindowEvent?) {}

            override fun windowClosing(e: WindowEvent?) {
                isVisible = false
            }
            override fun windowClosed(e: WindowEvent?) {}
            override fun windowActivated(e: WindowEvent?) {}
            override fun windowDeactivated(e: WindowEvent?) {}
            override fun windowOpened(e: WindowEvent?) {}
            override fun windowIconified(e: WindowEvent?) {}

        })
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
