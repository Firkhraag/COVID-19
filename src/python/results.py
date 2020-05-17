import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

number_of_people = 5383890

num_of_iterations = 10.0

df1 = pd.read_excel(r'..\output\results1.xlsx', header=None)
df2 = pd.read_excel(r'..\output\results2.xlsx', header=None)
df3 = pd.read_excel(r'..\output\results3.xlsx', header=None)
df4 = pd.read_excel(r'..\output\results4.xlsx', header=None)
df5 = pd.read_excel(r'..\output\results5.xlsx', header=None)
df6 = pd.read_excel(r'..\output\results6.xlsx', header=None)
df7 = pd.read_excel(r'..\output\results7.xlsx', header=None)
df8 = pd.read_excel(r'..\output\results8.xlsx', header=None)
df9 = pd.read_excel(r'..\output\results9.xlsx', header=None)
df10 = pd.read_excel(r'..\output\results10.xlsx', header=None)

stats = pd.read_excel(r'..\tables\stats.xlsx', index_col=0)

row_number = len(stats)

mean_cases = (np.array(df1.iloc[:row_number, 0]) + np.array(df2.iloc[:row_number, 0]) + np.array(df3.iloc[:row_number, 0]) +
              np.array(df4.iloc[:row_number, 0]) + np.array(df5.iloc[:row_number, 0]) + np.array(df6.iloc[:row_number, 0]) +
              np.array(df7.iloc[:row_number, 0]) + np.array(df8.iloc[:row_number, 0]) + np.array(df9.iloc[:row_number, 0]) +
              np.array(df10.iloc[:row_number, 0])) / num_of_iterations

mean_new_cases = (np.array(df1.iloc[:row_number, 2]) + np.array(df2.iloc[:row_number, 2]) + np.array(df3.iloc[:row_number, 2]) +
                  np.array(df4.iloc[:row_number, 2]) + np.array(df5.iloc[:row_number, 2]) + np.array(df6.iloc[:row_number, 2]) +
                  np.array(df7.iloc[:row_number, 2]) + np.array(df8.iloc[:row_number, 2]) + np.array(df9.iloc[:row_number, 2]) +
                  np.array(df10.iloc[:row_number, 2])) / num_of_iterations

mean_deaths = (np.array(df1.iloc[:row_number, 3]) + np.array(df2.iloc[:row_number, 3]) + np.array(df3.iloc[:row_number, 3]) +
               np.array(df4.iloc[:row_number, 3]) + np.array(df5.iloc[:row_number, 3]) + np.array(df6.iloc[:row_number, 3]) +
               np.array(df7.iloc[:row_number, 3]) + np.array(df8.iloc[:row_number, 3]) + np.array(df9.iloc[:row_number, 3]) +
               np.array(df10.iloc[:row_number, 3])) / num_of_iterations


rrss1 = 0
for i in range(0, row_number):
    if mean_cases[i] < 1:
        continue
    rrss1 += (((stats.iloc[i, 6] - mean_cases[i]) / stats.iloc[i, 6]) * (
            (stats.iloc[i, 6] - mean_cases[i]) / stats.iloc[i, 6]) +
             ((stats.iloc[i, 6] - mean_cases[i]) / mean_cases[i]) * (
                         (stats.iloc[i, 6] - mean_cases[i]) / mean_cases[i]))
print("Общее число случаев RRSS:", rrss1)

rrss2 = 0
for i in range(0, row_number):
    if mean_new_cases[i] < 1:
        continue
    if stats.iloc[i, 5] < 1:
        continue
    rrss2 += (((stats.iloc[i, 5] - mean_new_cases[i]) / stats.iloc[i, 5]) * (
            (stats.iloc[i, 5] - mean_new_cases[i]) / stats.iloc[i, 5]) +
             ((stats.iloc[i, 5] - mean_new_cases[i]) / mean_new_cases[i]) * (
                         (stats.iloc[i, 5] - mean_new_cases[i]) / mean_new_cases[i]))
