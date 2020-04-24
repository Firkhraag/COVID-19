package application.model

// Домохозяйство
class Household {

    // Группа агентов
    val group = Group()

    // Добавить агента
    fun addAgent(agent: Agent) {
        group.addAgent(agent)
    }

}
