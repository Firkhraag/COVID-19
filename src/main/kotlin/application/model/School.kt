package application.model

// Класс, содержащий все школы
class School {

    // Количество агентов в школьном классе
    private val schoolClassSize = 25

    // Классы по годам
    val groupsByAge = arrayListOf(
        arrayListOf<Group>(), arrayListOf(), arrayListOf(),
        arrayListOf(), arrayListOf(), arrayListOf(),
        arrayListOf(), arrayListOf(), arrayListOf(),
        arrayListOf(), arrayListOf()
    )

//    // Требуется ли взрослый в класс
//    val adultNeeded = arrayListOf(
//        false, false, false,
//        false, false, false,
//        false, false, false,
//        false, false
//    )

    // Добавление агента
    fun addAgent(agent: Agent) {
        // Выбор класса по возрасту
        val groupNum = when (agent.age) {
            7 -> 0
            8 -> if ((0..1).random() == 0) 0 else 1
            9 -> if ((0..1).random() == 0) 1 else 2
            10 -> if ((0..1).random() == 0) 2 else 3
            11 -> if ((0..1).random() == 0) 3 else 4
            12 -> if ((0..1).random() == 0) 4 else 5
            13 -> if ((0..1).random() == 0) 5 else 6
            14 -> if ((0..1).random() == 0) 6 else 7
            15 -> if ((0..1).random() == 0) 7 else 8
            16 -> if ((0..1).random() == 0) 8 else 9
            17 -> if ((0..1).random() == 0) 9 else 10
            18 -> 10
            else -> error("Wrong age")
        }
        // Добавление класса, если отсутствует
        if (groupsByAge[groupNum].size == 0) {
            groupsByAge[groupNum].add(Group())
//            adultNeeded[groupNum] = true
        }
        // Класс заполнен
        if (groupsByAge[groupNum][groupsByAge[groupNum].size - 1].agents.size == schoolClassSize) {
            groupsByAge[groupNum].add(Group())
//            adultNeeded[groupNum] = true
        }
        // Добавить агента в последний добавленный класс
        groupsByAge[groupNum][groupsByAge[groupNum].size - 1].addAgent(agent)
    }
}