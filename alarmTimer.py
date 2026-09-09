import threading
import datetime

def delayed_message():
    print("5 seconds passed")   

timer1 = threading.Timer(5, delayed_message)
timer1.start

def alarm():
    print("target time reached")

now = datetime.datetime.now()
target_time = now+datetime.timedelta(seconds=10)
delay_until_target = (target_time-now).total_seconds()

timer2 = threading.Timer(delay_until_target, alarm)
timer2.start()

class repeatingTimer:
    def __init__(self, interval, action):
        self.interval=interval
        self.action=action
        self._running=False
        self._thread=None

    def _run(self):
        self.action()
        if self._running:
            self._thread=threading.Timer(self.interval, self._run)
            self._thread.start()

    def start(self):
        self._running=True
        self._run()

    def cancel(self):
        self._running=False
        if self._thread:
            self._thread.cancel()

def beep():
    print("beep")

repeating_timer=repeatingTimer(2, beep)
repeating_timer.start()

threading.Timer(8, repeating_timer.cancel).start()

print("started")