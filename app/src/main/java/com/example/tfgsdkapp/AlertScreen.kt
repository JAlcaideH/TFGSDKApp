package com.example.tfgsdkapp

import UIKit.app.Screen
import UIKit.app.data.Direction
import UIKit.app.data.EvsColor
import UIKit.app.resources.ImgSrc
import UIKit.widgets.Arrow
import UIKit.widgets.Image

class AlertScreen: Screen() {

    private var valorAlerta: Int = 0
    override fun onCreate() {

        val alertSrc = ImgSrc("AlertSign2.png", ImgSrc.Slot.s0)
        if(valorAlerta == 1) {

            val arrow = Arrow()
            arrow
                .setDirection(Direction.down)
                .setArrowHeadInfo(20f)
                .setArrowBodyInfo(44f,100f)
                .setFillColor(EvsColor.Green)
                .setForegroundColor(EvsColor.Green)
                .setPenThickness(3)
                .setHeight(70f).setWidth(50f)
                .setX(130f).setY(150f)
            add(arrow)

            val alert = Image().setResource(alertSrc).setX(getHeight()/2).setY(getWidth()/4)
            add(alert)
        } else if(valorAlerta == 2) {

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
        } else if (valorAlerta == 3) {

            val arrow = Arrow()
            arrow
                .setDirection(Direction.up)
                .setArrowHeadInfo(20f)
                .setArrowBodyInfo(44f,100f)
                .setFillColor(EvsColor.Green)
                .setForegroundColor(EvsColor.Green)
                .setPenThickness(3)
                .setHeight(70f).setWidth(50f)
                .setX(340f).setY(150f)
                .rotate(90f)
            add(arrow)

            val alert = Image().setResource(alertSrc).setX(getHeight()/2).setY(getWidth()/4)
            add(alert)
        } else if (valorAlerta == 4) {

            removeAll()
        }
    }

    override fun onUpdateUI(timestampMs: Long) {
        super.onUpdateUI(timestampMs)

        if(valorAlerta == 1) {
            val alertSrc = ImgSrc("AlertSign2.png", ImgSrc.Slot.s0)
            val arrow = Arrow()
            arrow
                .setDirection(Direction.down)
                .setArrowHeadInfo(20f)
                .setArrowBodyInfo(44f,100f)
                .setFillColor(EvsColor.Green)
                .setForegroundColor(EvsColor.Green)
                .setPenThickness(3)
                .setHeight(70f).setWidth(50f)
                .setX(130f).setY(150f)
            add(arrow)

            val alert = Image().setResource(alertSrc).setX(getHeight()/2).setY(getWidth()/4)
            add(alert)
        } else if(valorAlerta == 2) {

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
        } else if (valorAlerta == 3) {
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
                .setX(340f).setY(150f)
                .rotate(90f)
            add(arrow)

            val alert = Image().setResource(alertSrc).setX(getHeight()/2).setY(getWidth()/4)
            add(alert)
        }
    }

    fun actualizarValor(valor: Int) {
        valorAlerta = valor
    }

}