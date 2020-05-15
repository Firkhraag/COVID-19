package application.model

// Средняя продолжительность инкубационного периода, дней
var durationIncubationParameter = 0.0
// Средняя продолжительность периода болезни в тяжелом случае, дней
var durationCriticalParameter = 0.0
// Средняя продолжительность периода болезни, дней
var durationSymptomaticParameter = 0.0
// Средняя продолжительность периода болезни в бессимптомном случае, дней
var durationAsymptomaticParameter = 0.0
// День самоизоляции
var isolationParameter = 0.0
// День выявления
var reportParameter = 0.0
// День смерти
var deathDayParameter = 0.0
// Начальная доля больных
var startingInfectedParameter = 0

// Параметры
var comorbidity1Parameter = 0.0
var comorbidity2Parameter = 0.0
var asymptomatic1Parameter = 0.0
var asymptomatic2Parameter = 0.0
var critical1Parameter = 0.0
var critical2Parameter = 0.0
var death1Parameter = 0.0
var death2Parameter = 0.0

// Сдвиг сигмоиды влияния продолжительности контакта
var durationInfluenceParameter = 0.0
// Влияние силы инфекции на концах
var viralLoadInfluenceParameter = 0.0
// Влияние восприимчивости
var susceptibilityInfluenceParameter = 0.0
var susceptibilityInfluence2Parameter = 0.0

fun setParameters(
    durationIncubationParameter: Double,
    durationCriticalParameter: Double,
    durationSymptomaticParameter: Double,
    durationAsymptomaticParameter: Double,
    isolationParameter: Double,
    reportParameter: Double,
    deathDayParameter: Double,
    startingInfectedParameter: Int,

    comorbidity1Parameter: Double,
    comorbidity2Parameter: Double,
    asymptomatic1Parameter: Double,
    asymptomatic2Parameter: Double,
    critical1Parameter: Double,
    critical2Parameter: Double,
    death1Parameter: Double,
    death2Parameter: Double,

    durationInfluenceParameter: Double,
    viralLoadInfluenceParameter: Double,
    susceptibilityInfluenceParameter: Double,
    susceptibilityInfluence2Parameter: Double
) {
    application.model.durationIncubationParameter = durationIncubationParameter
    application.model.durationCriticalParameter = durationCriticalParameter
    application.model.durationSymptomaticParameter = durationSymptomaticParameter
    application.model.durationAsymptomaticParameter = durationAsymptomaticParameter
    application.model.isolationParameter = isolationParameter
    application.model.reportParameter = reportParameter
    application.model.deathDayParameter = deathDayParameter
    application.model.startingInfectedParameter = startingInfectedParameter

    application.model.comorbidity1Parameter = comorbidity1Parameter
    application.model.comorbidity2Parameter = comorbidity2Parameter
    application.model.asymptomatic1Parameter = asymptomatic1Parameter
    application.model.asymptomatic2Parameter = asymptomatic2Parameter
    application.model.critical1Parameter = critical1Parameter
    application.model.critical2Parameter = critical2Parameter
    application.model.death1Parameter = death1Parameter
    application.model.death2Parameter = death2Parameter

    application.model.durationInfluenceParameter = durationInfluenceParameter
    application.model.viralLoadInfluenceParameter = viralLoadInfluenceParameter
    application.model.susceptibilityInfluenceParameter = susceptibilityInfluenceParameter
    application.model.susceptibilityInfluence2Parameter = susceptibilityInfluence2Parameter
}