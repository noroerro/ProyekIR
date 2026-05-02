import pandas as pd

# Baca File
data = pd.read_csv('test.csv')

# Tampilkan Data
print(data.head())

for index, row in data.iterrows():
    with open(f"{index}.txt", "w") as f:
        f.write(row["article"])
    if index == 100:
        break;