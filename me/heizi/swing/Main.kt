package me.heizi.swing

import me.heizi.utills.fastboot
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.JFrame


var frame: JFrame? = null
fun main(args: Array<String>) {
    val cardLayout = CardLayout(20,20)

     frame = Frame {
        Panel(BorderLayout.CENTER,BorderLayout()) {

            val cardPanel = Panel(cardLayout) {
                // home
                 Panel(FlowLayout()) {
                    this.setSize(400,300)
                    Label { "<h1>��ӭ����Heizi����</h1>\n" +
                            "<h3>����·�(���κ�δ֪)�İ�ťѡ��ģʽ</h3>\n" +
                            "���ڣ��������� \n" +
                            "�̳̣��ޣ������뿪��\n" +
                            "���գ��ߣ���ֻҪָ��ִ�е�û�����⣬������κ����������޹ء� \n" +
                            "(����Java��Kotlin������Ȩ�ɣ�)" }
                }
            }
            Panel(BorderLayout.SOUTH) {

                Button("adb����") {

                }
                Button("fastboot����") {

                }
                Button("ˢBoot") {
//                    if ( fastboot.waitForDevice().println() ){
//                        getFile()
//                    }
                }
            }
        }
    }
}

