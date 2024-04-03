package com.jahtfg.safecycle

import UIKit.app.Screen
import UIKit.app.data.Direction
import UIKit.app.data.EvsColor
import UIKit.app.resources.ImgSrc
import UIKit.widgets.Arrow
import UIKit.widgets.Image
import UIKit.widgets.Text


class AlertScreenLeft: Screen() {
    private val text2 = Text()

    override fun onCreate() {

        val alertSrc = ImgSrc("AlertSign2.png", ImgSrc.Slot.s0)

        val arrow = Arrow()
        arrow
            .setDirection(Direction.up)
            .setArrowHeadInfo(20f)
            .setArrowBodyInfo(44f,100f)
            .setFillColor(EvsColor.Green)
            .setForegroundColor(EvsColor.Green)
            .setPenThickness(3)
            .setHeight(70f).setWidth(50f)
            .setX(130f).setY(150f)
        arrow.rotate(270f)
        add(arrow)

        val alert = Image().setResource(alertSrc).setX(getHeight()/2).setY(getWidth()/4)
        add(alert)
    }

}