"""
Sistem Multi-Timer (Tkinter) — 4 timere într-un singur fișier.

  Timer 1 și Timer 2 — implementate de mine
  Timer 3 și Timer 4 — implementate de coleg

Toate cele 4 timere se bazează pe ACELAȘI mecanism de bibliotecă:
metoda `after(ms, callback)` a ferestrei Tkinter (`root`).

  root.after(ms, callback)

este instrucțiunea care CREEAZĂ efectiv un timer: înregistrează un
callback în bucla de evenimente Tcl/Tk, care va fi executat automat
peste `ms` milisecunde. Noi nu implementăm temporizarea de la zero
(nu numărăm cu un `while` + `sleep`) — delegăm complet crearea și
gestionarea timerului către bibliotecă. Fiecare clasă de mai jos are
un comentariu "<<< AICI se creează timer-ul" exact la linia respectivă,
ca să fie ușor de arătat/explicat.
"""

import datetime
import time
import tkinter as tk
from tkinter import messagebox, ttk


def configure_button_styles():
  """Stiluri ttk pentru butoane colorate, vizibile pe orice OS.

  Pe macOS, tk.Button cu `bg` colorat nu își randează fundalul (temă
  Aqua nativă) — textul alb rămâne invizibil pe fundal alb până la
  apăsare. ttk cu tema "clam" randează culorile corect pe Mac/Windows.
  """
  style = ttk.Style()
  style.theme_use("clam")
  colors = {
      "Green.TButton": "#4CAF50",
      "Blue.TButton": "#2196F3",
      "Red.TButton": "#F44336",
      "Orange.TButton": "#FF9800",
      "Gray.TButton": "#9E9E9E",
  }
  for name, color in colors.items():
    style.configure(name, background=color, foreground="white", padding=6)
    style.map(
        name,
        background=[("active", color), ("pressed", color)],
        foreground=[("active", "white"), ("pressed", "white")],
    )


class AlarmTimer:
  """TIMER 1 (eu): alarmă declanșată la o oră fixă din zi (HH:MM:SS)."""

  def __init__(self, root, parent):
    self.root = root

    frame = tk.LabelFrame(
        parent, text="Timer 1: Alarmă la oră fixă", padx=10, pady=5
    )
    frame.pack(fill="x", padx=15, pady=5)

    tk.Label(frame, text="Ora țintă (ex: 14:30:00):").pack()
    self.entry_time = tk.Entry(frame)
    self.entry_time.pack()

    ttk.Button(
        frame, text="Setează Alarmă", style="Green.TButton", command=self.start
    ).pack(pady=5)

  def start(self):
    target_str = self.entry_time.get().strip()
    try:
      now = datetime.datetime.now()
      target_time = datetime.datetime.strptime(target_str, "%H:%M:%S").time()
      target_dt = datetime.datetime.combine(now.date(), target_time)

      if target_dt < now:
        target_dt += datetime.timedelta(days=1)

      delay_ms = int((target_dt - now).total_seconds() * 1000)

      # <<< AICI se creează timer-ul: root.after() programează alarma
      # o singură dată, peste `delay_ms` milisecunde.
      self.root.after(delay_ms, self.alarm)

      messagebox.showinfo("Timer 1", f"Alarmă setată pentru ora {target_str}!")
    except ValueError:
      messagebox.showerror(
          "Eroare", "Format oră invalid! Folosește HH:MM:SS (ex: 15:30:00)."
      )

  def alarm(self):
    messagebox.showinfo("Timer 1", "Ora programată a sosit!")


class RepeatingTimer:
  """TIMER 2 (eu): se declanșează repetat, la fiecare N secunde."""

  def __init__(self, root, parent):
    self.root = root
    self.running = False
    self.job = None

    frame = tk.LabelFrame(
        parent, text="Timer 2: Repetitiv (interval secunde)", padx=10, pady=5
    )
    frame.pack(fill="x", padx=15, pady=5)

    tk.Label(frame, text="Interval (secunde):").pack()
    self.entry_interval = tk.Entry(frame)
    self.entry_interval.pack()

    frame_btn = tk.Frame(frame)
    frame_btn.pack(pady=5)

    ttk.Button(
        frame_btn, text="Start", style="Blue.TButton", command=self.start
    ).pack(side="left", padx=5)
    ttk.Button(
        frame_btn, text="Stop", style="Red.TButton", command=self.stop
    ).pack(side="left", padx=5)

  def start(self):
    if self.running:
      return
    try:
      interval_ms = float(self.entry_interval.get()) * 1000
      if interval_ms <= 0:
        raise ValueError
    except ValueError:
      messagebox.showerror("Eroare", "Introdu o valoare validă în secunde.")
      return

    self.running = True
    self._schedule(interval_ms)

  def _schedule(self, interval_ms):
    print("Timer 2: ciclu repetitiv declanșat.")
    # <<< AICI se creează (re-creează) timer-ul, la fiecare ciclu.
    # Fiindcă apelăm din nou after() în interiorul propriei funcții,
    # obținem efectul de repetare (un nou timer, nu unul singur reciclat).
    self.job = self.root.after(
        int(interval_ms), lambda: self._schedule(interval_ms)
    )

  def stop(self):
    if not self.running:
      return
    self.running = False
    if self.job:
      self.root.after_cancel(self.job)
      self.job = None
    messagebox.showinfo("Timer 2", "Timerul repetitiv a fost oprit.")


