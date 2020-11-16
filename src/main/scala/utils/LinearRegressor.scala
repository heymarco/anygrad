package utils

import Numeric.Implicits._

class LinearRegressor {

    private var intercept = .0
    private var slope = .0
    private var r2 = .0
    private var svar0 = .0
    private var svar1 = .0

    def fit(x: Seq[Double], y: Seq[Double], computeStatisticalProperties: Boolean = false): Unit = {
        if (x.length != y.length) throw new IllegalArgumentException("array lengths are not equal")
        val n = x.length

        // first pass
        val xbar = x.sum / n
        val ybar = y.sum / n

        // second pass: compute summary statistics
        var xxbar = 0.0
        var yybar = 0.0
        var xybar = 0.0

        (0 until n).foreach { i =>
            xxbar += (x(i) - xbar) * (x(i) - xbar)
            yybar += (y(i) - ybar) * (y(i) - ybar)
            xybar += (x(i) - xbar) * (y(i) - ybar)
        }
        slope = xybar / xxbar
        intercept = ybar - slope * xbar

        if (!computeStatisticalProperties) {
            return
        }

        // more statistical analysis
        var rss = 0.0 // residual sum of squares
        var ssr = 0.0 // regression sum of squares
        for (i <- 0 until n) {
            val fit = slope * x(i) + intercept
            rss += (fit - y(i)) * (fit - y(i))
            ssr += (fit - ybar) * (fit - ybar)
        }

        val degreesOfFreedom = n - 2
        r2 = ssr / yybar
        val svar = rss / degreesOfFreedom
        svar1 = svar / xxbar
        svar0 = svar / n + xbar * xbar * svar1
    }

    def computeStatisticalProperties(): Unit = {

    }

    def getSlope: Double = slope
}