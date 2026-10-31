# OS Control Center

Proiect comun pentru lucrările de laborator la **Sisteme de operare**.
Un singur program Java (Swing) care adună toate cele 5 lucrări și rulează în mașina virtuală Debian
(merge și pe Windows).

## Ce fișier ține de ce lucrare

| Fișier | Lucrarea | Ce face |
|---|---|---|
| `TimerPanel.java` | Lab 1 | `Timer` și `TimerTask`: după un interval, la o oră exactă, periodic; `cancel()` |
| `EnvironmentPanel.java` | Lab 2 | Afișează SO, CPU, RAM, disc, dacă e mașină virtuală, BIOS sau UEFI |
| `AdminPanel.java` | Lab 3 | Utilizatori, grupuri, parole, logări, fișier de pagină/swap, pornire automată |
| `Scheduler.java` | Lab 4 | Calculele: FCFS, SJF, SRTF, Round Robin, priorități, „cel mai scurt proces urmează”, echitabilă |
| `SchedulerPanel.java`, `GanttChart.java` | Lab 4 | Interfața simulatorului și diagrama Gantt |
| `MonitorPanel.java` | Lab 5 | CPU/RAM la 5 s, jurnal, editor de text, comenzi (`ProcessBuilder`), pornire automată, repornire |
| `LiveRunner.java` | **toate împreună** | Execută planul din Lab 4 în timp real, cu procese reale (vezi mai jos) |
| `OSControlCenter.java` | toate | Fereastra principală cu file |
| `Util.java`, `Theme.java` | toate | Funcții ajutătoare, culori și fonturi |

## Cum lucrează împreună: „Rulează în timp real”

Butonul **▶ Rulează în timp real** din fila Lab 4 folosește toate lucrările odată:

1. **Lab 4** – planificatorul calculează ordinea (ex. Round Robin: `A B(7) C D B`).
2. **Lab 1** – un `java.util.Timer` cu `scheduleAtFixedRate` execută planul: la fiecare unitate de timp
   decide ce proces trebuie să ruleze.
3. **Lab 5** – pentru fiecare proces se pornește un **proces real** în Linux (`ProcessBuilder`, un script `sh`
   care ține procesorul ocupat). Procesul care nu are voie să ruleze este oprit cu `kill -STOP`
   (starea **T**), iar cel ales este pornit cu `kill -CONT` (starea **R**) – exact o comutare de context.
   Monitorul citește procesorul la fiecare secundă și arată sub grafic ce proces rula.
4. **Lab 2** – procesele rulează pe nucleele mașinii virtuale: cu 2 nuclee, un proces ocupă ~50 % CPU,
   cu 1 nucleu ~100 %.

În timpul rulării, într-un terminal separat, poți arăta stările proceselor:

```bash
watch -n 1 "ps -o pid,stat,args -C sh | grep proces_"
```

Pe Windows, în loc de procese se folosesc fire de execuție (`Thread`).

## Lab 3 pe Windows și pe Linux

| Sarcina din Lab 3 | Windows | Debian |
|---|---|---|
| Lista utilizatorilor | `net user` | `getent passwd` |
| 1. Utilizator nou, schimbă parola la prima logare | `net user X parola /add /logonpasswordchg:yes` | `useradd -m X`, `chpasswd`, `chage -d 0 X` |
| 2. Fă-l administrator | `net localgroup Administrators X /add` | `usermod -aG sudo X` |
| 2. Șterge utilizatorul | `net user X /delete` | `userdel -r X` |
| 3. Parola expiră la 30 de zile | `net accounts /maxpwage:30` | `chage -M 30 X` |
| 4. Grup nou și membri | `net localgroup G /add`, `net localgroup G X /add` | `groupadd G`, `usermod -aG G X` |
| 6. Logări | Event Viewer, `wevtutil` (eveniment 4624) | `last`, `journalctl` |
| Pornire automată | registrul: `HKCU\...\Run` | `~/.config/autostart` |
| Memorie virtuală | `pagefile.sys` | swap: `swapon --show`, `free -h` |

În Debian, operațiile de administrare cer parola printr-o fereastră (`pkexec`). Dacă lipsește:
`sudo apt install pkexec`. Pe Windows, pornește programul cu „Run as administrator”.

## Pornire în Debian (mașina virtuală)

```bash
sudo apt update
sudo apt install default-jdk git          # Java + Git
git clone -b grupa-5 https://github.com/LaurUTM/sisteme-de-operare.git
cd sisteme-de-operare/Gancearuc_Daniela/OSControlCenter
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
