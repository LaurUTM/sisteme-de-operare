import datetime
import tkinter as tk
from tkinter import messagebox, ttk


class CountdownTimer:

  def __init__(self, root):
    self.root = root
    self.root.title("Sistem Multi-Timer")
    self.root.geometry("450x520")
    self.root.resizable(False, False)

    # Stări pentru timere
    self.timer2_running = False
    self.timer2_job = None
    self.timer3_job = None

    self.build_ui()

  def build_ui(self):
    # Butoanele colorate (bg) nu se randează pe macOS cu tk.Button (temă
    # Aqua nativă) — textul alb rămâne invizibil pe fundal alb până la
    # apăsare. ttk cu tema "clam" randează culorile corect pe orice OS.
    style = ttk.Style()
    style.theme_use("clam")
    for name, color in (
        ("Green.TButton", "#4CAF50"),
        ("Blue.TButton", "#2196F3"),
        ("Red.TButton", "#F44336"),
        ("Orange.TButton", "#FF9800"),
    ):
      style.configure(name, background=color, foreground="white", padding=6)
      style.map(
          name,
          background=[("active", color), ("pressed", color)],
          foreground=[("active", "white"), ("pressed", "white")],
      )

    # TIMER 1: Eveniment programat la o oră fixă (HH:MM:SS)
    frame_t1 = tk.LabelFrame(
        self.root,
        text="Timer 1: Eveniment Programat (HH:MM:SS)",
        padx=10,
        pady=5,
    )
    frame_t1.pack(fill="x", padx=15, pady=5)

    tk.Label(frame_t1, text="Ora țintă (ex: 14:30:00):").pack()
    self.entry_time1 = tk.Entry(frame_t1)
    self.entry_time1.pack()

    ttk.Button(
        frame_t1,
        text="Setează Alarmă",
        style="Green.TButton",
        command=self.start_timer1,
    ).pack(pady=5)

    # TIMER 2: Repetitiv la un interval specificat (secunde)
    frame_t2 = tk.LabelFrame(
        self.root,
        text="Timer 2: Repetitiv (Interval Secunde)",
        padx=10,
        pady=5,
    )
    frame_t2.pack(fill="x", padx=15, pady=5)

    tk.Label(frame_t2, text="Interval (secunde):").pack()
    self.entry_interval2 = tk.Entry(frame_t2)
    self.entry_interval2.pack()

    frame_btn2 = tk.Frame(frame_t2)
    frame_btn2.pack(pady=5)

    self.btn_start2 = ttk.Button(
        frame_btn2,
        text="Start",
        style="Blue.TButton",
        command=self.start_timer2,
    )
    self.btn_start2.pack(side="left", padx=5)

    self.btn_stop2 = ttk.Button(
        frame_btn2, text="Stop", style="Red.TButton", command=self.stop_timer2
    )
    self.btn_stop2.pack(side="left", padx=5)

    # TIMER 3: Alarmă cu întârziere fixă (secunde)
    frame_t3 = tk.LabelFrame(
        self.root, text="Timer 3: Alarmă cu Întârziere", padx=10, pady=5
    )
    frame_t3.pack(fill="x", padx=15, pady=5)

    tk.Label(frame_t3, text="Declanșează peste (secunde):").pack()
    self.entry_delay3 = tk.Entry(frame_t3)
    self.entry_delay3.pack()

    ttk.Button(
        frame_t3,
        text="Pornește Timer 3",
        style="Orange.TButton",
        command=self.trigger_timer3,
    ).pack(pady=5)

  # --- LOGICĂ TIMER 1 ---
  def start_timer1(self):
    target_str = self.entry_time1.get().strip()
    try:
      now = datetime.datetime.now()
      target_time = datetime.datetime.strptime(target_str, "%H:%M:%S").time()
      target_dt = datetime.datetime.combine(now.date(), target_time)

      if target_dt < now:
        target_dt += datetime.timedelta(days=1)

      delay_ms = int((target_dt - now).total_seconds() * 1000)
      self.root.after(delay_ms, self.alarm_timer1)
      messagebox.showinfo(
          "Timer 1", f"Alarmă setată pentru ora {target_str}!"
      )
    except ValueError:
      messagebox.showerror(
          "Eroare", "Format oră invalid! Folosește HH:MM:SS (ex: 15:30:00)."
      )

  def alarm_timer1(self):
    messagebox.showinfo("Timer 1 Alarmă", "Ora programată a sosit!")

  # --- LOGICĂ TIMER 2 ---
  def start_timer2(self):
    if self.timer2_running:
      return
    try:
      interval = float(self.entry_interval2.get()) * 1000
      if interval <= 0:
        raise ValueError
      self.timer2_running = True
      self.run_timer2(interval)
    except ValueError:
      messagebox.showerror("Eroare", "Introdu o valoare validă în secunde.")

  def run_timer2(self, interval):
    if self.timer2_running:
      print("Timer 2: Ciclu repetitiv declanșat.")
      self.timer2_job = self.root.after(
          int(interval), lambda: self.run_timer2(interval)
      )

  def stop_timer2(self):
    if self.timer2_running:
      self.timer2_running = False
      if self.timer2_job:
        self.root.after_cancel(self.timer2_job)
      messagebox.showinfo("Timer 2", "Timerul repetitiv a fost oprit.")

  # --- LOGICĂ TIMER 3 ---
  def trigger_timer3(self):
    try:
      delay_sec = float(self.entry_delay3.get())
      if delay_sec <= 0:
        raise ValueError
      delay_ms = int(delay_sec * 1000)
      self.timer3_job = self.root.after(delay_ms, self.alarm_timer3)
      messagebox.showinfo(
          "Timer 3", f"Timer pornit! Va suna peste {delay_sec} secunde."
      )
    except ValueError:
      messagebox.showerror(
          "Eroare", "Introdu un număr valid de secunde pentru Timer 3."
      )

  def alarm_timer3(self):
    messagebox.showinfo("Timer 3", "Timpul pentru Timer 3 a expirat!")