package application.model

import kotlin.math.*

// Агент (является ли мужчиной, возраст)
class Agent(private val isMale: Boolean, var age: Int) {

    // Состояние здоровья
    // 0 - восприимчивый, 1 - инфицированный, 2 - выздоровевший, 3 - готов перейти в инкубационный период, 4 - мертв
    var healthStatus = 0

    // Дней с момента инфицирования
    var daysInfected = 0

    // Инкубационный период
    var incubationPeriod = 0
    private fun willHaveIncubationPeriod() {
        // Erlang distribution (mean = 5.2, SD = 3.7)
        val minValue = 1.0
        val maxValue = 21.0
        val mean = durationIncubationParameter
        val variance = 13.69
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
        incubationPeriod = min(maxValue, max(minValue, erlangDistribution.sample())).roundToInt()
    }

    // Период болезни
    var infectionPeriod = 0
    fun willBeInfectionPeriod() {
        // Тяжелый случай
        // Erlang distribution (mean = 31.0, SD = 10.44)
        val minValueCritical = 18.0
        val maxValueCritical = 48.0
        val meanCritical = durationCriticalParameter
        val varianceCritical = 109.0

        // Средний случай
        // Erlang distribution (mean = 20.0, SD = 4.45)
        val minValue = 8.0
        val maxValue = 37.0
        val mean = durationSymptomaticParameter
        val variance = 19.8

        // Бессимптомный случай
        // Erlang distribution (mean = 14.0, SD = 3.0) (предположение)
        val minValueAsymptomatic = 7.0
        val maxValueAsymptomatic = 24.0
        val meanAsymptomatic = durationAsymptomaticParameter
        val varianceAsymptomatic = 9.0

        infectionPeriod = when {
            isAsymptomatic -> {
                var scale = varianceAsymptomatic / meanAsymptomatic
                val shape = round(meanAsymptomatic / scale)
                scale = meanAsymptomatic / shape
                val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
                min(maxValueAsymptomatic, max(minValueAsymptomatic, erlangDistribution.sample())).roundToInt()
            }
            willBeInCriticalCondition -> {
                var scale = varianceCritical / meanCritical
                val shape = round(meanCritical / scale)
                scale = meanCritical / shape
                val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
                min(maxValueCritical, max(minValueCritical, erlangDistribution.sample())).roundToInt()
            }
            else -> {
                var scale = variance / mean
                val shape = round(mean / scale)
                scale = mean / shape
                val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
                min(maxValue, max(minValue, erlangDistribution.sample())).roundToInt()
            }
        }
    }

//    0.0057
    // Хронические заболевания
    var hasComorbidity = (0..9999).random() * 0.0001 <
        exp(comorbidity1Parameter * age) - comorbidity2Parameter
    //            2.0 / (1 + exp(-comorbidity1Parameter * age)) - comorbidity2Parameter

    // Вероятность бессимптомного протекания болезни
    var isAsymptomatic = false
    private fun willBeAsymptomatic() {
        val probability = if (hasComorbidity) {
            2.0 / (1 + exp(asymptomatic1Parameter * age))
        } else {
            2.0 / (1 + exp(asymptomatic2Parameter * age))
        }
        isAsymptomatic = (0..9999).random() * 0.0001 < probability
    }

    // Самоизоляция
    var isIsolated = false
    var isolationPeriod = 0
    fun willBeIsolationPeriod() {
        // Erlang distribution (mean = 2.9, SD = 2.1)
        val mean = isolationParameter
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
        val mean = reportParameter
        val variance = 6.25
        var scale = variance / mean
        var shape = round(mean / scale)
        scale = mean / shape
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(shape, scale)
//        reportPeriod = erlangDistribution.sample().roundToInt()
        reportPeriod = min((infectionPeriod - 1).toDouble(),
            max(isolationPeriod.toDouble(), erlangDistribution.sample())).roundToInt()
    }

    // Тяжелое состояние
    var willBeInCriticalCondition = false
    fun willBeInCriticalCondition() {
        if (isAsymptomatic) {
            return
        }
        val probability = if (hasComorbidity) {
//            2 / (1 + exp(-critical1Parameter * age)) - 1.0
            exp(critical1Parameter * age) - 1.0
        } else {
//            2 / (1 + exp(-critical2Parameter * age)) - 1.0
            exp(critical2Parameter * age) - 1.0
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
//            2 / (1 + exp(-death1Parameter * age)) - 1.0
            exp(death1Parameter * age) - 1.0
        } else {
//            2 / (1 + exp(-death2Parameter * age)) - 1.0
            exp(death2Parameter * age) - 1.0
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
        val mean = deathDayParameter
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
        infectivityInfluence = if (daysInfected <= 0) {
            val a = -ln(viralLoadInfluenceParameter) / incubationPeriod
            exp(a * daysInfected)
        } else {
            val a = -ln(viralLoadInfluenceParameter) / infectionPeriod
            exp(-a * daysInfected)
        }
    }

    // Влияние восприимчивости
    var susceptibilityInfluence = when (age) {
        in 0..19 -> {
            val b = 2 * (1 - susceptibilityInfluenceParameter)
            b / (1 + exp(0.35 * age)) + susceptibilityInfluenceParameter
        }
        in 20..50 -> {
            susceptibilityInfluenceParameter
        }
        else -> {
            val b = 2 * (1 - susceptibilityInfluenceParameter)
            b / (1 + exp(-0.35 * (age - 70))) + susceptibilityInfluenceParameter
        }
    } - susceptibilityInfluence2Parameter

    // Обновить эпидемиологические параметры при заражении
    fun updateHealthParameters(initialization: Boolean) {
        willBeAsymptomatic()
        willBeInCriticalCondition()
        willDie()
        findDayOfDeath()
        willHaveIncubationPeriod()
        willBeInfectionPeriod()
        willBeIsolationPeriod()
        willBeReportPeriod()
        daysInfected = if (initialization) {
            (1 - incubationPeriod..isolationPeriod).random()
        } else {
            1 - incubationPeriod
        }

        // Debug
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

    // Наличие рабочего места
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

    // Посещает детский сад
    val isInKindergarten = when (age) {
        // Ясли
        in 0..2 -> (0..99).random() < 23
        // Детсад
        in 3..6 -> (0..99).random() < 83
        else -> false
    }

    // Посещает школу
    val isInSchool = when (age) {
        in 7..15 -> true
        in 16..18 -> !hasWork
        else -> false
    }

    // Посещает работу
    var isGoingToWork = hasWork
}