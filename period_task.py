"""
PeriodTask - reactioneaza REPETAT, cu o PERIOADA indicata intre executii.
Echivalentul lui Timer.scheduleAtFixedRate(task, delay, period) din Java:
se re-planifica singur dupa fiecare executie, la fiecare "period" secunde.

Primeste un callback (functie) care este apelat la fiecare executie,
astfel incat interfata grafica sa poata fi actualizata (ex: incrementarea
unui contor pe un buton).
"""

import threading


class PeriodTask:
    def __init__(self, period_seconds, callback, mesaj="Sound played"):
        self.period_seconds = period_seconds
        self.callback = callback
        self.mesaj = mesaj
        self._timer = None
        self._activ = False

    def run(self):
        print(f"[PeriodTask] {self.mesaj}")
        if self.callback:
            self.callback(self.mesaj)
        if self._activ:
            self._timer = threading.Timer(self.period_seconds, self.run)
            self._timer.daemon = True
            self._timer.start()

    def start(self):
        self._activ = True
        self._timer = threading.Timer(self.period_seconds, self.run)
        self._timer.daemon = True
        self._timer.start()
        return self._timer

    def cancel(self):
        self._activ = False
        if self._timer:
            self._timer.cancel()


if __name__ == "__main__":
    print("Start PeriodTask - se repeta la fiecare 2 secunde")
    task = PeriodTask(2, None, "Sound played")
    task.start()