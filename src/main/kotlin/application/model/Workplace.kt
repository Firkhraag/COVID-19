package application.model

import application.utility.generateBarabasiAlbertNetworkForWork

// Класс, содержащий все рабочие коллективы
class Workplace {
    // Минимальный размер компании
    private val minFirmSize = 6
    // Максимальный размер компании
    private val maxFirmSize = 2500 - minFirmSize

    // Индексы не полностью заполненных компаний
    private val indexesOfWorkplacesToFill = arrayListOf<Int>()

    // Создание графа Барабаши-Альберта для последних добавленных компаний
    fun generateLastBarabasiAlbertNetworks() {
        for (i in indexesOfWorkplacesToFill) {
            if (companies[i].agents.size == 0) {
                // Агентов нет
                continue
            }
            if (companies[i].agents.size >= minFirmSize) {
                generateBarabasiAlbertNetworkForWork(companies[i], minFirmSize)
            } else {
                // Число агентов меньше, чем минимальный размер компании
                generateBarabasiAlbertNetworkForWork(companies[i], companies[i].agents.size)
            }
        }
    }

    // Распределение по закону Ципфа
    private val zipfDistribution = org.apache.commons.math3.distribution.ZipfDistribution(maxFirmSize, 1.059)
    // Размеры компаний
    private val groupSizes = arrayListOf<Int>()
    // Массив компаний
    val companies = arrayListOf<Group>()

    // По сколько компаний добавляем
    private val batchSize = 100

    // Добавить агента
    fun addAgent(agent: Agent) {
        // Начальный batch компаний
        if (companies.size == 0) {
            // Добавление новых компаний
            for (i in (0 until batchSize)) {
                companies.add(Group())
                indexesOfWorkplacesToFill.add(i)
                groupSizes.add(zipfDistribution.sample() + (minFirmSize - 1))
            }
        }
        // Выбираем случайную компанию
        val randomWorkplaceIndex = indexesOfWorkplacesToFill.random()
        // Добавляем агента
        companies[randomWorkplaceIndex].addAgent(agent)
        // Если заполнили
        if (companies[randomWorkplaceIndex].agents.size == groupSizes[randomWorkplaceIndex]) {
            generateBarabasiAlbertNetworkForWork(companies[randomWorkplaceIndex], minFirmSize)
            // Убираем из массива компаний, нуждающихся в добавлении агентов
            indexesOfWorkplacesToFill.remove(randomWorkplaceIndex)
            // Добавляем новую компанию
            companies.add(Group())
            indexesOfWorkplacesToFill.add(companies.size - 1)
            groupSizes.add(zipfDistribution.sample() + (minFirmSize - 1))
        }
    }
}
