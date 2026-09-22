"""
ExactTimeTask - reactioneaza la un TIMP EXACT (data/ora specificata).
Echivalentul lui Timer.schedule(task, date) din Java: foloseste
un obiect datetime pentru a calcula cate secunde raman pana la
momentul tinta, apoi porneste un threading.Timer cu acea intarziere.

Primeste un callback (functie) care este apelat cand timer-ul expira,
astfel incat interfata grafica sa poata fi actualizata.
"""

import threading
from datetime import datetime, timedelta


class ExactTimeTask:
    def __init__(self, ora, minut, callback, secunda=0, mesaj="Ora mesei!"):
        self.ora = ora
        self.minut = minut
        self.secunda = secunda
        self.callback = callback
        self.mesaj = mesaj
        self._timer = None

    def run(self):
        print(f"[ExactTimeTask] {self.mesaj}")
        if self.callback:
            self.callback(self.mesaj)

    def _secunde_pana_la_tinta(self):
        acum = datetime.now()
        tinta = acum.replace(hour=self.ora, minute=self.minut,
                              second=self.secunda, microsecond=0)
        if tinta <= acum:
            tinta += timedelta(days=1)
        return (tinta - acum).total_seconds()

    def start(self):
        delay = self._secunde_pana_la_tinta()
        self._timer = threading.Timer(delay, self.run)
        self._timer.daemon = True
        self._timer.start()
        return self._timer

    def cancel(self):
        if self._timer:
            self._timer.cancel()


if __name__ == "__main__":
    print("Start ExactTimeTask - se va executa la ora 18:24:00")
    task = ExactTimeTask(18, 24, None, 0, "Salut, Salut, Salut!!!")
    task.start()
