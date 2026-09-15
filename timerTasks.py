import datetime
import sys
import threading
import time

_print_lock = threading.Lock()


def log(message):
    with _print_lock:
        print(f"[{datetime.datetime.now():%H:%M:%S}] {message}")


# Actiunea planificata se scrie in run()
class TimerTask:
    def __init__(self):
        self._cancelled = threading.Event()

    def run(self):
        raise NotImplementedError

    def cancel(self):
        self._cancelled.set()

    @property
    def cancelled(self):
        return self._cancelled.is_set()


# when = intarziere in secunde sau datetime
class Timer:
    def __init__(self, name="timer"):
        self.name = name
        self._stop = threading.Event()

    # intarziere fixa
    def schedule(self, task, when, period=None):
        self._start(self._fixed_delay_loop, task, self._delay(when), period)

    # rata fixa
    def schedule_at_fixed_rate(self, task, when, period):
        self._start(self._fixed_rate_loop, task, self._delay(when), period)

    def cancel(self):
        self._stop.set()

    @staticmethod
    def _delay(when):
        if isinstance(when, datetime.datetime):
            return max(0.0, (when - datetime.datetime.now()).total_seconds())
        return float(when)

    def _start(self, loop, *args):
        threading.Thread(target=loop, args=args, name=self.name).start()

    # True daca timerul a fost oprit
    def _wait(self, seconds):
        return self._stop.wait(max(0.0, seconds))

    def _fixed_delay_loop(self, task, delay, period):
        if self._wait(delay) or task.cancelled:
            return
        while True:
            task.run()
            if period is None or task.cancelled:
                return
            if self._wait(period) or task.cancelled:
                return

    def _fixed_rate_loop(self, task, delay, period):
        next_run = time.monotonic() + delay
        while True:
            if self._wait(next_run - time.monotonic()) or task.cancelled:
                return
            task.run()
            next_run += period


class SoundTask(TimerTask):
    def run(self):
        log("\a[sunet] Beep")


class MessageTask(TimerTask):
    def __init__(self, message):
        super().__init__()
        self.message = message

    def run(self):
        log(self.message)


# se opreste singur dupa `runs` executii
class WorkTask(TimerTask):
    def __init__(self, name, work, runs):
        super().__init__()
        self.name = name
        self.work = work
        self.runs = runs
        self.count = 0

    def run(self):
        self.count += 1
        log(f"[{self.name}] executia {self.count}/{self.runs}")
        time.sleep(self.work)
        if self.count >= self.runs:
            self.cancel()


class CancelTimersTask(TimerTask):
    def __init__(self, message, *timers):
        super().__init__()
        self.message = message
        self.timers = timers

    def run(self):
        log(self.message)
        for timer in self.timers:
            timer.cancel()


# "HH:MM" sau "HH:MM:SS"
def parse_time(text):
    parts = [int(p) for p in text.split(":")]
    hour, minute, second = (parts + [0])[:3]
    now = datetime.datetime.now()
    target = now.replace(hour=hour, minute=minute, second=second, microsecond=0)
    if target <= now:
        target += datetime.timedelta(days=1)
    return target


def main():
    log("Start")

    # 1. dupa un interval
    message_timer = Timer("message")
    message_timer.schedule(MessageTask("Au trecut 5 secunde"), 5)

    # 2. la o anumita ora
    alarm_time = (parse_time(sys.argv[1]) if len(sys.argv) > 1
                  else datetime.datetime.now() + datetime.timedelta(seconds=15))
    alarm_timer = Timer("alarm")
    alarm_timer.schedule(MessageTask("Alarma: ora stabilita a sosit!"), alarm_time)
    log(f"Alarma programata la {alarm_time:%H:%M:%S}")

    # 3. periodic
    sound_timer = Timer("sound")
    sound_timer.schedule_at_fixed_rate(SoundTask(), 0, 2)
    message_timer.schedule(CancelTimersTask("Au trecut 10 secunde - oprim sunetul", sound_timer), 10)

    # schedule() vs schedule_at_fixed_rate()
    work_timer = Timer("work")
    work_timer.schedule(WorkTask("fixed-delay", work=1, runs=4), 0, period=2)
    work_timer.schedule_at_fixed_rate(WorkTask("fixed-rate", work=1, runs=4), 0, period=2)

    # oprim toate timerele
    stop_after = max(20.0, (alarm_time - datetime.datetime.now()).total_seconds() + 1)
    stop_timer = Timer("stop")
    stop_timer.schedule(
        CancelTimersTask("Oprim toate timerele", message_timer, alarm_timer,
                         sound_timer, work_timer, stop_timer),
        stop_after)


if __name__ == "__main__":
    main()
