package com.example.tfgsdkapp

import UIKit.app.Screen
import UIKit.app.data.Align
import UIKit.app.data.EvsColor
import UIKit.app.resources.Font
import UIKit.app.resources.ImgSrc
import UIKit.widgets.Image
import UIKit.widgets.Text


class AlertScreen: Screen() {
    private val text2 = Text()

    override fun onCreate() {

        val alertSrc = ImgSrc("AlertSign2.png", ImgSrc.Slot.s0)

        //val alert = Image().setResource(alertSrc).setX(200f).setY(110f)
        val alert = Image().setResource(alertSrc).setX(getHeight()/2).setY(getWidth()/2)
        add(alert)
    }

}