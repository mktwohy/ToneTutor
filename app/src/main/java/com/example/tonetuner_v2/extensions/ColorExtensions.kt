package com.example.tonetuner_v2.extensions

import androidx.compose.ui.graphics.Color

// from https://stackoverflow.com/questions/28900598/how-to-combine-two-colors-with-varying-alpha-values
operator fun Color.plus(other: Color): Color {
    val (r0, g0, b0, a0) = other
    val (r1, g1, b1, a1) = this

    val a01 = (1 - a0) * a1 + a0
    val r01 = ((1 - a0) * a1 * r1 + a0 * r0) / a01
    val g01 = ((1 - a0) * a1 * g1 + a0 * g0) / a01
    val b01 = ((1 - a0) * a1 * b1 + a0 * b0) / a01

    return Color(r01, g01, b01, a01)
}

// operator fun Color.plus(other: Color): Color =
//    Color(
//        red = average(this.red, other.red),
//        green = average(this.green, other.green),
//        blue = average(this.green, other.green),
//        alpha = average(this.alpha, other.alpha)
//    )

// @ColorInt
// private fun Color.toColorInt(): Int {
//    val a = (255 * alpha).roundToInt() shl 32 and -0x1000000
//    val r = (255 * red).roundToInt() shl 16 and 0x00FF0000
//    val g = (255 * green).roundToInt() shl 8 and 0x0000FF00
//    val b = (255 * blue).roundToInt() and 0x000000FF
//    return a or r or g or b
// }
