package application.model

// Группа агентов
class Group {

    // Массив агентов
    val agents = arrayListOf<Agent>()

    // Добавить агента
    fun addAgent(agent: Agent) {
        agents.add(agent)
    }
}
