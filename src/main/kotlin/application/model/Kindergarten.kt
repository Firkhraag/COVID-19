package application.model

// Класс, содержащий все детские сады
class Kindergarten {

    // Количество детей в группах
    private var groupSizes = arrayListOf(10, 15, 15, 20, 20, 20)

    // Группы по годам
    val groupsByAge = arrayListOf(
        arrayListOf<Group>(), arrayListOf(), arrayListOf(),
        arrayListOf(), arrayListOf(), arrayListOf()
    )

//    // Требуется ли взрослый в группу
//    val adultNeeded = arrayListOf(
//        false, false, false,
//        false, false, false
//    )

    // Добавление агента
    fun addAgent(agent: Agent) {
        // Выбор группы по возрасту
        val groupNum = when (agent.age) {
            0 -> 0
            1 -> 1
            2 -> if ((0..1).random() == 0) 1 else 2
            3 -> if ((0..1).random() == 0) 2 else 3
            4 -> if ((0..1).random() == 0) 3 else 4
            5 -> if ((0..1).random() == 0) 4 else 5
            6 -> 5
            else -> error("Wrong age")
        }
        // Добавление группы, если отсутствует
        if (groupsByAge[groupNum].size == 0) {
            groupsByAge[groupNum].add(Group())
//            adultNeeded[groupNum] = true
        }
        // Группа заполнена
        if (groupsByAge[groupNum][groupsByAge[groupNum].size - 1].agents.size == groupSizes[groupNum]) {
            groupsByAge[groupNum].add(Group())
//            adultNeeded[groupNum] = true
        }
        // Добавить агента в последнюю добавленную группу
        groupsByAge[groupNum][groupsByAge[groupNum].size - 1].addAgent(agent)
    }
}