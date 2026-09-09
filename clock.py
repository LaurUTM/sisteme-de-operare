import threading


class DelayTimer:

    def __init__(self, delay_sec, callback):
        self.delay_sec = delay_sec
        self.callback = callback
        self._timer = None

    def start(self):
        self._timer = threading.Timer(self.delay_sec, self.callback)
        self._timer.daemon = True
        self._timer.start()

    def stop(self):
        if self._timer:
            self._timer.cancel()


class PeriodicTimer:

    def __init__(self, period_sec, callback):
        self.period_sec = period_sec
        self.callback = callback
        self._timer = None
        self._running = False

    def _tick(self):
        if not self._running:
            return
        self.callback()
        self._timer = threading.Timer(self.period_sec, self._tick)
        self._timer.daemon = True
        self._timer.start()

    def start(self):
        self._running = True
        self._tick()

    def stop(self):
        self._running = False
        if self._timer:
            self._timer.cancel()