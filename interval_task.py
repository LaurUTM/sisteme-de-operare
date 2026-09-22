"""
IntervalTask - reactioneaza la un anumit INTERVAL de timp (delay).
Echivalentul lui Timer.schedule(task, delay) din Java: se executa
o singura data, dupa ce trece intervalul specificat.

Primeste un callback (functie) care este apelat cand timer-ul expira,
astfel incat interfata grafica sa poata fi actualizata.
"""

import threading


class IntervalTask:
    def __init__(self, delay_seconds, callback, mesaj="Au trecut intervalul specificat!"):
        self.delay_seconds = delay_seconds
        self.callback = callback
        self.mesaj = mesaj
        self._timer = None

    def run(self):
        print(f"[IntervalTask] {self.mesaj}")
        if self.callback:
            self.callback(self.mesaj)

    def start(self):
        self._timer = threading.Timer(self.delay_seconds, self.run)
        self._timer.daemon = True
        self._timer.start()
        return self._timer

    def cancel(self):
        if self._timer:
            self._timer.cancel()


if __name__ == "__main__":
    print("Start IntervalTask - se va executa peste 3 secunde")
    task = IntervalTask(3, None, "Au trecut 3 secunde de la start!")
    task.start()