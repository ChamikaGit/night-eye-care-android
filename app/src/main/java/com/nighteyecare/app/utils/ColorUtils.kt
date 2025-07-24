package com.nighteyecare.app.utils

import android.graphics.Color

object ColorUtils {

    fun kelvinToRgb(kelvin: Int): Int {
        val temp = kelvin / 100.0

        var r: Double
        var g: Double
        var b: Double

        if (temp < 66) {
            r = 255.0
            g = 99.4708025861 * Math.log(temp) - 161.1195681661
            b = if (temp <= 19) {
                0.0
            } else {
                50.5596851305 * Math.log(temp - 10) - 68.1120455596
            }
        } else {
            r = 329.698727446 * Math.pow(temp - 60, -0.1332047592)
            g = 288.1221695283 * Math.pow(temp - 60, -0.0755148492)
            b = 255.0
        }

        return Color.rgb(r.coerceIn(0.0, 255.0).toInt(), g.coerceIn(0.0, 255.0).toInt(), b.coerceIn(0.0, 255.0).toInt())
    }
}