
import threading
import time

class TimerTask:

    def run(self):
        raise NotImplementedError("Suprascrieti metoda run() in subclasa.")


class Timer:
    def __init__(self, name="Timer"):
        self.name = name
        self._thread = None
        self._cancelled = False
        self._lock = threading.Lock()

    def _run_once_after_delay(self, task: TimerTask, delay, period, fixed_rate):
        def _fire():
            if self._cancelled:
                return
            start = time.monotonic()
            task.run()
            with self._lock:
                if self._cancelled or period is None:
                    return
                # scheduleAtFixedRate: compensam intarzierile ca sa
                # pastram numarul de executii constant intr-o perioada.
                if fixed_rate:
                    elapsed = time.monotonic() - start
                    next_delay = max(0.0, period - elapsed)
                else:
                    next_delay = period
                self._thread = threading.Timer(next_delay, _fire)
                self._thread.daemon = True
                self._thread.start()

        with self._lock:
            self._cancelled = False
            self._thread = threading.Timer(delay, _fire)
            self._thread.daemon = True
            self._thread.start()

    def schedule(self, task: TimerTask, delay, period=None):
        """delay/period in secunde. Fara period => o singura executie."""
        self._run_once_after_delay(task, delay, period, fixed_rate=False)

    def scheduleAtFixedRate(self, task: TimerTask, delay, period):
        self._run_once_after_delay(task, delay, period, fixed_rate=True)

    def cancel(self):
        with self._lock:
            self._cancelled = True
            if self._thread is not None:
                self._thread.cancel()


class ReminderTask(TimerTask):
    """Task repetitiv: la fiecare 'period' secunde afiseaza un mesaj de
    reamintire si incrementeaza un contor.
    """

    def __init__(self, message, on_tick):
        self.message = message
        self.count = 0
        self.on_tick = on_tick

    def run(self):
        self.count += 1
        try:
            print("\a", end="", flush=True) 
        except Exception:
            pass
        self.on_tick(self.count, self.message)


class DelayedMsgTask(TimerTask):
    """Task cu o singura executie: dupa 'delay' secunde apeleaza on_fire(message)."""

    def __init__(self, message, on_fire):
        self.message = message
        self.on_fire = on_fire

    def run(self):
        self.on_fire(self.message)