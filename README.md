## Запуск программ

### **1. Запуск программы**

#### Команда для запуска:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -cp ".\target\classes" lebibop.lab2.task1
```

#### Описание команды:
- `java -jar ".\mpj-v0_44\lib\starter.jar"`: **Запуск MPJ Express**.

- `-np 4`: **Указывает количество процессов** (в данном случае 4).

- `-cp ".\target\classes"`: **Указывает путь к скомпилированным классам программы**.

- `lebibop.lab2.task1`: **Указывает программу, которую хотите запустить**.  

### **2. Запуск программы `task2`**

#### Команда для запуска:
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dstart=100 -Dend=2000 -cp ".\target\classes" lebibop.lab2.task2
```

#### Описание команды:
- `-Dstart=100 -Dend=2000`: **Указывает старт и конец интервала**.
