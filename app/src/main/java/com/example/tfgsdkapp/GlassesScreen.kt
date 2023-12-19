package com.example.tfgsdkapp

import UIKit.app.Screen
import UIKit.app.data.Align
import UIKit.app.data.EvsColor
import UIKit.app.resources.Font
import UIKit.widgets.Text

class GlassesScreen: Screen() {
    private val text = Text()

    override fun onCreate() {

        text.setText("Hello world with Vodafone SDK and Maverick SDK").setResource(Font.StockFont.Small).setTextAlign(Align.center)
        text.setX(getWidth()/2).setY(getHeight()/2).setForegroundColor(EvsColor.Green.rgba)
        add(text)
    }

}
