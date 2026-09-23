[1mdiff --git a/interval_task.py b/interval_task.py[m
[1mindex 64c9071..55ec8f8 100644[m
[1m--- a/interval_task.py[m
[1m+++ b/interval_task.py[m
[36m@@ -2,22 +2,29 @@[m
 IntervalTask - reactioneaza la un anumit INTERVAL de timp (delay).[m
 Echivalentul lui Timer.schedule(task, delay) din Java: se executa[m
 o singura data, dupa ce trece intervalul specificat.[m
[32m+[m
[32m+[m[32mPrimeste un callback (functie) care este apelat cand timer-ul expira,[m
[32m+[m[32mastfel incat interfata grafica sa poata fi actualizata.[m
 """[m
 [m
 import threading[m
 [m
 [m
 class IntervalTask:[m
[31m-    def __init__(self, delay_seconds, mesaj="Au trecut intervalul specificat!"):[m
[32m+[m[32m    def __init__(self, delay_seconds, callback, mesaj="Au trecut intervalul specificat!"):[m
         self.delay_seconds = delay_seconds[m
[32m+[m[32m        self.callback = callback[m
         self.mesaj = mesaj[m
         self._timer = None[m
 [m
     def run(self):[m
         print(f"[IntervalTask] {self.mesaj}")[m
[32m+[m[32m        if self.callback:[m
[32m+[m[32m            self.callback(self.mesaj)[m
 [m
     def start(self):[m
         self._timer = threading.Timer(self.delay_seconds, self.run)[m
[32m+[m[32m        self._timer.daemon = True[m
         self._timer.start()[m
         return self._timer[m
 [m
[36m@@ -28,5 +35,5 @@[m [mclass IntervalTask:[m
 [m
 if __name__ == "__main__":[m
     print("Start IntervalTask - se va executa peste 3 secunde")[m
[31m-    task = IntervalTask(3, "Au trecut 3 secunde de la start!")[m
[31m-    task.start()[m
[32m+[m[32m    task = IntervalTask(3, None, "Au trecut 3 secunde de la start!")[m
[32m+[m[32m    task.start()[m
\ No newline at end of file[m
[1mdiff --git a/period_task.py b/period_task.py[m
[1mindex bd0b16b..d58864c 100644[m
[1m--- a/period_task.py[m
[1m+++ b/period_task.py[m
[36m@@ -2,27 +2,36 @@[m
 PeriodTask - reactioneaza REPETAT, cu o PERIOADA indicata intre executii.[m
 Echivalentul lui Timer.scheduleAtFixedRate(task, delay, period) din Java:[m
 se re-planifica singur dupa fiecare executie, la fiecare "period" secunde.[m
[32m+[m
[32m+[m[32mPrimeste un callback (functie) care este apelat la fiecare executie,[m
[32m+[m[32mastfel incat interfata grafica sa poata fi actualizata (ex: incrementarea[m
[32m+[m[32munui contor pe un buton).[m
 """[m
 [m
 import threading[m
 [m
 [m
 class PeriodTask:[m
[31m-    def __init__(self, period_seconds, mesaj="Beep!"):[m
[32m+[m[32m    def __init__(self, period_seconds, callback, mesaj="Sound played"):[m
         self.period_seconds = period_seconds[m
[32m+[m[32m        self.callback = callback[m
         self.mesaj = mesaj[m
         self._timer = None[m
         self._activ = False[m
 [m
     def run(self):[m
         print(f"[PeriodTask] {self.mesaj}")[m
[32m+[m[32m        if self.callback:[m
[32m+[m[32m            self.callback(self.mesaj)[m
         if self._activ:[m
             self._timer = threading.Timer(self.period_seconds, self.run)[m
[32m+[m[32m            self._timer.daemon = True[m
             self._timer.start()[m
 [m
     def start(self):[m
         self._activ = True[m
         self._timer = threading.Timer(self.period_seconds, self.run)[m
[32m+[m[32m        self._timer.daemon = True[m
         self._timer.start()[m
         return self._timer[m
 [m
[36m@@ -34,5 +43,5 @@[m [mclass PeriodTask:[m
 [m
 if __name__ == "__main__":[m
     print("Start PeriodTask - se repeta la fiecare 2 secunde")[m
[31m-    task = PeriodTask(2, "Sound played")[m
[31m-    task.start()[m
[32m+[m[32m    task = PeriodTask(2, None, "Sound played")[m
[32m+[m[32m    task.start()[m
\ No newline at end of file[m
