import time
import tkinter as tk


class Stopwatch:

  def __init__(self, root):
    self.root = root

    self.running = False
    self.elapsed = 0.0
    self.start_time = None
    self.update_job = None

    self.build_ui()

  def build_ui(self):
    frame = tk.LabelFrame(
        self.root,
        text="Timer 4: Cronometru (Stopwatch)",
        padx=10,
        pady=5,
    )
    frame.pack(fill="x", padx=15, pady=5)

    self.label_time = tk.Label(frame, text="00:00:00.0", font=("Helvetica", 18))
    self.label_time.pack(pady=5)

    frame_btn = tk.Frame(frame)
    frame_btn.pack(pady=5)

    self.btn_start = tk.Button(
        frame_btn,
        text="Start",
        bg="#4CAF50",
        fg="white",
        command=self.start,
    )
    self.btn_start.pack(side="left", padx=5)

    self.btn_stop = tk.Button(
        frame_btn,
        text="Stop",
        bg="#F44336",
        fg="white",
        command=self.stop,
    )
    self.btn_stop.pack(side="left", padx=5)

    self.btn_reset = tk.Button(
        frame_btn,
        text="Resetează",
        bg="#9E9E9E",
        fg="white",
        command=self.reset,
    )
    self.btn_reset.pack(side="left", padx=5)

  def start(self):
    if self.running:
      return
    self.running = True
    self.start_time = time.time() - self.elapsed
    self.tick()

  def tick(self):
    if not self.running:
      return
    self.elapsed = time.time() - self.start_time
    self.label_time.config(text=self.format_time(self.elapsed))
    self.update_job = self.root.after(100, self.tick)

  def stop(self):
    if not self.running:
      return
    self.running = False
    if self.update_job:
      self.root.after_cancel(self.update_job)
      self.update_job = None

  def reset(self):
    self.stop()
    self.elapsed = 0.0
    self.label_time.config(text=self.format_time(self.elapsed))

  @staticmethod
  def format_time(seconds):
    minutes, sec = divmod(seconds, 60)
    hours, minutes = divmod(minutes, 60)
    tenths = int((sec - int(sec)) * 10)
    return f"{int(hours):02d}:{int(minutes):02d}:{int(sec):02d}.{tenths}"
