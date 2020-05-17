package application.model

import application.utility.writeTableInt
import kotlinx.coroutines.*
import application.utility.readTableDouble
import application.utility.readTableInt
import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.scene.chart.XYChart
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*

class World(private val progress: ReadOnlyDoubleWrapper) {
    // Параметры, отвечающие за дату
    // День
    private var day = 15
    // Месяц
    private var month = 3
    // День недели
    private var dayOfTheWeek = 7
    // Шаг модели
    private var globalDay = 0

    // Общее число случаев, Число активных больных, Число новых случаев, Число смертей, Число выздоровевших
    // Зарегистрированные
    private var stats = arrayListOf(0, 0, 0, 0, 0)
    // Данные по заболеваемости, выздоровлению, смертности
    private val realData = arrayListOf(arrayListOf<Int>())
    // Число известных дней реальных данных
    private val realDataDayNumber = 62

    // Случаи по возрастам
    private var ageStats = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0)
    // Тяжелые случаи по возрастам: 0-9, 10-19, 20-29, 30-39, 40-49, 50-59, 60-69, 70+
    private var criticalStats = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0)
    // Смерти по возрастам: 0-9, 10-19, 20-29, 30-39, 40-49, 50-59, 60-69, 70+
    private var deathStats = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0)

    // Домохозяйства
    private val households = arrayListOf<Household>()
    // Рабочие коллективы
    private val workplace = Workplace()
    // Детские сады
    private val kindergarten = Kindergarten()
    // Школы
    private val school = School()

    // Данные по переписи 2010 года по всему городу
    // 1 взрослый в домохозяйстве
    // 1 Человек - 29%, 2 Человека - 40%, 3 Человека - 31%, 4 Человека - 18%, 5 Человек - 11%
    private val householdsWith1PeoplePercentage = 29

    // 2+ людей с детьми в домохозяйстве - 36%
    private val householdsWithChildrenPercentage = 36

    // 1 Ребенок - 75%, 2 Ребенка - 22%, 3 Ребенка - 3%
    private val householdsWith1ChildrenPercentage = 75
    private val householdsWith12ChildrenPercentage = 97

    // 2+ людей без детей в домохозяйстве
    // 2 Человека - 55%, 3 Человека - 30%, 4 Человека - 12%, 5 Человек - 3%
    private val householdsWith2People0ChildrenPercentage = 55
    private val householdsWith23People0ChildrenPercentage = 85
    private val householdsWith234People0ChildrenPercentage = 97

    // 2+ людей с 1 ребенком в домохозяйстве
    // 2 Человека - 18%, 3 Человека - 40%, 4 Человека - 22%, 5 Человек - 20%
    private val householdsWith2People1ChildrenPercentage = 18
    private val householdsWith23People1ChildrenPercentage = 58
    private val householdsWith234People1ChildrenPercentage = 80

    // 2+ людей с 2 детьми в домохозяйстве
    // 3 Человека - 15%, 4 Человека - 54%, 5 Человек - 31%
    private val householdsWith3People2ChildrenPercentage = 15
    private val householdsWith34People2ChildrenPercentage = 69

    // Продолжительности контактов в домохозяйстве
    private fun getHouseholdContactDuration(): Double {
        // Normal distribution (mean = 12.4, SD = 5.13)
        val rand = java.util.Random()
        return min(24.0, max(0.0,12.4 + rand.nextGaussian() * 5.13))
    }

    private fun getHouseholdContactDurationWithKindergarten(): Double {
        // Normal distribution (mean = 5.0, SD = 2.05)
        val rand = java.util.Random()
        return min(20.0, max(0.0,5.0 + rand.nextGaussian() * 2.05))
    }

    private fun getHouseholdContactDurationWithWork(): Double {
        // Normal distribution (mean = 8.0, SD = 3.28)
        val rand = java.util.Random()
        return min(20.0, max(0.0,5.0 + rand.nextGaussian() * 2.05))
    }

    private fun getHouseholdContactDurationWithSchool(): Double {
        // Normal distribution (mean = 6.0, SD = 2.46)
        val rand = java.util.Random()
        return min(20.0, max(0.0,6.0 + rand.nextGaussian() * 2.46))
    }

    private fun getHouseholdContactDurationWithUniversity(): Double {
        // Normal distribution (mean = 9.0, SD = 3.69)
        val rand = java.util.Random()
        return min(20.0, max(0.0,7.0 + rand.nextGaussian() * 3.69))
    }

    // Продолжительность контактов на работе
    private fun getWorkplaceContactDuration(): Double {
        // Exponential distribution (mean = 3.0, SD = 3.0)
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(1.0, 3.0)
        return min(8.0, erlangDistribution.sample())
    }

    // Продолжительность контактов в школе
    private fun getSchoolContactDuration(): Double {
        // Normal distribution (mean = 4.783, SD = 2.67)
        val rand = java.util.Random()
        return max(0.0,4.783 + rand.nextGaussian() * 2.67)
    }

    // Продолжительность контактов в университете
    private fun getUniversityContactDuration(): Double {
        // Exponential distribution (mean = 2.1, SD = 3.0)
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(1.0, 2.1)
        return erlangDistribution.sample()
    }

    // Продолжительность контактов в детских садах
    private fun getKindergartenContactDuration(): Double {
        // Normal distribution (mean = 5.88, SD = 2.52)
        val rand = java.util.Random()
        return max(0.0,5.88 + rand.nextGaussian() * 2.52)
    }

    // Создание агента
    private fun createAgent(indexForAll: Int, indexForMales: Int,
                            districtsAgeSexRatioMatrix: ArrayList<ArrayList<Double>>, isChild: Boolean): Agent {
        // Случайное число для возраста
        val ageRandomNum = (0..999).random() * 0.001
        // Случайное число для пола
        val sexRandomNum = (0..999).random() * 0.001
        return if (isChild) {
            // Ребенок
            when {
                // 0 лет
                ageRandomNum < districtsAgeSexRatioMatrix[0][indexForAll] -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[0][indexForMales]), 0)
                }
                // 1 год
                (ageRandomNum >= districtsAgeSexRatioMatrix[0][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[1][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[1][indexForMales]), 1)
                }
                // 2 года
                (ageRandomNum >= districtsAgeSexRatioMatrix[1][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[2][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[2][indexForMales]), 2)
                }
                // 3-5 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[2][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[3][indexForAll]) -> {
                    val isMale = (sexRandomNum < districtsAgeSexRatioMatrix[3][indexForMales])
                    val randomNum3 = (1..100).random()
                    when {
                        randomNum3 <= 35 -> Agent(isMale, 3)
                        randomNum3 in 36..68 -> Agent(isMale, 4)
                        else -> Agent(isMale, 5)
                    }
                }
                // 6 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[3][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[4][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[4][indexForMales]), 6)
                }
                // 7 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[4][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[5][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[5][indexForMales]), 7)
                }
                // 8-13 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[5][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[6][indexForAll]) -> {
                    val isMale = (sexRandomNum < districtsAgeSexRatioMatrix[6][indexForMales])
                    val randomNum3 = (1..100).random()
                    when {
                        randomNum3 <= 18 -> Agent(isMale, 8)
                        randomNum3 in 19..36 -> Agent(isMale, 9)
                        randomNum3 in 37..53 -> Agent(isMale, 10)
                        randomNum3 in 54..69 -> Agent(isMale, 11)
                        randomNum3 in 70..84 -> Agent(isMale, 12)
                        else -> Agent(isMale, 13)
                    }
                }
                // 14-15 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[6][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[7][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[7][indexForMales]), (14..15).random())
                }
                // 16-17 лет
                else -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[8][indexForMales]), (16..17).random())
                }
            }
        } else {
            // Взрослый
            when {
                // 18-19 лет
                ageRandomNum < districtsAgeSexRatioMatrix[9][indexForAll] -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[9][indexForMales]), (18..19).random())
                }
                // 20-24 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[9][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[10][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[10][indexForMales]), (20..24).random())
                }
                // 25-29 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[10][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[11][indexForAll]) -> {
                    val isMale = (sexRandomNum < districtsAgeSexRatioMatrix[11][indexForMales])
                    val randomNum3 = (1..100).random()
                    when {
                        randomNum3 <= 13 -> Agent(isMale, 25)
                        randomNum3 in 14..31 -> Agent(isMale, 26)
                        randomNum3 in 32..52 -> Agent(isMale, 27)
                        randomNum3 in 53..76 -> Agent(isMale, 28)
                        else -> Agent(isMale, 29)
                    }
                }
                // 30-34 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[11][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[12][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[12][indexForMales]), (30..34).random())
                }
                // 35-39 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[12][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[13][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[13][indexForMales]), (35..39).random())
                }
                // 40-44 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[13][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[14][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[14][indexForMales]), (40..44).random())
                }
                // 45-49 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[14][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[15][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[15][indexForMales]), (45..49).random())
                }
                // 50-54 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[15][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[16][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[16][indexForMales]), (50..54).random())
                }
                // 55-59 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[16][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[17][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[17][indexForMales]), (55..59).random())
                }
                // 60-64 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[17][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[18][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[18][indexForMales]), (60..64).random())
                }
                // 65-69 лет
                (ageRandomNum >= districtsAgeSexRatioMatrix[18][indexForAll]) &&
                        (ageRandomNum < districtsAgeSexRatioMatrix[19][indexForAll]) -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[19][indexForMales]), (65..69).random())
                }
                // 70+ лет
                else -> {
                    Agent((sexRandomNum < districtsAgeSexRatioMatrix[20][indexForMales]), 70)
                }
            }
        }
    }

    // Создание партнера
    private fun addSpouse(agentMale: Agent): Agent {
        val difference = when ((1..100).random()) {
            in 1..3 -> (-20..-15).random()
            in 4..8 -> (-14..-10).random()
            in 9..20 -> (-9..-6).random()
            in 21..33 -> (-5..-4).random()
            in 34..53 -> (-3..-2).random()
            in 54..86 -> (-1..1).random()
            in 87..93 -> (2..3).random()
            in 94..96 -> (4..5).random()
            in 97..98 -> (6..9).random()
            else -> (10..14).random()
        }
        var spouseAge = agentMale.age + difference
        if (spouseAge < 18) {
            spouseAge = 18
        } else if (spouseAge > 70) {
            spouseAge = 70
        }
        return Agent(false, spouseAge)
    }

    // Добавление агентов в коллективы
    @Synchronized private fun addAgentsToGroups(agents: ArrayList<Agent>, household: Household) {
        agents.forEach { agent ->
            // Добавление в коллективы
            when {
                agent.hasWork -> {
                    workplace.addAgent(agent)
                }
                agent.isInSchool -> {
                    school.addAgent(agent)
                }
                agent.isInKindergarten -> {
                    kindergarten.addAgent(agent)
                }
            }
            // Добавление в домохозяйство
            household.addAgent(agent)
        }
        // Добавление нового домохозяйства в массив домохозяйств
        households.add(household)
    }

    // Задача создания домохозяйств с параллельным исполнением
    private suspend fun processAll(
        districtsAgeSexRatioMatrix: ArrayList<ArrayList<Double>>) = withContext(Dispatchers.IO
    ) {
        // Число людей по районам города
        val numOfPeopleInDistricts = arrayListOf(161911, 208713, 518709, 533597, 336248, 191847, 357498,
            397609, 44321, 78131, 354525, 527861, 131356, 143154, 568516, 217983, 394972, 216939)
        // Счетчик прогресса создания популяции
        val counter = AtomicInteger()
        // Проходим по каждому району
        for (index in (0 until numOfPeopleInDistricts.size)) {
            launch {
                val indexForAll = index * 3
                val indexForMales = index * 3 + 1

                // Добавляем агентов, пока не достигнем числа людей в текущем районе
                var numOfAgentsAdded = 0
                while (numOfAgentsAdded < numOfPeopleInDistricts[index]) {
                    val agents = arrayListOf<Agent>()
                    when {
                        // Домохозяйство с 1 взрослым
                        (0..99).random() < householdsWith1PeoplePercentage -> {
                            val household = Household()
                            val agent = createAgent(indexForAll, indexForMales,
                                districtsAgeSexRatioMatrix, false)
                            agents.add(agent)
                            addAgentsToGroups(agents, household)
                        }
                        // Домохозяйство с детьми младше 18 лет
                        (0..99).random() < householdsWithChildrenPercentage -> {
                            var randomNumber = (1..100).random()
                            when {
                                // 1 ребенок
                                randomNumber <= householdsWith1ChildrenPercentage -> {
                                    randomNumber = (1..100).random()
                                    when {
                                        // 2 человек
                                        randomNumber <= householdsWith2People1ChildrenPercentage -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            addAgentsToGroups(agents, household)
                                        }
                                        // 3 человек
                                        randomNumber in (householdsWith2People1ChildrenPercentage+1..
                                                householdsWith23People1ChildrenPercentage) -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent3 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            agents.add(agent3)
                                            addAgentsToGroups(agents, household)
                                        }
                                        // 4 человек
                                        randomNumber in (householdsWith23People1ChildrenPercentage+1..
                                                householdsWith234People1ChildrenPercentage) -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent3 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent4 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            agents.add(agent3)
                                            agents.add(agent4)
                                            addAgentsToGroups(agents, household)
                                        }
                                        // 5 человек
                                        else -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent3 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent4 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent5 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            agents.add(agent3)
                                            agents.add(agent4)
                                            agents.add(agent5)
                                            addAgentsToGroups(agents, household)
                                        }
                                    }
                                }
                                // 2 ребенка
                                randomNumber in (householdsWith1ChildrenPercentage+1..
                                        householdsWith12ChildrenPercentage) -> {
                                    randomNumber = (1..100).random()
                                    when {
                                        // 3 человек
                                        randomNumber <= householdsWith3People2ChildrenPercentage -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            val agent3 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            agents.add(agent3)
                                            addAgentsToGroups(agents, household)
                                        }
                                        // 4 человек
                                        randomNumber in (householdsWith3People2ChildrenPercentage+1..
                                                householdsWith34People2ChildrenPercentage) -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent3 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            val agent4 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            agents.add(agent3)
                                            agents.add(agent4)
                                            addAgentsToGroups(agents, household)
                                        }
                                        // 5 человек
                                        else -> {
                                            val household = Household()
                                            val agent = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent2 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent3 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, false)
                                            val agent4 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            val agent5 = createAgent(indexForAll, indexForMales,
                                                districtsAgeSexRatioMatrix, true)
                                            agents.add(agent)
                                            agents.add(agent2)
                                            agents.add(agent3)
                                            agents.add(agent4)
                                            agents.add(agent5)
                                            addAgentsToGroups(agents, household)
                                        }
                                    }
                                }
                                // 3 ребенка
                                else -> {
                                    // 5 человек
                                    val household = Household()
                                    val agent = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent2 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent3 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, true)
                                    val agent4 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, true)
                                    val agent5 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, true)
                                    agents.add(agent)
                                    agents.add(agent2)
                                    agents.add(agent3)
                                    agents.add(agent4)
                                    agents.add(agent5)
                                    addAgentsToGroups(agents, household)
                                }
                            }
                        }
                        // Домохозяйство без детей
                        else -> {
                            val randomNumber = (1..100).random()
                            when {
                                // 2 человек
                                randomNumber <= householdsWith2People0ChildrenPercentage -> {
                                    val household = Household()
                                    val agent = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent2 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    agents.add(agent)
                                    agents.add(agent2)
                                    addAgentsToGroups(agents, household)
                                }
                                // 3 человек
                                randomNumber in (householdsWith2People0ChildrenPercentage+1..
                                        householdsWith23People0ChildrenPercentage) -> {
                                    val household = Household()
                                    val agent = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent2 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent3 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    agents.add(agent)
                                    agents.add(agent2)
                                    agents.add(agent3)
                                    addAgentsToGroups(agents, household)
                                }
                                // 4 человек
                                randomNumber in (householdsWith23People0ChildrenPercentage+1..
                                        householdsWith234People0ChildrenPercentage) -> {
                                    val household = Household()
                                    val agent = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent2 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent3 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent4 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    agents.add(agent)
                                    agents.add(agent2)
                                    agents.add(agent3)
                                    agents.add(agent4)
                                    addAgentsToGroups(agents, household)
                                }
                                // 5 человек
                                else -> {
                                    val household = Household()
                                    val agent = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent2 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent3 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent4 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    val agent5 = createAgent(indexForAll, indexForMales,
                                        districtsAgeSexRatioMatrix, false)
                                    agents.add(agent)
                                    agents.add(agent2)
                                    agents.add(agent3)
                                    agents.add(agent4)
                                    agents.add(agent5)
                                    addAgentsToGroups(agents, household)
                                }
                            }
                        }
                    }
                    numOfAgentsAdded += agents.size
                }
                progress.set(counter.incrementAndGet() / 18.0)
            }
        }
    }

    // Инициализация
    private fun addHouseholdsToPool(districtsAgeSexRatioMatrix: ArrayList<ArrayList<Double>>) {
        runBlocking {
            processAll(districtsAgeSexRatioMatrix)
        }
        println("Households created")
        // Создание графов в оставшихся компаниях
        workplace.generateLastBarabasiAlbertNetworks()
        println("World creation has ended")
    }

    // Контакты внутри домохозяйства
    private fun contactsInHouseholdForAgent(
        isContactOnHoliday: Boolean, household: Household, agent: Agent,
        isWorkingHoliday: Boolean = false,
        isKindergartenHoliday: Boolean = false, isSchoolHoliday: Boolean = false
    ) {
        household.group.agents.forEach { agent2 ->
            // agent инфицирован, agent2 восприимчив
            if (agent2.healthStatus == 0) {
                // Случайное число
                val randomNum = (0..9999).random() * 0.0001
                // Влияние продолжительности контакта
                val durationInfluence = if (isContactOnHoliday) {
                    // На выходных
                    1 / (1 + exp(-getHouseholdContactDuration() + durationInfluenceParameter))
                } else {
                    if (((agent.isInKindergarten && !agent.isIsolated) || agent2.isInKindergarten) &&
                        (!isKindergartenHoliday)) {
                        // Один из них посещал детский сад
                        1 / (1 + exp(-getHouseholdContactDurationWithKindergarten() + durationInfluenceParameter))
                    } else if (((agent.isGoingToWork && !agent.isIsolated) || agent2.isGoingToWork) &&
                        (!isWorkingHoliday)) {
                        // Один из них посещал работу
                        1 / (1 + exp(-getHouseholdContactDurationWithWork() + durationInfluenceParameter))
                    } else if (((agent.isInSchool && !agent.isIsolated) || agent2.isInSchool) &&
                        (!isSchoolHoliday)) {
                        // Один из них посещал школу
                        1 / (1 + exp(-getHouseholdContactDurationWithSchool() + durationInfluenceParameter))
                    } else {
                        // Оба сидят дома
                        1 / (1 + exp(-getHouseholdContactDuration() + durationInfluenceParameter))
                    }
                }
                // Вероятность заражения при контакте
                val probabilityOfInfection = agent.infectivityInfluence *
                        agent.susceptibilityInfluence * durationInfluence
//                println("Infectivity: ${agent.infectivityInfluence}")
//                println("Susceptibility: ${agent.susceptibilityInfluence}")
//                println("Duration: ${durationInfluence}")
//                println("Probability: $probabilityOfInfection")
//                readLine()

//                if (agent.infectivityInfluence > 0.001) {
//                    println("Infectivity: ${agent.infectivityInfluence}")
//                    println("Susceptibility: ${agent.susceptibilityInfluence}")
//                    println("Duration: ${durationInfluence}")
//                    println("Probability: $probabilityOfInfection")
//                    readLine()
//                }
                if (randomNum < probabilityOfInfection) {
                    agent2.healthStatus = 3
                }
            }
        }
    }

    // Контакты в детсадах, школах
    private fun contactsInGroupForAgent(group: Group, agent: Agent) {
        group.agents.forEach { agent2 ->
            // agent инфицирован, agent2 восприимчив
            if (agent2.healthStatus == 0) {
                // Случайное число
                val randomNum = (0..9999).random() * 0.0001
                // Влияние продолжительности контакта
                val durationInfluence = when {
                    agent.isInKindergarten -> 1 / (1 + exp(
                        -getKindergartenContactDuration() + durationInfluenceParameter))
                    agent.isInSchool -> 1 / (1 + exp(
                        -getSchoolContactDuration() + durationInfluenceParameter))
                    else -> error("Should be in a collective")
                }
                // Вероятность заражения при контакте
                val probabilityOfInfection = agent.infectivityInfluence *
                        agent.susceptibilityInfluence * durationInfluence
                if (randomNum < probabilityOfInfection) {
                    agent2.healthStatus = 3
                }
            }
        }
    }

    // Контакты в рабочих коллективах
    private fun contactsInGroupForAgentAtWork(group: Group, agent: Agent) {
        group.agents.forEachIndexed { index, agent2 ->
            // agent инфицирован, agent2 восприимчив и имеет связь с больным агентом
            if ((agent2.healthStatus == 0) && (index in agent.connectedWorkAgents)) {
                // Случайное число
                val randomNum = (0..9999).random() * 0.0001
                // Влияние продолжительности контакта
                val durationInfluence = 1 / (1 + exp(
                    -getWorkplaceContactDuration() + durationInfluenceParameter))
                // Вероятность заражения при контакте
                val probabilityOfInfection = agent.infectivityInfluence *
                        agent.susceptibilityInfluence * durationInfluence
                if (randomNum < probabilityOfInfection) {
                    agent2.healthStatus = 3
                }
            }
        }
    }

    // Симуляция
    fun runSimulation(numOfIter: Int, series1: XYChart.Series<String, Number>,
                      series2: XYChart.Series<String, Number>, series3: XYChart.Series<String, Number>,
                      series4: XYChart.Series<String, Number>, series1Real: XYChart.Series<String, Number>,
                      series2Real: XYChart.Series<String, Number>, series3Real: XYChart.Series<String, Number>,
                      series4Real: XYChart.Series<String, Number>, dateLabelText: SimpleStringProperty) {

        // Окончание карантина
        var quarantineEnded = false

        // Цикл симуляции
        while(true) {
            // Закрытие 70% компаний на 15-й день
            if (globalDay == 14) {
                workplace.companies.parallelStream().forEach { company ->
                    if ((0..9).random() < 7) {
                        company.isActiveGroup = false
                        company.agents.forEach { agent ->
                            agent.isGoingToWork = false
                        }
                    }
                }
            }

            // Число новых случаев устанавливаем в 0 на новом шаге
            stats[2] = 0

            // Является ли день выходным
            var isHoliday = false
            // Воскресенье
            if (dayOfTheWeek == 7) {
                isHoliday = true
            }
            // Новогодние праздники
            if ((month == 1) && (day in arrayListOf(1, 2, 3, 4, 5, 6, 7, 8))) {
                isHoliday = true
            }
            // 1, 9 мая
            if ((month == 5) && (day in arrayListOf(1, 9))) {
                isHoliday = true
            }
            // 23 февраля
            if ((month == 2) && (day == 23)) {
                isHoliday = true
            }
            // 8 марта
            if ((month == 3) && (day == 8)) {
                isHoliday = true
            }
            // 12 июня
            if ((month == 6) && (day == 12)) {
                isHoliday = true
            }
            // 4 ноября
            if ((month == 11) && (day == 4)) {
                isHoliday = true
            }

            // Рабочий выходной
            var isWorkingHoliday = false
            // Суббота
            if (dayOfTheWeek == 6) {
                isWorkingHoliday = true
            }

            if (quarantineEnded) {
                // Карантин снят

                // Выходной в детском саду
                var isKindergartenHoliday = isWorkingHoliday
                // Закрытие детсада на лето
                if (month in arrayListOf(7, 8)) {
                    isKindergartenHoliday = true
                }

                // Школьные каникулы
                var isSchoolHoliday = false
                // Летние каникулы
                if (month in arrayListOf(6, 7, 8)) {
                    isSchoolHoliday = true
                }
                // Осенние каникулы
                if ((month == 10) && (day in arrayListOf(28, 29, 30, 31))) {
                    isSchoolHoliday = true
                }
                if ((month == 11) && (day in arrayListOf(1, 2, 3))) {
                    isSchoolHoliday = true
                }
                // Зимние каникулы
                if ((month == 12) && (day in arrayListOf(28, 29, 30, 31))) {
                    isSchoolHoliday = true
                }
                if ((month == 1) && (day in arrayListOf(8, 9, 10, 11))) {
                    isSchoolHoliday = true
                }
                // Весенние каникулы
                if ((month == 3) && (day in arrayListOf(23, 24, 25, 26, 27, 28, 29))) {
                    isSchoolHoliday = true
                }

                // Контакты в детском саду
                if (!isKindergartenHoliday) {
                    kindergarten.groupsByAge.parallelStream().forEach { groupByAge ->
                        groupByAge.forEach { group ->
                            group.agents.forEach { agent ->
                                if ((agent.healthStatus == 1) &&
                                    (!agent.isIsolated)) {
                                    contactsInGroupForAgent(group, agent)
                                }
                            }
                        }
                    }
                }
                // Контакты в рабочем коллективе
                if (!isWorkingHoliday) {
                    workplace.companies.parallelStream().forEach { company ->
                        company.agents.forEach { agent ->
                            if ((agent.healthStatus == 1) && (!agent.isIsolated)) {
                                contactsInGroupForAgentAtWork(company, agent)
                            }
                        }
                    }
                }
                // Контакты в школе
                if (!isSchoolHoliday) {
                    school.groupsByAge.forEachIndexed { index, groupByAge ->
                        if ((index == 0) && (isWorkingHoliday)) {
                            // 1-й класс не учится по Субботам
                        } else {
                            groupByAge.forEach { group ->
                                group.agents.forEach { agent ->
                                    if ((agent.healthStatus == 1) &&
                                        (!agent.isIsolated)) {
                                        contactsInGroupForAgent(group, agent)
                                    }
                                }
                            }
                        }
                    }
                }
                // Контакты в домохозяйстве
                households.parallelStream().forEach { household ->
                    household.group.agents.forEach { agent ->
                        if (agent.healthStatus == 1) {
                            contactsInHouseholdForAgent(false, household, agent,
                                isWorkingHoliday, isKindergartenHoliday, isSchoolHoliday)
                        }
                    }
                }
            } else {
                // Карантин не снят
                if (isHoliday || isWorkingHoliday) {
                    // Контакты в домохозяйстве на выходных
                    households.parallelStream().forEach { household ->
                        household.group.agents.forEach { agent ->
                            if (agent.healthStatus == 1) {
                                contactsInHouseholdForAgent(true, household, agent)
                            }
                        }
                    }
                } else {
                    // Контакты в рабочем коллективе
                    workplace.companies.parallelStream().forEach { company ->
                        if (company.isActiveGroup) {
                            company.agents.forEach { agent ->
                                if ((agent.healthStatus == 1) && (!agent.isIsolated)) {
                                    contactsInGroupForAgentAtWork(company, agent)
                                }
                            }
                        }
                    }
                    // Контакты в домохозяйстве
                    households.parallelStream().forEach { household ->
                        household.group.agents.forEach { agent ->
                            if (agent.healthStatus == 1) {
                                contactsInHouseholdForAgent(false, household, agent,
                                    isWorkingHoliday)
                            }
                        }
                    }
                }
            }

            households.forEach { household ->
                household.group.agents.forEach { agent ->
                    when (agent.healthStatus) {
                        // Переход в инфицированное состояние
                        3 -> {
                            agent.healthStatus = 1
                            agent.updateHealthParameters()
                            when (agent.age) {
                                in 0..9 -> ageStats[0]++
                                in 10..19 -> ageStats[1]++
                                in 20..29 -> ageStats[2]++
                                in 30..39 -> ageStats[3]++
                                in 40..49 -> ageStats[4]++
                                in 50..59 -> ageStats[5]++
                                in 60..69 -> ageStats[6]++
                                else -> ageStats[7]++
                            }
                            if (agent.willBeInCriticalCondition) {
                                if (agent.willDie) {
                                    when (agent.age) {
                                        in 0..9 -> deathStats[0]++
                                        in 10..19 -> deathStats[1]++
                                        in 20..29 -> deathStats[2]++
                                        in 30..39 -> deathStats[3]++
                                        in 40..49 -> deathStats[4]++
                                        in 50..59 -> deathStats[5]++
                                        in 60..69 -> deathStats[6]++
                                        else -> deathStats[7]++
                                    }
                                }
                                when (agent.age) {
                                    in 0..9 -> criticalStats[0]++
                                    in 10..19 -> criticalStats[1]++
                                    in 20..29 -> criticalStats[2]++
                                    in 30..39 -> criticalStats[3]++
                                    in 40..49 -> criticalStats[4]++
                                    in 50..59 -> criticalStats[5]++
                                    in 60..69 -> criticalStats[6]++
                                    else -> criticalStats[7]++
                                }
                            }
                        }
                        1 -> {
                            if (agent.daysInfected == agent.infectionPeriod) {
                                // Переход в иммунное состояние
                                stats[1]--
                                stats[4]++
                                agent.healthStatus = 2
                            } else {
                                // Самоизоляция
                                if ((agent.daysInfected == agent.isolationPeriod) && (!agent.isAsymptomatic)) {
                                    agent.isIsolated = true
                                }
                                // Выявление больного
                                if (agent.daysInfected == agent.reportPeriod) {
                                    stats[2]++
                                }
                                if ((agent.daysInfected == agent.dayOfDeath) && (agent.willDie)) {
                                    agent.healthStatus = 4
                                    stats[3]++
                                    stats[1]--
                                }
                                // Переход на новый день болезни
                                agent.daysInfected += 1
                                // Обновление влияния силы инфекции
                                agent.findCurrentInfectivityInfluence()
                            }
                        }
                    }
                }
            }

            // Прибавляем к числу общих и активных случаев число новых случаев за день
            stats[0] += stats[2]
            stats[1] += stats[2]

            // Название месяца для UI
            val monthName = when (month) {
                1 -> "Января"
                2 -> "Февраля"
                3 -> "Марта"
                4 -> "Апреля"
                5 -> "Мая"
                6 -> "Июня"
                7 -> "Июля"
                8 -> "Августа"
                9 -> "Сентября"
                10 -> "Октября"
                11 -> "Ноября"
                else -> "Декабря"
            }
            // Обновление UI
            Platform.runLater {
                series1.data.add(XYChart.Data<String, Number>("$day $monthName", stats[0]))
                series2.data.add(XYChart.Data<String, Number>("$day $monthName", stats[1]))
                series3.data.add(XYChart.Data<String, Number>("$day $monthName", stats[2]))
                series4.data.add(XYChart.Data<String, Number>("$day $monthName", stats[3]))

                if (globalDay < realDataDayNumber) {
                    series1Real.data.add(XYChart.Data<String, Number>("$day $monthName", realData[globalDay][6]))
                    series3Real.data.add(XYChart.Data<String, Number>("$day $monthName", realData[globalDay][5]))
                    series4Real.data.add(XYChart.Data<String, Number>("$day $monthName", realData[globalDay][2]))
                }
                dateLabelText.set("$day $monthName")
            }

            // Запись в таблицу результатов на текущем шаге
            writeTableInt(
                "D:\\Dev\\Projects\\KotlinProjects\\TornadoFXCOVID\\src\\output\\results${numOfIter}.xlsx",
                globalDay, stats)

            // Время на обновление UI
            sleep(100)

            // Меняем день
            day += 1
            // Условие прекращения работы симуляции
            if ((month == 5) && (day == 16)) {
                println("-----------Critical-----------")
                println("0-9 Critical: ${criticalStats[0] / ageStats[0].toDouble()}")
                println("10-19 Critical: ${criticalStats[1] / ageStats[1].toDouble()}")
                println("20-29 Critical: ${criticalStats[2] / ageStats[2].toDouble()}")
                println("30-39 Critical: ${criticalStats[3] / ageStats[3].toDouble()}")
                println("40-49 Critical: ${criticalStats[4] / ageStats[4].toDouble()}")
                println("50-59 Critical: ${criticalStats[5] / ageStats[5].toDouble()}")
                println("60-69 Critical: ${criticalStats[6] / ageStats[6].toDouble()}")
                println("70+ Critical: ${criticalStats[7] / ageStats[7].toDouble()}")
                println("------------Death------------")
                println("0-9 Death: ${deathStats[0] / ageStats[0].toDouble()}")
                println("10-19 Death: ${deathStats[1] / ageStats[1].toDouble()}")
                println("20-29 Death: ${deathStats[2] / ageStats[2].toDouble()}")
                println("30-39 Death: ${deathStats[3] / ageStats[3].toDouble()}")
                println("40-49 Death: ${deathStats[4] / ageStats[4].toDouble()}")
                println("50-59 Death: ${deathStats[5] / ageStats[5].toDouble()}")
                println("60-69 Death: ${deathStats[6] / ageStats[6].toDouble()}")
                println("70+ Death: ${deathStats[7] / ageStats[7].toDouble()}")
                break
            }
            // Меняем день недели
            dayOfTheWeek += 1
            if (dayOfTheWeek == 8) {
                dayOfTheWeek = 1
            }
            // Меняем шаг модели
            globalDay += 1

            // Меняем месяц
            if (
                (month in arrayListOf(1, 3, 5, 7, 8, 10) && (day == 32)) ||
                (month in arrayListOf(4, 6, 9, 11) && (day == 31)) ||
                (month == 2) and (day == 29)
            ) {
                day = 1
                month += 1
                println("Month $month")
            } else if ((month == 12) && (day == 32)) {
                day = 1
                month = 1
                println("Month 1")
            }
        }
    }

    // Инициализация
    init {
        println("Init...")
        val districtsAgeSexRatioMatrix = arrayListOf<ArrayList<Double>>()
        readTableDouble("D:\\Dev\\Projects\\KotlinProjects\\TornadoFXCOVID\\src\\tables\\districts_ratio_cumulative.xlsx",
                21, 54, districtsAgeSexRatioMatrix)

        readTableInt("D:\\Dev\\Projects\\KotlinProjects\\TornadoFXCOVID\\src\\tables\\stats.xlsx",
            realDataDayNumber, 7, realData)

        println("Creating households...")
        addHouseholdsToPool(districtsAgeSexRatioMatrix)

        for (i in (1..startingInfectedParameter)) {
            val household = households[(0 until households.size).random()]
            val agent = household.group.agents[(0 until household.group.agents.size).random()]
            agent.healthStatus = 1
            agent.updateHealthParameters()
            when (agent.age) {
                in 0..9 -> ageStats[0]++
                in 10..19 -> ageStats[1]++
                in 20..29 -> ageStats[2]++
                in 30..39 -> ageStats[3]++
                in 40..49 -> ageStats[4]++
                in 50..59 -> ageStats[5]++
                in 60..69 -> ageStats[6]++
                else -> ageStats[7]++
            }
            if (agent.willBeInCriticalCondition) {
                if (agent.willDie) {
                    when (agent.age) {
                        in 0..9 -> deathStats[0]++
                        in 10..19 -> deathStats[1]++
                        in 20..29 -> deathStats[2]++
                        in 30..39 -> deathStats[3]++
                        in 40..49 -> deathStats[4]++
                        in 50..59 -> deathStats[5]++
                        in 60..69 -> deathStats[6]++
                        else -> deathStats[7]++
                    }
                }
                when (agent.age) {
                    in 0..9 -> criticalStats[0]++
                    in 10..19 -> criticalStats[1]++
                    in 20..29 -> criticalStats[2]++
                    in 30..39 -> criticalStats[3]++
                    in 40..49 -> criticalStats[4]++
                    in 50..59 -> criticalStats[5]++
                    in 60..69 -> criticalStats[6]++
                    else -> criticalStats[7]++
                }
            }
        }
    }

    fun restartWorld() {
        day = 15
        month = 3
        dayOfTheWeek = 7
        globalDay = 0

        stats = arrayListOf(0, 0, 0, 0, 0)
        ageStats = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0)
        criticalStats = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0)
        deathStats = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0)

        households.parallelStream().forEach { household ->
            household.group.agents.forEach { agent ->
                agent.healthStatus = 0
            }
        }

        for (i in (1..startingInfectedParameter)) {
            val household = households[(0 until households.size).random()]
            val agent = household.group.agents[(0 until household.group.agents.size).random()]
            agent.healthStatus = 1
            agent.updateHealthParameters()
            when (agent.age) {
                in 0..9 -> ageStats[0]++
                in 10..19 -> ageStats[1]++
                in 20..29 -> ageStats[2]++
                in 30..39 -> ageStats[3]++
                in 40..49 -> ageStats[4]++
                in 50..59 -> ageStats[5]++
                in 60..69 -> ageStats[6]++
                else -> ageStats[7]++
            }
            if (agent.willBeInCriticalCondition) {
                if (agent.willDie) {
                    when (agent.age) {
                        in 0..9 -> deathStats[0]++
                        in 10..19 -> deathStats[1]++
                        in 20..29 -> deathStats[2]++
                        in 30..39 -> deathStats[3]++
                        in 40..49 -> deathStats[4]++
                        in 50..59 -> deathStats[5]++
                        in 60..69 -> deathStats[6]++
                        else -> deathStats[7]++
                    }
                }
                when (agent.age) {
                    in 0..9 -> criticalStats[0]++
                    in 10..19 -> criticalStats[1]++
                    in 20..29 -> criticalStats[2]++
                    in 30..39 -> criticalStats[3]++
                    in 40..49 -> criticalStats[4]++
                    in 50..59 -> criticalStats[5]++
                    in 60..69 -> criticalStats[6]++
                    else -> criticalStats[7]++
                }
            }
        }

        workplace.companies.parallelStream().forEach { company ->
            company.isActiveGroup = true
            company.agents.forEach { agent ->
                agent.isGoingToWork = true
            }
        }
    }
}
