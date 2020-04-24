package application.model

import kotlin.math.*

// Агент (является ли мужчиной, возраст)
class Agent(private val isMale: Boolean,
            var age: Int) {

    // Состояние здоровья
    // 0 - восприимчивый, 1 - инфицированный, 2 - выздоровевший, 3 - готов перейти в инкубационный период, 4 - мертв
    var healthStatus = if ((0..9999).random() == 0) 1 else 0

    // Дней с момента инфицирования
    var daysInfected = 0

    // Период болезни
    var shouldBeInfected = 0
    fun willBeInfectedPeriod(): Int {
        // Erlang distribution (mean = 15.0, SD = 3.0)
        val mean = 15.0
        val variance = 9.0
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(
            shape, scale)
        // From 4 days
        return erlangDistribution.sample().roundToInt()
    }

    // Инкубационный период
    var incubationPeriod = 0
    fun willHaveIncubationPeriod(): Int {
        // Erlang distribution (mean = 5.2, SD = 3.7)
        val minValue = 1.0
        val maxValue = 21.0
        val mean = 5.2
        val variance = 13.69
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(
            shape, scale)
        return min(maxValue, max(minValue, erlangDistribution.sample())).roundToInt()
    }

    // Вероятность бессимптомного протекания болезни
    var isAsymptomatic = false
    fun willBeAsymptomatic(a: Double): Boolean {
        val probability = if (hasComorbidity) {
            2.0 / (1 + exp(0.1 * age))
        } else {
            2.0 / (1 + exp(0.04 * age))
        }
        return (0..9999).random() * 0.0001 < probability
    }

    // Точка максимальной тяжести болезни
    var changePoint = 0
    fun willHaveChangePointPeriod(): Int {
        return shouldBeInfected / 2
    }

    // Самоизоляция
    var isIsolated = false
    var isolationPeriod = 0
    fun willBeIsolationPeriod(): Int {
        // Erlang distribution (mean = 2.9, SD = 2.1)
        val mean = 2.9
        val variance = 4.41
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(
            shape, scale)
        return erlangDistribution.sample().roundToInt()
    }

    // Регистрирование случая заболевания коронавирусом
    var isReported = false
    var reportPeriod = 0
    fun willBeReportPeriod(): Int {
        // Erlang distribution (mean = 6.1, SD = 2.5)
        val mean = 6.1
        val variance = 6.25
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(
            shape, scale)
        return max(isolationPeriod.toDouble(), erlangDistribution.sample()).roundToInt()
    }

    // Хронические заболевания
    var hasComorbidity = false
    fun findComorbidity() {
        val probability = 2 / (1 + exp(-0.0066 * age)) - 1.0
        hasComorbidity =  (0..9999).random() * 0.0001 < probability
    }

    // Тяжелое состояние
    var isInCriticalCondition = false
    fun chanceToBeInCriticalCondition() {
        val probability = if (hasComorbidity) {
            2 / (1 + exp(-0.006 * age)) - 1.0
        } else {
            2 / (1 + exp(-0.001 * age)) - 1.0
        }
        isInCriticalCondition = (0..9999).random() * 0.0001 < probability
    }

    // Смерть
    fun willDie(): Boolean {
        val probability = if (hasComorbidity) {
            2 / (1 + exp(-0.003 * age)) - 1.0
        } else {
            2 / (1 + exp(-0.0005 * age)) - 1.0
        }
        return (0..9999).random() * 0.0001 < probability
    }

    // Влияние силы инфекции
    var infectivityInfluence = 0.0
    fun findCurrentInfectivityInfluence() {
        val influenceInChangePoint = 0.85
        infectivityInfluence = when {
            daysInfected <= 1 -> {
                val k = 1.0 / incubationPeriod
                val b = k * (incubationPeriod - 1)
                k * daysInfected + b
            }
            daysInfected < changePoint -> {
                val k = (1 - influenceInChangePoint) / (1 - changePoint)
                val b = (changePoint - influenceInChangePoint) / (changePoint - 1)
                k * daysInfected + b
            }
            else -> {
                val k = influenceInChangePoint / (changePoint - shouldBeInfected)
                val b = -k * shouldBeInfected
                k * daysInfected + b
            }
        }
    }

    // Влияние восприимчивости
    var susceptibilityInfluence = 0.0
    fun findSusceptibilityInfluence(lowerPointInfluence: Double) {
        susceptibilityInfluence = 0.1
//        susceptibilityInfluence = when (age) {
//            in 0..19 -> {
//                val b = 2 * (1 - lowerPointInfluence)
//                b / (1 + exp(0.2 * age)) + lowerPointInfluence
//            }
//            in 20..50 -> {
//                lowerPointInfluence
//            }
//            else -> {
//                val b = 2 * (1 - lowerPointInfluence)
//                b / (1 + exp(-0.2 * (age - 70))) + lowerPointInfluence
//            }
//        } - 0.7
    }

    // Обновить эпидемиологические параметры при заражении
    fun updateHealthParameters(asympCoeff: Double) {
        isAsymptomatic = willBeAsymptomatic(asympCoeff)
        incubationPeriod = willHaveIncubationPeriod()
        shouldBeInfected = willBeInfectedPeriod()
        changePoint = willHaveChangePointPeriod()
        isolationPeriod = willBeIsolationPeriod()
        reportPeriod = willBeReportPeriod()
        daysInfected = 1 - incubationPeriod
    }

    // Массив идентификаторов агентов, с которыми происходят контакты на работе
    var connectedWorkAgents = arrayListOf<Int>()

    // Социальный статус (наличие работы)
    private val hasWork = if (isMale) {
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

    // Посещает работу в условиях карантина 30%
    var isGoingToWork = if (hasWork) (0..9).random() < 3 else false
}