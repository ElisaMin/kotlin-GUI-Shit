package me.heizi.swing

import kotlinx.coroutines.runBlocking
import me.heizi.utills.fastboot
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.JFrame
import kotlin.concurrent.thread

const val welcome = "<h1>��ӭ����Heizi����</h1>\n" +
        "<h3>����·�(���κ�δ֪)�İ�ťѡ��ģʽ</h3>\n" +
        "���ڣ��������� \n" +
        "�̳̣��ޣ������뿪��\n" +
        "���գ��ߣ���ֻҪָ��ִ�е�û�����⣬������κ����������޹ء� \n" +
        "(����Java��Kotlin������Ȩ�ɣ�)"

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
                Button("adb����") {

                }
                Button("fastboot����") {

                }

                Button("ˢBoot") {



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

