package application.utility

import application.model.Group

// Создание графа Барабаши-Альберта
// На вход подаются группа с набором агентов (group) и число минимальных связей, которые должен иметь агент (m)
fun generateBarabasiAlbertNetworkForWork(group: Group, m: Int) {
    // Связный граф с m вершинами
    for (i in 0 until m) {
        val agent = group.agents[i]
        for (j in 0 until m) {
            if (i == j) continue
            agent.connectedWorkAgents.add(j)
        }
    }
    // Сумма связей всех вершин
    var degreeSum = m * (m - 1)
    // Добавление новых вершин
    for (i in m until group.agents.size) {
        val agent = group.agents[i]
        var addedCon = 0
        // Новая вершина должна иметь m связей
        while (addedCon < m) {
            for (j in 0 until i) {
                val agent2 = group.agents[j]
                val p = agent2.connectedWorkAgents.size / degreeSum.toDouble()

                val randomNum = (0..9999).random() * 0.0001
                if (randomNum < p) {
                    agent.connectedWorkAgents.add(j)
                    agent2.connectedWorkAgents.add(i)
                    addedCon += 1
                    if (addedCon == m) {
                        break
                    }
                }
            }
        }
        degreeSum += m
    }
}
