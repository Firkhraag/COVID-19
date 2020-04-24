package application.model

import application.utility.generateBarabasiAlbertNetworkForWork

// Класс, содержащий все рабочие коллективы
class Workplace {
    // Минимальный размер фирмы
    private val minFirmSize = 6
    // Максимальный размер фирмы
    private val maxFirmSize = 2500

    // Создание графа Барабаши-Альберта для последней добавленной компании
    fun generateLastBarabasiAlbertNetwork() {
        if (companies[companies.size - 1].agents.size >= minFirmSize) {
            generateBarabasiAlbertNetworkForWork(
                companies[companies.size - 1], minFirmSize)
        } else {
            // Может быть не заполнена до минимального числа агентов
            generateBarabasiAlbertNetworkForWork(
                companies[companies.size - 1], companies[companies.size - 1].agents.size)
        }
    }

    // Распределение по закону Ципфа
    private val zipfDistribution = org.apache.commons.math3.distribution.ZipfDistribution(maxFirmSize, 1.0)
    // Размер последней добавленной компании
    private var currentGroupSize = zipfDistribution.sample() + (minFirmSize - 1)

    // Массив компаний
    val companies = arrayListOf(Group())

    // Добавить агента
    fun addAgent(agent: Agent) {
        // Компания заполнена агентами
        if (companies[companies.size - 1].agents.size == currentGroupSize) {
            // Создаем граф Барабаши-Альберта
            generateBarabasiAlbertNetworkForWork(companies[companies.size - 1], minFirmSize)
            // Добавляем новую компанию
            companies.add(Group())
            // Обновляем размер последней добавленной компании
            currentGroupSize = zipfDistribution.sample() + (minFirmSize - 1)
        }
        // Иначе просто добавляем агента
        companies[companies.size - 1].addAgent(agent)
    }
}
