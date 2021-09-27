package com.example.tonetuner_v2

import android.util.Log
import kotlin.math.pow
import kotlin.math.roundToInt

//sampleRate / duration of time bins

object Util {
    fun logd(message: Any){ Log.d("m_tag",message.toString()) }

    fun arange(start: Double, stop: Double? = null, step: Double = 1.0): List<Double> {
        val lstart: Double
        val lstop: Double

        if (stop == null) {
            lstart = 0.0
            lstop = start-1.0
        }
        else {
            lstart = start
            lstop = stop
        }

        val num = ((lstop-lstart)/step).roundToInt() + 1
        return List(num) { index -> step*index + lstart }
    }

    fun poly(x: List<Double>, y: List<Double>): Harmonic {
        val coef = polyFit(x,y)
        val a = coef[0]
        val b = coef[1]
        val c = coef[2]

        return Harmonic(-b/(2*a), c-b.pow(2)/(4*a))
    }

    fun quadInterp(x: Double, xVals: List<Double>, yVals: List<Double>): Double {
        val coef = polyFit(xVals,yVals)
        return coef[0]*x.pow(2)+coef[1]*x+coef[2]
    }

    fun polyFit(x: List<Double>, y: List<Double> ) : List<Double> {
        val denom = (x[0] - x[1])*(x[0] - x[2])*(x[1] - x[2])
        val a = (x[2] * (y[1] - y[0]) + x[1] * (y[0] - y[2]) + x[0] * (y[2] - y[1])) / denom
        val b = (x[2].pow(2) * (y[0] - y[1]) + x[1].pow(2) * (y[2] - y[0]) + x[0].pow(2) * (y[1] - y[2])) / denom
        val c = (x[1]*x[2]*(x[1]-x[2])*y[0]+x[2] * x[0] * (x[2] - x[0]) * y[1] + x[0] * x[1] * (x[0] - x[1]) * y[2]) / denom

        return listOf(a,b,c)
    }


    fun MutableList<Float>.normalize(
        lowerBound: Float = -1f,
        upperBound: Float = 1f
    ) {
        //Check that array isn't empty
        if (isEmpty()) return

        val minValue   = this.minByOrNull { it }!!
        val maxValue   = this.maxByOrNull { it }!!
        val valueRange = (maxValue - minValue).toFloat()
        val boundRange = (upperBound - lowerBound).toFloat()

        //Check that array isn't already normalized
        // (I would use in range, but this produces excess memory)
        if ((minValue == 0f && maxValue == 0f)
            || (maxValue <= upperBound && maxValue > upperBound
                    && minValue >= lowerBound && minValue < lowerBound)) {
            return
        }

        //Normalize
        for (i in indices) {
            this[i] = ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound
        }
    }

}