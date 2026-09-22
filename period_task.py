"""
PeriodTask - reactioneaza REPETAT, cu o PERIOADA indicata intre executii.
Echivalentul lui Timer.scheduleAtFixedRate(task, delay, period) din Java:
se re-planifica singur dupa fiecare executie, la fiecare "period" secunde.
"""

import threading


class PeriodTask:
    def __init__(self, period_seconds, mesaj="Beep!"):
        self.period_seconds = period_seconds
        self.mesaj = mesaj
        self._timer = None
        self._activ = False

    def run(self):
        print(f"[PeriodTask] {self.mesaj}")
        if self._activ:
            self._timer = threading.Timer(self.period_seconds, self.run)
            self._timer.start()

    def start(self):
        self._activ = True
        self._timer = threading.Timer(self.period_seconds, self.run)
        self._timer.start()
        return self._timer

    def cancel(self):
        self._activ = False
        if self._timer:
            self._timer.cancel()


if __name__ == "__main__":
    print("Start PeriodTask - se repeta la fiecare 2 secunde")
    task = PeriodTask(2, "Sound played")
    task.start()
