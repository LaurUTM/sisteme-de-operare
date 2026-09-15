import datetime
import queue
import threading
import time
import tkinter as tk
from tkinter import ttk


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


class Timer:
    def __init__(self, name="timer"):
        self.name = name
        self._stop = threading.Event()

    def schedule(self, task, when, period=None):
        self._start(self._fixed_delay_loop, task, self._delay(when), period)

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
        threading.Thread(target=loop, args=args, name=self.name, daemon=True).start()

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


class RepeatingTimer:
    def __init__(self, interval, action):
        self.interval = interval
        self.action = action
        self._running = False
        self._thread = None

    def _run(self):
        self.action()
        if self._running:
            self._thread = threading.Timer(self.interval, self._run)
            self._thread.daemon = True
            self._thread.start()

    def start(self):
        self._running = True
        self._run()

    def cancel(self):
        self._running = False
        if self._thread:
            self._thread.cancel()


class CallbackTask(TimerTask):
    def __init__(self, callback):
        super().__init__()
        self.callback = callback

    def run(self):
        self.callback()


class App:
    def __init__(self, root):
        self.root = root
        root.title("Planificare Timere")
        root.geometry("540x520")

        self.log_queue = queue.Queue()
        self.interval_timer = None
        self.alarm_timer = None
        self.repeating_timer = None

        frame = ttk.Frame(root, padding=12)
        frame.pack(fill="both", expand=True)

        ttk.Label(frame, text="1. Interval fix", font=("Segoe UI", 10, "bold")).grid(row=0, column=0, sticky="w")
        self.interval_seconds = tk.StringVar(value="5")
        ttk.Entry(frame, textvariable=self.interval_seconds, width=6).grid(row=0, column=1)
        ttk.Label(frame, text="secunde").grid(row=0, column=2, sticky="w")
        ttk.Button(frame, text="Start", command=self.start_interval).grid(row=0, column=3, padx=5)

        ttk.Label(frame, text="2. Moment exact (HH:MM:SS)", font=("Segoe UI", 10, "bold")).grid(
            row=1, column=0, sticky="w", pady=(12, 0))
        self.alarm_time = tk.StringVar()
        ttk.Entry(frame, textvariable=self.alarm_time, width=10).grid(row=1, column=1, pady=(12, 0))
        ttk.Button(frame, text="Programeaza", command=self.start_alarm).grid(row=1, column=3, padx=5, pady=(12, 0))

        ttk.Label(frame, text="3. Perioada (repetitiv)", font=("Segoe UI", 10, "bold")).grid(
            row=2, column=0, sticky="w", pady=(12, 0))
        self.period_seconds = tk.StringVar(value="2")
        ttk.Entry(frame, textvariable=self.period_seconds, width=6).grid(row=2, column=1, pady=(12, 0))
        ttk.Label(frame, text="secunde").grid(row=2, column=2, sticky="w", pady=(12, 0))
        self.beep_button = ttk.Button(frame, text="Start beep", command=self.toggle_beep)
        self.beep_button.grid(row=2, column=3, padx=5, pady=(12, 0))

        ttk.Separator(frame, orient="horizontal").grid(row=3, column=0, columnspan=5, sticky="ew", pady=12)

        ttk.Label(frame, text="Jurnal:", font=("Segoe UI", 10, "bold")).grid(row=4, column=0, sticky="w")
        self.log_box = tk.Listbox(frame, width=64, height=18)
        self.log_box.grid(row=5, column=0, columnspan=4, pady=5)

        scrollbar = ttk.Scrollbar(frame, orient="vertical", command=self.log_box.yview)
        scrollbar.grid(row=5, column=4, sticky="ns")
        self.log_box.config(yscrollcommand=scrollbar.set)

        ttk.Button(frame, text="Opreste tot", command=self.stop_all).grid(row=6, column=0, pady=10, sticky="w")

        self.root.after(100, self.process_log_queue)

    def log(self, message):
        self.log_queue.put(f"[{datetime.datetime.now():%H:%M:%S}] {message}")

    def process_log_queue(self):
        while not self.log_queue.empty():
            message = self.log_queue.get_nowait()
            self.log_box.insert("end", message)
            self.log_box.see("end")
        self.root.after(100, self.process_log_queue)

    def start_interval(self):
        try:
            seconds = float(self.interval_seconds.get())
        except ValueError:
            self.log("[Interval] valoare invalida")
            return
        self.interval_timer = Timer("interval")
        self.interval_timer.schedule(
            CallbackTask(lambda: self.log(f"[Interval] Au trecut {seconds:g} secunde")), seconds)
        self.log(f"[Interval] Timer pornit pentru {seconds:g} secunde")

    def start_alarm(self):
        text = self.alarm_time.get().strip()
        try:
            if text:
                parts = [int(p) for p in text.split(":")]
                hour, minute, second = (parts + [0, 0])[:3]
                now = datetime.datetime.now()
                target = now.replace(hour=hour, minute=minute, second=second, microsecond=0)
                if target <= now:
                    target += datetime.timedelta(days=1)
            else:
                target = datetime.datetime.now() + datetime.timedelta(seconds=10)
        except ValueError:
            self.log("[Moment exact] format invalid, foloseste HH:MM:SS")
            return
        self.alarm_timer = Timer("alarm")
        self.alarm_timer.schedule(
            CallbackTask(lambda: self.log("[Moment exact] S-a atins timpul tinta")), target)
        self.log(f"[Moment exact] Alarma programata la {target:%H:%M:%S}")

    def toggle_beep(self):
        if self.repeating_timer is None:
            try:
                interval = float(self.period_seconds.get())
            except ValueError:
                self.log("[Periodic] valoare invalida")
                return
            self.repeating_timer = RepeatingTimer(interval, lambda: self.log("[Periodic] beep"))
            self.repeating_timer.start()
            self.beep_button.config(text="Stop beep")
            self.log(f"[Periodic] Beep pornit la fiecare {interval:g} secunde")
        else:
            self.repeating_timer.cancel()
            self.repeating_timer = None
            self.beep_button.config(text="Start beep")
            self.log("[Periodic] Beep oprit")

    def stop_all(self):
        if self.interval_timer:
            self.interval_timer.cancel()
        if self.alarm_timer:
            self.alarm_timer.cancel()
        if self.repeating_timer:
            self.repeating_timer.cancel()
            self.repeating_timer = None
            self.beep_button.config(text="Start beep")
        self.log("Toate timerele au fost oprite")


def main():
    root = tk.Tk()
    App(root)
    root.mainloop()


if __name__ == "__main__":
    main()