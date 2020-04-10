package lib

import java.awt.event.MouseEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener as InterfaceWindowListener
import java.awt.event.MouseListener as InterfaceMouseListener
open class MouseListener() : java.awt.event.MouseListener{
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mouseEntered(e: MouseEvent?) {}
    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseExited(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}
}
open class WindowListener() : InterfaceWindowListener{
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

fun closeListener( function: (WindowEvent?)->Unit) :WindowListener{
    return object : WindowListener(){
        override fun windowClosing(e: WindowEvent?) {
            super.windowClosing(e)
            function(e)
        }
    }
}

fun pressMouseListener( onPress:()->Unit) :java.awt.event.MouseListener{
    return object : MouseListener() {
        override fun mousePressed(e: MouseEvent?) {
            super.mousePressed(e)
            onPress()
        }
    }
}
fun clickMouseListener( onClick:()->Unit) :java.awt.event.MouseListener{
    return object : MouseListener() {
        override fun mousePressed(e: MouseEvent?) {
            super.mousePressed(e)
            onClick()
        }
    }
}