print("Число новых случаев RRSS:", rrss2)

rrss3 = 0
for i in range(0, row_number):
    if mean_deaths[i] < 1:
        continue
    if stats.iloc[i, 2] < 1:
        continue
    rrss3 += (((stats.iloc[i, 2] - mean_deaths[i]) / stats.iloc[i, 2]) * (
            (stats.iloc[i, 2] - mean_deaths[i]) / stats.iloc[i, 2]) +
             ((stats.iloc[i, 2] - mean_deaths[i]) / mean_deaths[i]) * (
                         (stats.iloc[i, 2] - mean_deaths[i]) / mean_deaths[i]))
print("Общее число смертей RRSS:", rrss3)


# Общее число случаев RRSS: 116.08622772161874
# Число новых случаев RRSS: 124.53392592418385
# Общее число смертей RRSS: 11.859157047198442


plt.plot(np.linspace(1, row_number, row_number), stats.iloc[:, 6], c='r', label="данные")
plt.plot(np.linspace(1, row_number, row_number), mean_cases, c='b', label="модель")
plt.title("Общее число случаев")
plt.xlabel("День")
plt.ylabel("Человек")
plt.legend()


plt.figure()
plt.plot(np.linspace(1, row_number, row_number), stats.iloc[:, 5], c='r', label="данные")
plt.plot(np.linspace(1, row_number, row_number), mean_new_cases, c='b', label="модель")
plt.title("Число новых случаев")
plt.xlabel("День")
plt.ylabel("Человек")
plt.legend()


plt.figure()
plt.plot(np.linspace(1, row_number, row_number), stats.iloc[:, 2], c='r', label="данные")
plt.plot(np.linspace(1, row_number, row_number), mean_deaths, c='b', label="модель")
plt.title("Общее число смертей")
plt.xlabel("День")
plt.ylabel("Человек")
plt.legend()


plt.show()

# plt.figure()
# plt.plot(np.linspace(1, 52, 52), res1_15[:52], label="модель")
# plt.plot(np.linspace(1, 52, 52), res2_15[:52])
# plt.plot(np.linspace(1, 52, 52), res3_15[:52])
# plt.plot(np.linspace(1, 52, 52), res4_15[:52])
# plt.plot(np.linspace(1, 52, 52), res5_15[:52])
# plt.plot(np.linspace(1, 52, 52), res6_15[:52])
# plt.plot(np.linspace(1, 52, 52), res7_15[:52])
# plt.plot(np.linspace(1, 52, 52), res8_15[:52])
# plt.plot(np.linspace(1, 52, 52), res9_15[:52])
# plt.plot(np.linspace(1, 52, 52), res10_15[:52])
# plt.plot(np.linspace(1, 52, 52), mean_flu, c='r', label="данные")
# plt.plot(np.linspace(1, 52, 52), mean_res15_recorded[:52], c='b', label="модель")
# plt.title("Регистрируемая заболеваемость для возрастной группы 15+")
# plt.xlabel("Месяц")
# plt.ylabel("Количество случаев на 1000 чел.")
# plt.xticks(np.linspace(1, 52, 13), ('Авг', 'Сен', 'Окт', 'Ноя', 'Дек', 'Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг'))
# plt.legend()
# plt.show()
#
# diff4 = 0
# for week in range(1, 53):
#     diff4 += (mean_flu[week] - mean_res15_recorded[week - 1]) * (mean_flu[week] - mean_res15_recorded[week - 1])
# # for week in range(1, 53):
# #     diff4 += (mean_flu[week] - np.array(res1_15[:52])[week - 1]) * (mean_flu[week] - np.array(res1_15[:52])[week - 1])
#
# print("Error15: ", diff4)
#
#
# print("Sum of errors: ", diff1 + diff2 + diff3 + diff4)
# print("Global error: ", diff)
