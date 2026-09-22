"""
IntervalTask - reactioneaza la un anumit INTERVAL de timp (delay).
Echivalentul lui Timer.schedule(task, delay) din Java: se executa
o singura data, dupa ce trece intervalul specificat.
"""

import threading


class IntervalTask:
    def __init__(self, delay_seconds, mesaj="Au trecut intervalul specificat!"):
        self.delay_seconds = delay_seconds
        self.mesaj = mesaj
        self._timer = None

    def run(self):
        print(f"[IntervalTask] {self.mesaj}")

    def start(self):
        self._timer = threading.Timer(self.delay_seconds, self.run)
        self._timer.start()
        return self._timer

    def cancel(self):
        if self._timer:
            self._timer.cancel()


if __name__ == "__main__":
    print("Start IntervalTask - se va executa peste 3 secunde")
    task = IntervalTask(3, "Au trecut 3 secunde de la start!")
    task.start()
