"""
RepeatingExactTask - porneste la un TIMP EXACT si apoi se REPETA periodic.
Echivalentul lui Timer.scheduleAtFixedRate(task, date, period) din Java:
combina cerinta 2 (timp exact) cu cerinta 3 (perioada indicata) - prima
executie are loc la ora specificata, iar apoi se repeta la fiecare
"period" secunde, pana este oprit.

Primeste un callback (functie) apelat la fiecare executie, pentru
actualizarea interfetei grafice.
"""

import threading
from datetime import datetime, timedelta


class RepeatingExactTask:
    def __init__(self, ora, minut, period_seconds, callback, secunda=0, mesaj="Reamintire!"):
        self.ora = ora
        self.minut = minut
        self.secunda = secunda
        self.period_seconds = period_seconds
        self.callback = callback
        self.mesaj = mesaj
        self._timer = None
        self._activ = False

    def run(self):
        print(f"[RepeatingExactTask] {self.mesaj}")
        if self.callback:
            self.callback(self.mesaj)
        if self._activ:
            self._timer = threading.Timer(self.period_seconds, self.run)
            self._timer.daemon = True
            self._timer.start()

    def _secunde_pana_la_tinta(self):
        acum = datetime.now()
        tinta = acum.replace(hour=self.ora, minute=self.minut,
                              second=self.secunda, microsecond=0)
        if tinta <= acum:
            tinta += timedelta(days=1)
        return (tinta - acum).total_seconds()

    def start(self):
        self._activ = True
        delay = self._secunde_pana_la_tinta()
        self._timer = threading.Timer(delay, self.run)
        self._timer.daemon = True
        self._timer.start()
        return self._timer

    def cancel(self):
        self._activ = False
        if self._timer:
            self._timer.cancel()


if __name__ == "__main__":
    print("Start RepeatingExactTask - porneste la ora 18:24, apoi se repeta la 5 secunde")
    task = RepeatingExactTask(10, 14, 5, None, 0, "Reamintire periodica!")
    task.start()
