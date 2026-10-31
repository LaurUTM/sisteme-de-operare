# OS Control Center

Proiect comun pentru lucrările de laborator la **Sisteme de operare**.
Un singur program Java (Swing) care adună Lab 1, Lab 2, Lab 4 și Lab 5 și rulează în mașina virtuală Debian.
Lab 3 (administrarea Windows) se prezintă separat, pe Windows.

## Ce fișier ține de ce lucrare

| Fișier | Lucrarea | Ce face |
|---|---|---|
| `TimerPanel.java` | Lab 1 | `Timer` și `TimerTask`: după un interval, la o oră exactă, periodic; `cancel()` |
| `EnvironmentPanel.java` | Lab 2 | Afișează SO, CPU, RAM, disc, dacă e mașină virtuală, BIOS sau UEFI |
| `Scheduler.java` | Lab 4 | Calculele: FCFS, SJF, SRTF, Round Robin, priorități, „cel mai scurt proces urmează”, echitabilă |
| `SchedulerPanel.java`, `GanttChart.java` | Lab 4 | Interfața simulatorului și diagrama Gantt |
| `MonitorPanel.java` | Lab 5 | CPU/RAM la 5 s, jurnal, editor de text, comenzi (`ProcessBuilder`), pornire automată, repornire |
| `OSControlCenter.java` | toate | Fereastra principală cu file |
| `Util.java`, `Theme.java` | toate | Funcții ajutătoare, culori și fonturi |

Legătura dintre lucrări: monitorizarea din Lab 5 este pornită de un `java.util.Timer` cu
`scheduleAtFixedRate`, adică exact mecanismul din Lab 1.

## Pornire în Debian (mașina virtuală)

```bash
sudo apt update
sudo apt install default-jdk git          # Java + Git
git clone https://github.com/<utilizator>/OSControlCenter.git
cd OSControlCenter
chmod +x run.sh
./run.sh
```

Fără script:

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out OSControlCenter
```

Datele tale (nume, grupă) se scriu în `src/OSControlCenter.java`, constantele `STUDENT` și `GROUP`.

## Git: câte o ramură pentru fiecare lucrare (Lab 1)

```bash
git init
git add .gitignore README.md run.sh src/OSControlCenter.java src/Util.java src/Theme.java
git commit -m "Structura proiectului"

git checkout -b lab1-timer
git add src/TimerPanel.java
git commit -m "Lab 1: timere"

git checkout main
git checkout -b lab2-mediu
git add src/EnvironmentPanel.java
git commit -m "Lab 2: informații despre mașina virtuală"

git checkout main
git checkout -b lab4-planificator
git add src/Scheduler.java src/SchedulerPanel.java src/GanttChart.java
git commit -m "Lab 4: algoritmi de planificare"

git checkout main
git checkout -b lab5-monitor
git add src/MonitorPanel.java
git commit -m "Lab 5: monitor sistem"

# unirea tuturor în main
git checkout main
git merge lab1-timer lab2-mediu lab4-planificator lab5-monitor

git remote add origin https://github.com/<utilizator>/OSControlCenter.git
git push -u origin --all
```

## Observații la Lab 4

- Programul afișează două medii: **media pe procese** (suma momentelor de terminare / numărul de procese)
  și **metoda din îndrumar** (media momentelor de sfârșit ale tuturor segmentelor din diagrama Gantt).
  Pentru exemplele din îndrumar rezultatele coincid: FCFS 27, SJF 21, SRTF 23,33, Round Robin 26,2,
  „cel mai scurt proces urmează” 25,14, iar ordinea la planificarea echitabilă este identică.
- La exemplul de planificare prioritară din îndrumar, formula `(4*20+3*12+2*8+4)/4` dă **34**, nu 24
  (în îndrumar e o greșeală de calcul). Programul afișează 34.
- La prioritate, implicit **numărul mai mare = prioritate mai mare** (ca în îndrumar, unde B-8 rulează primul).
  Dacă profesorul consideră că 1 este cea mai mare prioritate, debifează căsuța din program.
