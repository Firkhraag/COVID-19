package application.model

import kotlin.math.*

// Агент (является ли мужчиной, возраст)
class Agent(private val isMale: Boolean, var age: Int) {

    // Состояние здоровья
    // 0 - восприимчивый, 1 - инфицированный, 2 - выздоровевший, 3 - готов перейти в инкубационный период, 4 - мертв
    var healthStatus = if ((0..99999).random() == 0) 1 else 0

    // Дней с момента инфицирования
    var daysInfected = 0

    // Период болезни
    var infectionPeriod = 0
    fun willBeInfectionPeriod() {
        // Тяжелый случай
        // Erlang distribution (mean = 31.0, SD = 10.44)
        val minValueCritical = 18.0
        val maxValueCritical = 48.0
        val meanCritical = 31.0
        val varianceCritical = 109.0

        // Средний случай
        // Erlang distribution (mean = 20.0, SD = 4.45)
        val minValue = 8.0
        val maxValue = 37.0
        val mean = 20.0
        val variance = 19.8

        // Бессимптомный случай
        // Erlang distribution (mean = 14.0, SD = 3.0) (предположение)
        val minValueAsymptomatic = 7.0
        val maxValueAsymptomatic = 24.0
        val meanAsymptomatic = 14.0
        val varianceAsymptomatic = 9.0

        infectionPeriod = when {
            isAsymptomatic -> {
                var scale = varianceAsymptomatic / meanAsymptomatic
                var shape = round(meanAsymptomatic / scale)
                scale = meanAsymptomatic / shape
                val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
                min(maxValueAsymptomatic, max(minValueAsymptomatic, erlangDistribution.sample())).roundToInt()
            }
            willBeInCriticalCondition -> {
                var scale = varianceCritical / meanCritical
                var shape = round(meanCritical / scale)
                scale = meanCritical / shape
                val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
                min(maxValueCritical, max(minValueCritical, erlangDistribution.sample())).roundToInt()
            }
            else -> {
                var scale = variance / mean
                var shape = round(mean / scale)
                scale = mean / shape
                val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
                min(maxValue, max(minValue, erlangDistribution.sample())).roundToInt()
            }
        }
    }

    // Инкубационный период
    var incubationPeriod = 0
    fun willHaveIncubationPeriod() {
        // Erlang distribution (mean = 5.2, SD = 3.7)
        val minValue = 1.0
        val maxValue = 21.0
        val mean = 5.2
        val variance = 13.69
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
        incubationPeriod = min(maxValue, max(minValue, erlangDistribution.sample())).roundToInt()
    }

    // Вероятность бессимптомного протекания болезни ?
    var isAsymptomatic = false
    fun willBeAsymptomatic(a: Double) {
        val probability = if (hasComorbidity) {
            2.0 / (1 + exp(0.1 * age))
        } else {
            2.0 / (1 + exp(0.04 * age))
        }
        isAsymptomatic = (0..9999).random() * 0.0001 < probability
    }

    // Самоизоляция
    var isIsolated = false
    var isolationPeriod = 0
    fun willBeIsolationPeriod() {
        // Erlang distribution (mean = 2.9, SD = 2.1)
        val mean = 2.9
        val variance = 4.41
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
        isolationPeriod = erlangDistribution.sample().roundToInt()
    }

    // Регистрирование случая заболевания коронавирусом
//    var isReported = false
    var reportPeriod = 0
    fun willBeReportPeriod() {
        // Erlang distribution (mean = 6.1, SD = 2.5)
        val mean = 6.1
        val variance = 6.25
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
//        reportPeriod = erlangDistribution.sample().roundToInt()
        reportPeriod = min((infectionPeriod - 1).toDouble(),
            max(isolationPeriod.toDouble(), erlangDistribution.sample())).roundToInt()
    }

    // Хронические заболевания
    fun hasComorbidity(): Boolean {
        val probability = 2.0 / (1 + exp(-0.0111 * age)) - 0.93
        return (0..9999).random() * 0.0001 < probability
    }
    var hasComorbidity = hasComorbidity()
//    var hasComorbidity = false
//    fun findComorbidity() {
//        val probability = 2.0 / (1 + exp(-0.0111 * age)) - 0.93
//        hasComorbidity =  (0..9999).random() * 0.0001 < probability
//    }

