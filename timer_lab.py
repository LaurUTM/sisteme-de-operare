
import threading
import time

class TimerTask:
    """Clasa de baza pentru "sarcinile" programabile (echivalentul TimerTask din Java).

    Fiecare task concret mosteneste aceasta clasa si isi suprascrie run().
    """

    def run(self):
        raise NotImplementedError("Suprascrieti metoda run() in subclasa.")


class Timer:
    """Planificator de task-uri, dupa modelul java.util.Timer.

    Poate programa un task o singura data (schedule) sau repetitiv
    (scheduleAtFixedRate). Fiecare executie ruleaza pe un thread separat.
    """

    def __init__(self, name="Timer"):
        self.name = name                    # nume folosit doar pentru identificare/logare
        self._thread = None                 # threading.Timer-ul care asteapta urmatoarea executie
        self._cancelled = False             # flag setat de cancel(), verificat inainte de fiecare rulare
        self._lock = threading.Lock()       # protejeaza _thread si _cancelled (acces din mai multe thread-uri)

    def _run_once_after_delay(self, task: TimerTask, delay, period, fixed_rate):
        """Implementarea comuna pentru schedule() si scheduleAtFixedRate()."""

        def _fire():
            # Rulat pe thread-ul timer-ului, dupa expirarea intarzierii.
            if self._cancelled:
                return
            start = time.monotonic()        # ceas monoton: nu e afectat de schimbarea orei sistemului
            task.run()
            with self._lock:
                # period=None => task cu o singura executie, nu reprogramam.
                if self._cancelled or period is None:
                    return
                # scheduleAtFixedRate: compensam intarzierile ca sa
                # pastram numarul de executii constant intr-o perioada.
                if fixed_rate:
                    elapsed = time.monotonic() - start          # cat a durat task.run()
                    next_delay = max(0.0, period - elapsed)     # scadem durata din perioada
                else:
                    # fixed-delay: asteptam perioada intreaga de la sfarsitul executiei.
                    next_delay = period
                self._thread = threading.Timer(next_delay, _fire)
                self._thread.daemon = True
                self._thread.start()

        with self._lock:
            # Resetam flag-ul ca timer-ul sa poata fi refolosit dupa un cancel().
            self._cancelled = False
            self._thread = threading.Timer(delay, _fire)
            self._thread.daemon = True
            self._thread.start()

    def schedule(self, task: TimerTask, delay, period=None):
        """delay/period in secunde. Fara period => o singura executie."""
        self._run_once_after_delay(task, delay, period, fixed_rate=False)

    def scheduleAtFixedRate(self, task: TimerTask, delay, period):
        """Executie repetitiva la rata fixa: perioada se masoara de la inceputul
        executiei precedente, nu de la sfarsitul ei."""
        self._run_once_after_delay(task, delay, period, fixed_rate=True)

    def cancel(self):
        """Opreste timer-ul: task-urile deja programate nu se mai executa."""
        with self._lock:
            self._cancelled = True          # opreste un _fire() care tocmai a pornit
            if self._thread is not None:
                self._thread.cancel()       # anuleaza asteptarea in curs


class ReminderTask(TimerTask):
    """Task repetitiv: la fiecare 'period' secunde afiseaza un mesaj de
    reamintire si incrementeaza un contor.
    """

    def __init__(self, message, on_tick):
        self.message = message              # textul reamintirii
        self.count = 0                      # de cate ori s-a executat task-ul
        self.on_tick = on_tick              # callback catre UI, primeste (count, message)

    def run(self):
        self.count += 1
        try:
            # "\a" = caracterul BEL, produce un beep in terminal (daca e suportat).
            print("\a", end="", flush=True)
        except Exception:
            # Un terminal care nu suporta beep nu trebuie sa opreasca reamintirea.
            pass
        self.on_tick(self.count, self.message)


class DelayedMsgTask(TimerTask):
    """Task cu o singura executie: dupa 'delay' secunde apeleaza on_fire(message)."""

    def __init__(self, message, on_fire):
        self.message = message              # mesajul intarziat
        self.on_fire = on_fire              # callback catre UI, primeste (message)

    def run(self):
        self.on_fire(self.message)
