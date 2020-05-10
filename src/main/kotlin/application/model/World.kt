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

    // Общее число случаев, Число активных больных, Число новых случаев, Число смертей
    // Зарегистрированные
    private var stats = arrayListOf(0, 0, 0, 0, 0)
    // Данные по заболеваемости, выздоровлению, смертности
    private val realData = arrayListOf(arrayListOf<Int>())

//    val infectedNumbers = arrayListOf(125, 147, 156, 171, 191, 226, 295, 329, 373, 408, 488, 557,
//        678, 799, 929, 1083, 98)
//    val infectedNumbersMarch = arrayListOf(5, 8, 9, 9, 10, 14, 16, 16, 16, 21, 21, 26, 26, 37, 42, 50, 98)

    // Рабочие коллективы
    private val workplace = Workplace()
    // Домохозяйства
    private val households = arrayListOf<Household>()

    // Данные по переписи 2010 года
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
    private fun getFullHouseholdContactDuration(): Double {
        // Normal distribution (mean = 12.4, SD = 5.13)
        val rand = java.util.Random()
        return min(24.0, max(0.0,12.4 + rand.nextGaussian() * 5.13))
    }

    private fun getHouseholdContactDuration(): Double {
        // Normal distribution (mean = 8.0, SD = 3.28)
        val rand = java.util.Random()
//        return min(24.0, max(0.0,12.4 + rand.nextGaussian() * 5.13))
        return min(24.0, max(0.0,8.0 + rand.nextGaussian() * 3.28))
    }

    // Продолжительности контактов на работе
    private fun getWorkplaceContactDuration(): Double {
        // Exponential distribution (mean = 3.0, SD = 3.0)
        val erlangDistribution = org.apache.commons.math3.distribution.GammaDistribution(1.0, 3.0)
        return min(8.0, erlangDistribution.sample())
    }

    // Создание агента
    private fun createAgent(indexForAll: Int, indexForMales: Int,
                            districtsAgeSexRatioMatrix: ArrayList<ArrayList<Double>>, isChild: Boolean): Agent {
        var ageRandomNum = (0..999).random() * 0.001
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
            // Добавление в рабочий коллектив
            if (agent.hasWork) {
                workplace.addAgent(agent)
            }
//            if (agent.isGoingToWork) {
//                workplace.addAgent(agent)
//            }
            if (agent.healthStatus == 1) {
                agent.updateHealthParameters(0.05)
            }
            // Добавление в домохозяйство
            household.addAgent(agent)
        }
        // Добавление нового домохозяйства в массив домохозяйств
        households.add(household)
    }

    // Задача создания домохозяйств с параллельным исполнением
    private suspend fun processAll(
        districtsAgeSexRatioMatrix: ArrayList<ArrayList<Double>>) = withContext(Dispatchers.IO) {

        // Число людей по районам города
        val numOfPeopleInDistricts = arrayListOf(161911, 208713, 518709, 533597, 336248, 191847, 357498, 397609,
            44321, 78131, 354525, 527861, 131356, 143154, 568516, 217983, 394972, 216939)

        var counter = AtomicInteger()
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
        workplace.generateLastBarabasiAlbertNetworks()
        println("World creation has ended")
    }

    // Симуляция
    fun runSimulation(numOfIter: Int, durationBias: Double, series1: XYChart.Series<String, Number>,
                      series2: XYChart.Series<String, Number>, series3: XYChart.Series<String, Number>,
                      series4: XYChart.Series<String, Number>, series1Real: XYChart.Series<String, Number>,
                      series2Real: XYChart.Series<String, Number>, series3Real: XYChart.Series<String, Number>,
                      series4Real: XYChart.Series<String, Number>, dateLabelText: SimpleStringProperty) {

        // Цикл симуляции
        while(true) {

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
            if ((dayOfTheWeek == 7) || (dayOfTheWeek == 6)) {
                isHoliday = true
            }
            if ((month == 1) && (day in arrayListOf(1, 2, 3, 7))) {
                isHoliday = true
            }
            if ((month == 5) && (day in arrayListOf(1, 9))) {
                isHoliday = true
            }
            if ((month == 2) && (day == 23)) {
                isHoliday = true
            }
            if ((month == 3) && (day == 8)) {
                isHoliday = true
            }
            if ((month == 6) && (day == 12)) {
                isHoliday = true
            }

            if (isHoliday) {
                // Контакты в домохозяйстве на выходных
                households.parallelStream().forEach { household ->
                    household.group.agents.forEach { agent ->
                        if (agent.healthStatus == 1) {
                            household.group.agents.forEach { agent2 ->
                                if (agent2.healthStatus == 0) {
                                    val randomNum = (0..9999).random() * 0.0001
                                    val probabilityOfInfection = agent.infectivityInfluence *
                                            agent.susceptibilityInfluence
//                                    println("Holiday")
//                                    println(probabilityOfInfection)
//                                    readLine()
                                    if (randomNum < probabilityOfInfection) {
                                        agent2.healthStatus = 3
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Контакты в домохозяйстве
                households.parallelStream().forEach { household ->
                    household.group.agents.forEach { agent ->
                        if (agent.healthStatus == 1) {
                            household.group.agents.forEach { agent2 ->
                                if (agent2.healthStatus == 0) {
                                    if ((agent.isGoingToWork && !agent.isIsolated) ||
                                            (agent2.isGoingToWork && !agent2.isIsolated)) {
                                        // Контакт в домохозяйстве с работающим агентом
                                        val randomNum = (0..9999).random() * 0.0001
                                        val durationCoefficient = 1 / (1 + exp(
                                            -getHouseholdContactDuration() + durationBias))
                                        val probabilityOfInfection = agent.infectivityInfluence *
                                                agent.susceptibilityInfluence * durationCoefficient
//                                        println("Household + work")
//                                        println(probabilityOfInfection)
//                                        readLine()
                                        if (randomNum < probabilityOfInfection) {
                                            agent2.healthStatus = 3
                                        }
                                    } else {
                                        // Контакт в домохозяйстве между сидящими дома агентами
                                        val randomNum = (0..9999).random() * 0.0001
                                        val probabilityOfInfection = agent.infectivityInfluence *
                                                agent.susceptibilityInfluence
//                                        println("Household")
//                                        println(agent.susceptibilityInfluence)
//                                        readLine()
                                        if (randomNum < probabilityOfInfection) {
                                            agent2.healthStatus = 3
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Контакты в рабочем коллективе
                workplace.companies.parallelStream().forEach { company ->
                    if (company.isActiveGroup) {
                        company.agents.forEach { agent ->
                            if ((agent.healthStatus == 1) && (!agent.isIsolated))  {
                                company.agents.forEachIndexed { index, agent2 ->
                                    if ((agent2.healthStatus == 0) &&
                                        (index in agent.connectedWorkAgents)) {

                                        val durationCoefficient = 1 / (1 + exp(
                                            -getWorkplaceContactDuration() + durationBias))
                                        val randNum = (0..9999).random() * 0.0001
                                        val probabilityOfInfection = agent.infectivityInfluence *
                                                agent.susceptibilityInfluence * durationCoefficient
//                                    println("Work")
//                                    println(probabilityOfInfection)
//                                    readLine()
                                        if (randNum < probabilityOfInfection) {
                                            agent2.healthStatus = 3
                                        }

//                                    if ((globalDay > 13) && ((!agent.isGoingToWork) || (!agent2.isGoingToWork))) {
//
//                                    } else {
//                                        val durationCoefficient = 1 / (1 + exp(
//                                            -getWorkplaceContactDuration() + durationBias))
//                                        val randNum = (0..9999).random() * 0.0001
//                                        val probabilityOfInfection = agent.infectivityInfluence *
//                                                agent.susceptibilityInfluence * durationCoefficient
////                                    println("Work")
////                                    println(probabilityOfInfection)
////                                    readLine()
//                                        if (randNum < probabilityOfInfection) {
//                                            agent2.healthStatus = 3
//                                        }
//                                    }

//                                    val durationCoefficient = 1 / (1 + exp(
//                                        -getWorkplaceContactDuration() + durationBias))
//                                    val randNum = (0..9999).random() * 0.0001
//                                    val probabilityOfInfection = agent.infectivityInfluence *
//                                            agent.susceptibilityInfluence * durationCoefficient
////                                    println("Work")
////                                    println(probabilityOfInfection)
////                                    readLine()
//                                    if (randNum < probabilityOfInfection) {
//                                        agent2.healthStatus = 3
//                                    }
                                    }
                                }
                            }
                        }
                    }
//                    company.agents.forEach { agent ->
//                        if ((agent.healthStatus == 1) && (!agent.isIsolated))  {
//                            company.agents.forEachIndexed { index, agent2 ->
//                                if ((agent2.healthStatus == 0) &&
//                                    (index in agent.connectedWorkAgents)) {
//
//                                    val durationCoefficient = 1 / (1 + exp(
//                                        -getWorkplaceContactDuration() + durationBias))
//                                    val randNum = (0..9999).random() * 0.0001
//                                    val probabilityOfInfection = agent.infectivityInfluence *
//                                            agent.susceptibilityInfluence * durationCoefficient
////                                    println("Work")
////                                    println(probabilityOfInfection)
////                                    readLine()
//                                    if (randNum < probabilityOfInfection) {
//                                        agent2.healthStatus = 3
//                                    }
//
////                                    if ((globalDay > 13) && ((!agent.isGoingToWork) || (!agent2.isGoingToWork))) {
////
////                                    } else {
////                                        val durationCoefficient = 1 / (1 + exp(
////                                            -getWorkplaceContactDuration() + durationBias))
////                                        val randNum = (0..9999).random() * 0.0001
////                                        val probabilityOfInfection = agent.infectivityInfluence *
////                                                agent.susceptibilityInfluence * durationCoefficient
//////                                    println("Work")
//////                                    println(probabilityOfInfection)
//////                                    readLine()
////                                        if (randNum < probabilityOfInfection) {
////                                            agent2.healthStatus = 3
////                                        }
////                                    }
//
////                                    val durationCoefficient = 1 / (1 + exp(
////                                        -getWorkplaceContactDuration() + durationBias))
////                                    val randNum = (0..9999).random() * 0.0001
////                                    val probabilityOfInfection = agent.infectivityInfluence *
////                                            agent.susceptibilityInfluence * durationCoefficient
//////                                    println("Work")
//////                                    println(probabilityOfInfection)
//////                                    readLine()
////                                    if (randNum < probabilityOfInfection) {
////                                        agent2.healthStatus = 3
////                                    }
//                                }
//                            }
//                        }
//                    }
                }
            }

            households.forEach { household ->
                household.group.agents.forEach { agent ->
                    when (agent.healthStatus) {
                        // Переход в инфицированное состояние
                        3 -> {
                            agent.healthStatus = 1
                            agent.updateHealthParameters(0.05)
                        }
                        1 -> {
                            if (agent.daysInfected == agent.infectionPeriod) {
//                                println("Global step: ${globalDay}")
//                                println("Days infected: ${agent.daysInfected}")
//                                println("Infection period: ${agent.infectionPeriod}")
//                                readLine()
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
//                                if ((agent.daysInfected == agent.reportPeriod) &&
//                                    (!agent.isAsymptomatic) &&
//                                    (!agent.isReported)) {
//                                    stats[2]++
//                                    agent.isReported = true
//                                }
//                                // Точка максимальной тяжести
//                                if ((agent.daysInfected == agent.changePoint) && (!agent.isAsymptomatic)) {
//                                    // Возможность перейти в тяжелую форму
//                                    agent.chanceToBeInCriticalCondition()
//                                    if (agent.isInCriticalCondition) {
//                                        agent.isIsolated = true
//                                        if (!agent.isReported) {
//                                            stats[2]++
//                                            agent.isReported = true
//                                        }
//                                    }
//                                } else if (agent.isInCriticalCondition) {
//                                    // Возможность умереть
//                                    if (agent.willDie()) {
//                                        agent.healthStatus = 4
//                                        stats[3]++
//                                        stats[1]--
//                                    } else {
//                                        // Выходит из тяжелой формы, если число дней проведенных в состоянии болезни
//                                        // равно 0.75 * продолжительность периода болезни
//                                        if (agent.daysInfected == (0.75 * agent.shouldBeInfected).roundToInt()) {
//                                            agent.isInCriticalCondition = false
//                                        }
//                                    }
//                                }
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

                if (globalDay < 46) {
                    series1Real.data.add(XYChart.Data<String, Number>("$day $monthName", realData[globalDay][6]))
//                    series2Real.data.add(XYChart.Data<String, Number>(globalDay.toString(), realData[globalDay][3]))
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
//            if ((month == 8) && (day == 32)) {
//                break
//            }
            if ((month == 7) && (day == 2)) {
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
            if ((month in arrayListOf(1, 3, 5, 7, 8, 10) && (day == 32)) ||
                    (month in arrayListOf(4, 6, 9, 11) && (day == 31)) ||
                    (month == 2) and (day == 29)) {
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
            46, 7, realData)

        println("Creating households...")
        addHouseholdsToPool(districtsAgeSexRatioMatrix)
    }

    fun restartWorld() {
        day = 15
        month = 3
        dayOfTheWeek = 4
        globalDay = 0

//        households.parallelStream().forEach { household ->
//            household.agents.forEach { agent ->
//                agent.healthStatus = if ((0..9999).random() == 0) 1 else 0
//
////                agent.isAsymptomatic = agent.willBeAsymptomatic()
//                agent.incubationPeriod = agent.willHaveIncubationPeriod()
//                agent.shouldBeInfected = agent.willBeInfectedPeriod()
//                agent.daysInfected = if (agent.healthStatus == 1)
//                    ((1 - agent.incubationPeriod)..agent.shouldBeInfected).random() else 0
//            }
//        }
    }
}
