package application.model

// Группа агентов
class Group {

    // Массив агентов
    val agents = arrayListOf<Agent>()

    // Для рабочих групп
    var isActiveGroup = true

    // Добавить агента
    fun addAgent(agent: Agent) {
        agents.add(agent)
    }
}
