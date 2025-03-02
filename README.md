## Запуск программ

### **1. Запуск программы**

#### Команда для запуска:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -cp ".\target\classes" lebibop.lab2.task1
```

### **2. Запуск программы `task2`**

#### Команда для запуска:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dstart=100 -Dend=2000 -cp ".\target\classes" lebibop.lab2.task2
```

### **3. Запуск программы `Lab3`**

#### Команда для запуска:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Drow=5 -Dcol=6 -cp ".\target\classes" lebibop.lab3.Lab3
```

### **4. Запуск программы `Lab4`**

#### Команда для запуска `Task1`:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dstart=100 -Dend=500 -cp ".\target\classes" lebibop.lab4.Task1
```

#### Команда для запуска `Task2`:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Drow=5 -Dcol=6 -cp ".\target\classes" lebibop.lab4.Task2
```

### Описание команды:
- `java -jar ".\mpj-v0_44\lib\starter.jar"`: **Запуск MPJ Express**.

- `-np 4`: **Указывает количество процессов** (в данном случае 4).

- `-cp ".\target\classes"`: **Указывает путь к скомпилированным классам программы**.

- `lebibop.lab2.task1`: **Указывает программу, которую хотите запустить**.

- `-Drow=5 -Dcol=6`: **Указывает размерность матрицы**.

- `-Dstart=100 -Dend=2000`: **Указывает старт и конец интервала**.
