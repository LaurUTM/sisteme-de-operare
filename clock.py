import threading


class DelayTimer:
    """Timer cu o singura executie: dupa 'delay_sec' secunde apeleaza callback().
    """

    def __init__(self, delay_sec, callback):
        self.delay_sec = delay_sec          # intarzierea, in secunde
        self.callback = callback            # functia apelata la expirare
        self._timer = None                  # obiectul threading.Timer creat la start()

    def start(self):
        # creează un obiect nou de tip threading.Timer, dar nu îl pornește încă.
        self._timer = threading.Timer(self.delay_sec, self.callback) # Este un constructor, creeaza o instanta a clasei Timer, self_delay = interval cat timp asteapta pana la exec, fallback functioa v a fi apelata la expirarea timerului. 
        # daemon=True => thread-ul nu tine procesul in viata la inchiderea aplicatiei.
        self._timer.daemon = True
        self._timer.start()

    def stop(self):
        # cancel() opreste timer-ul doar daca nu a apucat inca sa execute callback-ul.
        if self._timer:
            self._timer.cancel()


class PeriodicTimer:
    """Timer repetitiv: apeleaza callback() la fiecare 'period_sec' secunde.

    Se reprogrameaza singur dupa fiecare executie (fixed-delay: perioada se
    masoara de la sfarsitul executiei, nu de la inceputul ei).
    """

    def __init__(self, period_sec, callback):
        self.period_sec = period_sec        # perioada dintre doua executii, in secunde
        self.callback = callback            # functia apelata la fiecare tick
        self._timer = None                  # threading.Timer-ul curent
        self._running = False               # flag de oprire, verificat la fiecare tick

    def _tick(self):
        # Daca intre timp s-a apelat stop(), nu mai executam si nu reprogramam.
        if not self._running:
            return
        self.callback()
        # Reprogramam urmatoarea executie => bucla periodica.
        self._timer = threading.Timer(self.period_sec, self._tick)
        self._timer.daemon = True
        self._timer.start()

    def start(self):
        self._running = True
        # Primul tick se executa imediat, nu dupa o perioada de asteptare.
        self._tick()

    def stop(self):
        # Intai ridicam flag-ul (opreste un tick aflat in curs sa reprogrameze),
        # apoi anulam timer-ul care asteapta.
        self._running = False
        if self._timer:
            self._timer.cancel()