    // Тяжелое состояние
    var willBeInCriticalCondition = false
    fun willBeInCriticalCondition() {
        if (isAsymptomatic) {
            return
        }
        val probability = if (hasComorbidity) {
            2 / (1 + exp(-0.011 * age)) - 1.0
        } else {
            2 / (1 + exp(-0.001 * age)) - 1.0
        }
        willBeInCriticalCondition = (0..9999).random() * 0.0001 < probability
    }

    // Смерть
    var willDie = false
    fun willDie() {
        if (!willBeInCriticalCondition) {
            return
        }
        val probability = if (hasComorbidity) {
            2 / (1 + exp(-0.01 * age)) - 1.0
        } else {
            2 / (1 + exp(-0.001 * age)) - 1.0
        }
        willDie = (0..9999).random() * 0.0001 < probability
    }

    // День смерти
    var dayOfDeath = 0
    fun findDayOfDeath() {
        if (!willDie) {
            dayOfDeath = -1
            return
        }
        // Erlang distribution (mean = 17.8, SD = 1.6)
        val mean = 17.8
        val variance = 2.56
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
        dayOfDeath = max((infectionPeriod - 1).toDouble(), erlangDistribution.sample()).roundToInt()
    }

    // Влияние силы инфекции
    var infectivityInfluence = 0.0
    fun findCurrentInfectivityInfluence() {
        val influenceAtTheEnds = 0.05
        infectivityInfluence = if (daysInfected <= 0) {
            val a = -ln(influenceAtTheEnds) / incubationPeriod
            exp(a * daysInfected)
        } else {
            val a = -ln(influenceAtTheEnds) / infectionPeriod
            exp(-a * daysInfected)
        }
    }

    // Влияние восприимчивости
//    fun findSusceptibilityInfluence(lowerPointInfluence: Double) {
//        susceptibilityInfluence = 0.1
////        susceptibilityInfluence = when (age) {
////            in 0..19 -> {
////                val b = 2 * (1 - lowerPointInfluence)
////                b / (1 + exp(0.2 * age)) + lowerPointInfluence
////            }
////            in 20..50 -> {
////                lowerPointInfluence
////            }
////            else -> {
////                val b = 2 * (1 - lowerPointInfluence)
////                b / (1 + exp(-0.2 * (age - 70))) + lowerPointInfluence
////            }
////        } - 0.7
//    }
//    var susceptibilityInfluence = 0.25
    var susceptibilityInfluence = 0.318

    // Обновить эпидемиологические параметры при заражении
    fun updateHealthParameters(asympCoeff: Double) {
        willBeAsymptomatic(asympCoeff)
        willBeInCriticalCondition()
        willDie()
        findDayOfDeath()
        willHaveIncubationPeriod()
        willBeInfectionPeriod()
        willBeIsolationPeriod()
        willBeReportPeriod()
        daysInfected = 1 - incubationPeriod

//        println("isAsymptomatic: $isAsymptomatic")
//        println("isInCriticalCondition: $willBeInCriticalCondition")
//        println("willDie: $willDie")
//        println("dayOfDeath: $dayOfDeath")
//        println("incubationPeriod: $incubationPeriod")
//        println("infectionPeriod: $infectionPeriod")
//        println("isolationPeriod: $isolationPeriod")
//        println("reportPeriod: $reportPeriod")
//        readLine()
    }

    // Массив идентификаторов агентов, с которыми происходят контакты на работе
    var connectedWorkAgents = arrayListOf<Int>()

    // Социальный статус (наличие работы)
    val hasWork = if (isMale) {
        when (age) {
            in 16..19 -> (0..99).random() < 6
            in 20..29 -> (0..99).random() < 92
            in 30..39 -> (0..99).random() < 91
            in 40..49 -> (0..99).random() < 93
            in 50..59 -> (0..99).random() < 95
            in 60..69 -> (0..99).random() < 43
            else -> (0..99).random() < 4
        }
    } else {
        when (age) {
            in 16..19 -> (0..99).random() < 3
            in 20..29 -> (0..99).random() < 83
            in 30..39 -> (0..99).random() < 83
            in 40..49 -> (0..99).random() < 90
            in 50..59 -> (0..99).random() < 84
            in 60..69 -> (0..99).random() < 30
            else -> (0..99).random() < 2
        }
    }

    val isInKindergarten = when (age) {
        // Ясли
        in 0..2 -> (0..99).random() < 23
        // Детсад
        in 3..6 -> (0..99).random() < 83
        else -> false
    }

    val isInSchool = when (age) {
        in 7..15 -> true
        in 16..18 -> !hasWork
        else -> false
    }

    var isGoingToWork = hasWork
}