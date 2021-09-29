package utils

import utils.types.{Solution, Variance}

case class _Snapshot(experimentRepetition: Int,
                round: Int,
                target: Int,
                result: Double,
                variance: Variance,
                quality: Double,
                utility: Double,
                totalIterations: Double,
                T: Double,
                M: Double,
                m: Double,
                tsMeasured: Double,
                t1Measured: Double,
                tsCalculated: Double,
                t1Calculated: Double,
               ) {

    def toMap(): Map[String, Double] = {
        def namesValues = getClass.getDeclaredFields.map { field =>
            field.setAccessible(true)
            (field.getName, field.get(this).asInstanceOf[Double])
        }
        Map(namesValues :_*)
    }

    def getHeader(): Seq[String] = {
        getClass.getDeclaredFields.map { field =>
            field.setAccessible(true)
            field.getName
        }
    }

    def getRow(): Seq[Double] = {
        getClass.getDeclaredFields.map { field =>
            field.setAccessible(true)
            if (field.getName == "variance") {
                utils.helper.variance(field.get(this).asInstanceOf[Variance])
            }
            else {
                field.get(this).toString.toDouble
            }
        }
    }

    def getSolution(): Solution = { (result, M.toInt, variance) }

    def getQuality(): Double = {
        quality
    }

    def getValue(): Double = { result }

    def getUtility(): Double = { utility }

    def getM(): Double = { M }
}