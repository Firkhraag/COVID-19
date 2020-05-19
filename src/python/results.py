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

rss1 = 0
for i in range(0, row_number):
    rss1 += (stats.iloc[i, 6] - mean_cases[i]) * (stats.iloc[i, 6] - mean_cases[i])
print("Общее число случаев RSS:", int(rss1))

rss2 = 0
for i in range(0, row_number):
    rss2 += (stats.iloc[i, 5] - mean_new_cases[i]) * (stats.iloc[i, 5] - mean_new_cases[i])
print("Число новых случаев RSS:", int(rss2))

rss3 = 0
for i in range(0, row_number):
    rss3 += (stats.iloc[i, 2] - mean_deaths[i]) * (stats.iloc[i, 2] - mean_deaths[i])
print("Общее число смертей RSS:", int(rss3))


# Общее число случаев RSS: 246959681
# Число новых случаев RSS: 1338702
# Общее число смертей RSS: 8621


plt.plot(np.linspace(1, row_number, row_number), stats.iloc[:, 6], c='r', label="данные x2")
plt.plot(np.linspace(1, row_number, row_number), stats.iloc[:, 0], c='m', label="данные x1")
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