class DelayedAlarm:
  """TIMER 3 (coleg): alarmă unică, după N secunde de întârziere."""

  def __init__(self, root, parent):
    self.root = root

    frame = tk.LabelFrame(
        parent, text="Timer 3: Alarmă cu întârziere", padx=10, pady=5
    )
    frame.pack(fill="x", padx=15, pady=5)

    tk.Label(frame, text="Declanșează peste (secunde):").pack()
    self.entry_delay = tk.Entry(frame)
    self.entry_delay.pack()

    ttk.Button(
        frame, text="Pornește Timer 3", style="Orange.TButton", command=self.start
    ).pack(pady=5)

  def start(self):
    try:
      delay_sec = float(self.entry_delay.get())
      if delay_sec <= 0:
        raise ValueError
    except ValueError:
      messagebox.showerror(
          "Eroare", "Introdu un număr valid de secunde pentru Timer 3."
      )
      return

    # <<< AICI se creează timer-ul: o singură programare, peste
    # `delay_sec` secunde (convertite în milisecunde).
    self.root.after(int(delay_sec * 1000), self.alarm)

    messagebox.showinfo(
        "Timer 3", f"Timer pornit! Va suna peste {delay_sec} secunde."
    )

  def alarm(self):
    messagebox.showinfo("Timer 3", "Timpul pentru Timer 3 a expirat!")


class Stopwatch:
  """TIMER 4 (coleg): cronometru cu Start / Stop / Resetează."""

  def __init__(self, root, parent):
    self.root = root
    self.running = False
    self.elapsed = 0.0
    self.start_time = None
    self.job = None

    frame = tk.LabelFrame(
        parent, text="Timer 4: Cronometru (Stopwatch)", padx=10, pady=5
    )
    frame.pack(fill="x", padx=15, pady=5)

    self.label_time = tk.Label(frame, text="00:00:00.0", font=("Helvetica", 18))
    self.label_time.pack(pady=5)

    frame_btn = tk.Frame(frame)
    frame_btn.pack(pady=5)

    ttk.Button(
        frame_btn, text="Start", style="Green.TButton", command=self.start
    ).pack(side="left", padx=5)
    ttk.Button(
        frame_btn, text="Stop", style="Red.TButton", command=self.stop
    ).pack(side="left", padx=5)
    ttk.Button(
        frame_btn, text="Resetează", style="Gray.TButton", command=self.reset
    ).pack(side="left", padx=5)

  def start(self):
    if self.running:
      return
    self.running = True
    # Scădem `elapsed` ca reluarea după Stop să continue de unde a rămas,
    # nu de la 0.
    self.start_time = time.time() - self.elapsed
    self._tick()

  def _tick(self):
    if not self.running:
      return
    self.elapsed = time.time() - self.start_time
    self.label_time.config(text=self._format(self.elapsed))
    # <<< AICI se creează (re-creează) timer-ul, la fiecare 100ms —
    # ăsta e "pulsul" cronometrului.
    self.job = self.root.after(100, self._tick)

  def stop(self):
    if not self.running:
      return
    self.running = False
    if self.job:
      self.root.after_cancel(self.job)
      self.job = None

  def reset(self):
    self.stop()
    self.elapsed = 0.0
    self.label_time.config(text=self._format(self.elapsed))

  @staticmethod
  def _format(seconds):
    minutes, sec = divmod(seconds, 60)
    hours, minutes = divmod(minutes, 60)
    tenths = int((sec - int(sec)) * 10)
    return f"{int(hours):02d}:{int(minutes):02d}:{int(sec):02d}.{tenths}"


def main():
  root = tk.Tk()
  root.title("Sistem Multi-Timer")
  root.geometry("450x680")
  root.resizable(False, False)

  configure_button_styles()

  AlarmTimer(root, root)
  RepeatingTimer(root, root)
  DelayedAlarm(root, root)
  Stopwatch(root, root)

  root.mainloop()


if __name__ == "__main__":
  main()
