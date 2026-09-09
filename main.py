import time
import tkinter as tk
from datetime import datetime, timedelta
from tkinter import messagebox, ttk

from clock import DelayTimer
from timer_lab import DelayedMsgTask, ReminderTask,Timer, TimerTask

class TimerApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Our Timer App")
        self.geometry("700x600")
        self.minsize(600, 540)
        self.configure(bg="#eef2f6")

        self._setup_style()
        self.running = False
        self.active_phase = None
        self.active_timer = None
        self.deadline = None
        self.phase_duration = 0
        self.phase_data = {}
        self.timer_lab_message_timer = Timer("TimerLabMessage")
        self.timer_lab_reminder_timer = Timer("TimerLabReminder")
        self.timer_lab_count = 0
        self.timer_lab_progress_vars = {"message": None, "reminder": None}
        self.timer_lab_progress_jobs = {"message": None, "reminder": None}
        self.timer_lab_progress_totals = {"message": None, "reminder": None}
        self.timer_lab_progress_starts = {"message": None, "reminder": None}

        self.notebook = ttk.Notebook(self)
        self.notebook.pack(fill="both", expand=True)

        self.main_tab = ttk.Frame(self.notebook, style="App.TFrame")
        self.notebook.add(self.main_tab, text="Two Phase Timer")

        self.timer_lab_tab = ttk.Frame(self.notebook, style="App.TFrame")
        self.notebook.add(self.timer_lab_tab, text="Timer Lab")

        self._build_header(self.main_tab)
        self._build_timer_grid(self.main_tab)
        self._build_controls(self.main_tab)
        self._build_log(self.main_tab)
        self._build_timer_lab_tab(self.timer_lab_tab)
        self._refresh_display()
        self.protocol("WM_DELETE_WINDOW", self._close)

    def _setup_style(self):
        style = ttk.Style(self)
        style.theme_use("clam")
        style.configure("App.TFrame", background="#eef2f6")
        style.configure("Card.TFrame", background="#ffffff")
        style.configure("Title.TLabel", background="#eef2f6", foreground="#18212f", font=("Helvetica", 25, "bold"))
        style.configure("Subtitle.TLabel", background="#eef2f6", foreground="#647184", font=("Helvetica", 10))
        style.configure("Clock.TLabel", background="#eef2f6", foreground="#18212f", font=("Helvetica", 15, "bold"))
        style.configure("CardTitle.TLabel", background="#ffffff", foreground="#18212f", font=("Helvetica", 13, "bold"))
        style.configure("CardText.TLabel", background="#ffffff", foreground="#647184", font=("Helvetica", 9))
        style.configure("Time.TLabel", background="#ffffff", foreground="#18212f", font=("Courier", 27, "bold"))
        style.configure("Status.TLabel", background="#ffffff", foreground="#647184", font=("Helvetica", 9, "bold"))
        style.configure("Primary.TButton", background="#2563eb", foreground="#ffffff", borderwidth=0, padding=(13, 7), font=("Helvetica", 9, "bold"))
        style.map("Primary.TButton", background=[("active", "#1d4ed8")])
        style.configure("Stop.TButton", background="#e8edf3", foreground="#334155", borderwidth=0, padding=(13, 7), font=("Helvetica", 9, "bold"))
        style.map("Stop.TButton", background=[("active", "#d7dee8")])
        style.configure("Card.Horizontal.TProgressbar", troughcolor="#e8edf3", background="#2563eb", borderwidth=0, thickness=7)

    def _build_header(self, parent):
        header = ttk.Frame(parent, style="App.TFrame", padding=(28, 24, 28, 12))
        header.pack(fill="x")
        ttk.Label(header, text="Our Timer App", style="Title.TLabel").pack(anchor="w")
        ttk.Label(header, text="A simple timer application.", style="Subtitle.TLabel").pack(anchor="w", pady=(3, 11))
        self.clock_label = ttk.Label(header, style="Clock.TLabel")
        self.clock_label.pack(anchor="w")

    def _build_timer_grid(self, parent):
        grid = ttk.Frame(parent, style="App.TFrame", padding=(22, 5, 22, 14))
        grid.pack(fill="x")
        grid.columnconfigure(0, weight=1)
        grid.columnconfigure(1, weight=1)
        self.phase_data["phase1"] = self._build_phase_card(grid, 0, "Ceas 1", "Preparation", "40", "Card.Horizontal.TProgressbar")
        self.phase_data["phase2"] = self._build_phase_card(grid, 1, "Ceas 2", "Action", "5", "Card.Horizontal.TProgressbar")

    def _build_phase_card(self, parent, column, heading, default_name, default_minutes, progress_style):
        card = ttk.Frame(parent, style="Card.TFrame", padding=16)
        card.grid(row=0, column=column, sticky="nsew", padx=6)
        card.columnconfigure(0, weight=1)
        ttk.Label(card, text=heading, style="CardTitle.TLabel").grid(row=0, column=0, sticky="w")
        name_entry = ttk.Entry(card, font=("Helvetica", 10))
        name_entry.insert(0, default_name)
        name_entry.grid(row=1, column=0, sticky="ew", pady=(7, 11))
        time_label = ttk.Label(card, text=f"{int(default_minutes):02d}:00", style="Time.TLabel")
        time_label.grid(row=2, column=0, sticky="w", pady=(0, 7))
        status_label = ttk.Label(card, text="READY", style="Status.TLabel")
        status_label.grid(row=3, column=0, sticky="w")
        progress = ttk.Progressbar(card, style=progress_style, mode="determinate", maximum=100)
        progress.grid(row=4, column=0, sticky="ew", pady=(9, 12))
        ttk.Label(card, text="Duration in minutes", style="CardText.TLabel").grid(row=5, column=0, sticky="w")
        duration_entry = ttk.Entry(card, width=10, font=("Helvetica", 10))
        duration_entry.insert(0, default_minutes)
        duration_entry.grid(row=6, column=0, sticky="w", pady=(3, 0))
        return {"name": name_entry, "duration": duration_entry, "time": time_label, "status": status_label, "progress": progress}

    def _build_log(self, parent):
        log_frame = ttk.Frame(parent, style="App.TFrame", padding=(28, 0, 28, 18))
        log_frame.pack(fill="x")
        ttk.Label(log_frame, text="Activity", style="CardText.TLabel").pack(anchor="w", pady=(0, 4))
        self.log_box = tk.Text(log_frame, height=4, state="disabled", bg="#ffffff", fg="#475569", relief="flat", font=("Courier", 9), padx=9, pady=7)
        self.log_box.pack(fill="x")

    def _build_controls(self, parent):
        controls = ttk.Frame(parent, style="App.TFrame", padding=(28, 2, 28, 14))
        controls.pack(fill="x")
        buttons = ttk.Frame(controls, style="App.TFrame")
        buttons.pack()
        ttk.Button(buttons, text="Start sequence", style="Primary.TButton", command=self.start_sequence).pack(side="left", padx=5)
        ttk.Button(buttons, text="Stop", style="Stop.TButton", command=self.stop_sequence).pack(side="left", padx=5)
        self.sequence_label = ttk.Label(controls, text="Ready to start Phase 1", style="Subtitle.TLabel")
        self.sequence_label.pack(pady=(9, 0))

    def _build_timer_lab_tab(self, parent):
        container = ttk.Frame(parent, style="App.TFrame", padding=(24, 18, 24, 20))
        container.pack(fill="both", expand=True)

        ttk.Label(container, text="Timer Lab tools", style="CardTitle.TLabel").pack(anchor="w")
        ttk.Label(
            container,
            text="2 timere,Primul o actiune repetitiva,al 2-lea un mesaj intarziat.",
            style="CardText.TLabel",
        ).pack(anchor="w", pady=(4, 12))

        reminder_frame = ttk.Frame(container, style="Card.TFrame", padding=14)
        reminder_frame.pack(fill="x", pady=(0, 12))

        ttk.Label(reminder_frame, text="Timer 1 - Reminder", style="CardTitle.TLabel").grid(row=0, column=0, columnspan=3, sticky="w")

        ttk.Label(reminder_frame, text="Delay (sec)", style="CardText.TLabel").grid(row=1, column=0, sticky="w", padx=(0, 12), pady=(10, 6))
        self.timer_lab_reminder_delay = ttk.Entry(reminder_frame, width=12)
        self.timer_lab_reminder_delay.insert(0, "3")
        self.timer_lab_reminder_delay.grid(row=1, column=1, sticky="w", pady=(10, 6))

        ttk.Label(reminder_frame, text="Perioada (sec)", style="CardText.TLabel").grid(row=2, column=0, sticky="w", padx=(0, 12), pady=(0, 6))
        self.timer_lab_reminder_period = ttk.Entry(reminder_frame, width=12)
        self.timer_lab_reminder_period.insert(0, "1")
        self.timer_lab_reminder_period.grid(row=2, column=1, sticky="w", pady=(0, 6))

        ttk.Label(reminder_frame, text="Mesaj", style="CardText.TLabel").grid(row=3, column=0, sticky="w", padx=(0, 12), pady=(0, 6))
        self.timer_lab_reminder_message = ttk.Entry(reminder_frame, width=24)
        self.timer_lab_reminder_message.insert(0, "Bea apa!")
        self.timer_lab_reminder_message.grid(row=3, column=1, sticky="w", pady=(0, 6))

        self.timer_lab_reminder_status = ttk.Label(reminder_frame, text="Ready", style="Status.TLabel")
        self.timer_lab_reminder_status.grid(row=4, column=0, columnspan=2, sticky="w", pady=(6, 6))

        self.timer_lab_progress_vars["reminder"] = tk.DoubleVar(value=0)
        self.timer_lab_reminder_progress = ttk.Progressbar(
            reminder_frame,
            style="Card.Horizontal.TProgressbar",
            mode="determinate",
            maximum=100,
            length=330,
            variable=self.timer_lab_progress_vars["reminder"],
        )
        self.timer_lab_reminder_progress.grid(row=5, column=0, columnspan=3, sticky="ew", pady=(0, 8))

        reminder_buttons = ttk.Frame(reminder_frame, style="App.TFrame")
        reminder_buttons.grid(row=6, column=0, columnspan=3, sticky="w")
        ttk.Button(reminder_buttons, text="Start reminder", style="Primary.TButton", command=self.start_timer_lab_beep).pack(side="left", padx=(0, 8))
        ttk.Button(reminder_buttons, text="Stop reminder", style="Stop.TButton", command=self.stop_timer_lab_reminder).pack(side="left")

        message_frame = ttk.Frame(container, style="Card.TFrame", padding=14)
        message_frame.pack(fill="x")

        ttk.Label(message_frame, text="Timer 2 - Mesaj intarziat", style="CardTitle.TLabel").grid(row=0, column=0, columnspan=2, sticky="w")

        ttk.Label(message_frame, text="Delay (sec)", style="CardText.TLabel").grid(row=1, column=0, sticky="w", padx=(0, 12), pady=(10, 6))
        self.timer_lab_message_delay = ttk.Entry(message_frame, width=12)
        self.timer_lab_message_delay.insert(0, "5")
        self.timer_lab_message_delay.grid(row=1, column=1, sticky="w", pady=(10, 6))

        ttk.Label(message_frame, text="Mesaj", style="CardText.TLabel").grid(row=2, column=0, sticky="w", padx=(0, 12), pady=(0, 6))
        self.timer_lab_message_text = ttk.Entry(message_frame, width=24)
        self.timer_lab_message_text.insert(0, "Salut, Salut, Salut!!!")
        self.timer_lab_message_text.grid(row=2, column=1, sticky="w", pady=(0, 6))

        self.timer_lab_message_status = ttk.Label(message_frame, text="Ready", style="Status.TLabel")
        self.timer_lab_message_status.grid(row=3, column=0, columnspan=2, sticky="w", pady=(6, 6))

        self.timer_lab_progress_vars["message"] = tk.DoubleVar(value=0)
        self.timer_lab_message_progress = ttk.Progressbar(
            message_frame,
            style="Card.Horizontal.TProgressbar",
            mode="determinate",
            maximum=100,
            length=330,
            variable=self.timer_lab_progress_vars["message"],
        )
        self.timer_lab_message_progress.grid(row=4, column=0, columnspan=2, sticky="ew", pady=(0, 8))

        message_buttons = ttk.Frame(message_frame, style="App.TFrame")
        message_buttons.grid(row=5, column=0, columnspan=2, sticky="w")
        ttk.Button(message_buttons, text="Start message", style="Primary.TButton", command=self.start_timer_lab_message).pack(side="left", padx=(0, 8))
        ttk.Button(message_buttons, text="Stop message", style="Stop.TButton", command=self.stop_timer_lab_message).pack(side="left")

        self.timer_lab_log = tk.Text(container, height=8, state="disabled", bg="#ffffff", fg="#475569", relief="flat", font=("Courier", 9), padx=9, pady=7)
        self.timer_lab_log.pack(fill="both", expand=True, pady=(12, 0))

    def _timer_lab_log(self, text):
        self.timer_lab_log.config(state="normal")
        self.timer_lab_log.insert("end", f"[{datetime.now().strftime('%H:%M:%S')}] {text}\n")
        self.timer_lab_log.see("end")
        self.timer_lab_log.config(state="disabled")

    def _read_timer_lab_delay(self, entry_widget, field_name):
        try:
            value = float(entry_widget.get())
            if value < 0:
                raise ValueError
            return value
        except ValueError:
            messagebox.showerror("Invalid delay", f"{field_name} must be a positive number of seconds.")
            return None

    def _read_timer_lab_period(self, entry_widget, field_name):
        try:
            value = float(entry_widget.get())
            if value <= 0:
                raise ValueError
            return value
        except ValueError:
            messagebox.showerror("Invalid period", f"{field_name} must be a positive number of seconds.")
            return None

    def _clear_timer_lab_progress(self, timer_name):
        job = self.timer_lab_progress_jobs.get(timer_name)
        if job is not None:
            self.after_cancel(job)
        self.timer_lab_progress_jobs[timer_name] = None
        self.timer_lab_progress_totals[timer_name] = None
        self.timer_lab_progress_starts[timer_name] = None
        if self.timer_lab_progress_vars.get(timer_name) is not None:
            self.timer_lab_progress_vars[timer_name].set(0)

    def _start_timer_lab_progress(self, timer_name, total_seconds):
        self._clear_timer_lab_progress(timer_name)
        self.timer_lab_progress_totals[timer_name] = max(float(total_seconds), 0.1)
        self.timer_lab_progress_starts[timer_name] = time.monotonic()
        self._update_timer_lab_progress(timer_name)

    def _update_timer_lab_progress(self, timer_name):
        start = self.timer_lab_progress_starts.get(timer_name)
        total = self.timer_lab_progress_totals.get(timer_name)
        if start is None or total is None:
            return

        elapsed = time.monotonic() - start
        progress = min(100.0, max(0.0, (elapsed / total) * 100))
        self.timer_lab_progress_vars[timer_name].set(progress)

        if elapsed < total:
            self.timer_lab_progress_jobs[timer_name] = self.after(100, self._update_timer_lab_progress, timer_name)
        else:
            self.timer_lab_progress_vars[timer_name].set(100)
            self._clear_timer_lab_progress(timer_name)

    def start_timer_lab_message(self):
        delay = self._read_timer_lab_delay(self.timer_lab_message_delay, "Delay")
        if delay is None:
            return

        message = self.timer_lab_message_text.get().strip() or "Timer Lab message"
        self.timer_lab_message_timer.cancel()
        self.timer_lab_message_timer = Timer("TimerLabMessage")
        task = DelayedMsgTask(message, self._on_timer_lab_message)
        self.timer_lab_message_timer.schedule(task, delay)
        self.timer_lab_message_status.config(text=f"Scheduled message in {delay:.1f}s")
        self._start_timer_lab_progress("message", delay)
        self._timer_lab_log(f"Message timer started ({delay:.1f}s): {message}")

    def start_timer_lab_beep(self):
        delay = self._read_timer_lab_delay(self.timer_lab_reminder_delay, "Delay")
        period = self._read_timer_lab_period(self.timer_lab_reminder_period, "Period")
        if delay is None or period is None:
            return

        self.timer_lab_reminder_timer.cancel()
        self.timer_lab_reminder_timer = Timer("TimerLabReminder")
        self.timer_lab_count = 0
        message = self.timer_lab_reminder_message.get().strip() or "Bea apa!"
        task = ReminderTask(message, self._on_timer_lab_beep)
        self.timer_lab_reminder_timer.scheduleAtFixedRate(task, delay, period)
        self.timer_lab_reminder_status.config(text=f"Repeating reminder every {period:.1f}s")
        self._start_timer_lab_progress("reminder", period)
        self._timer_lab_log(f"Reminder timer started (delay={delay:.1f}s, period={period:.1f}s, message='{message}')")

    def _on_timer_lab_message(self, message):
        self.after(0, self._timer_lab_message_ui, message)

    def _timer_lab_message_ui(self, message):
        self.timer_lab_message_status.config(text=f"Message executed: {message}")
        self._clear_timer_lab_progress("message")
        self._timer_lab_log(f"Message fired: {message}")

    def _on_timer_lab_beep(self, count, message):
        self.after(0, self._timer_lab_beep_ui, count, message)

    def _timer_lab_beep_ui(self, count, message):
        self.timer_lab_count = count
        self.timer_lab_reminder_status.config(text=f"Reminder running • count={count} • {message}")
        self._start_timer_lab_progress("reminder", float(self.timer_lab_reminder_period.get()))
        self._timer_lab_log(f"Reminder #{count}: {message}")

    def stop_timer_lab_message(self):
        self.timer_lab_message_timer.cancel()
        self.timer_lab_message_status.config(text="Ready")
        self._clear_timer_lab_progress("message")
        self._timer_lab_log("Message timer stopped")

    def stop_timer_lab_reminder(self):
        self.timer_lab_reminder_timer.cancel()
        self.timer_lab_reminder_status.config(text="Ready")
        self._clear_timer_lab_progress("reminder")
        self._timer_lab_log("Reminder timer stopped")

    def stop_timer_lab(self):
        self.stop_timer_lab_message()
        self.stop_timer_lab_reminder()

    def log(self, text):
        self.after(0, self._log_impl, text)

    def _log_impl(self, text):
        ts = datetime.now().strftime("%H:%M:%S")
        self.log_box.config(state="normal")
        self.log_box.insert("end", f"[{ts}] {text}\n")
        self.log_box.see("end")
        self.log_box.config(state="disabled")

    def _read_phase(self, phase):
        data = self.phase_data[phase]
        name = data["name"].get().strip() or phase.title()
        try:
            minutes = float(data["duration"].get())
            if minutes <= 0:
                raise ValueError
        except ValueError:
            messagebox.showerror("Invalid duration", "Duration must be a positive number of minutes.")
            return None
        return name, minutes

    def start_sequence(self):
        first = self._read_phase("phase1")
        second = self._read_phase("phase2")
        if first is None or second is None:
            return
        if self.running:
            self.stop_sequence(silent=True)
        self.phases = {"phase1": first, "phase2": second}
        self.running = True
        self._start_phase("phase1")
        self.log(f"Sequence started: {first[0]} -> {second[0]}")

    def _start_phase(self, phase):
        name, minutes = self.phases[phase]
        self.active_phase = phase
        self.phase_duration = minutes * 60
        self.deadline = datetime.now() + timedelta(seconds=self.phase_duration)
        self.active_timer = DelayTimer(self.phase_duration, lambda: self._phase_finished(phase))
        self.active_timer.start()
        other = "phase2" if phase == "phase1" else "phase1"
        self._set_phase_state(phase, "RUNNING")
        self._set_phase_state(other, "NEXT")
        self.sequence_label.config(text=f"Now running: {name}")

    def _phase_finished(self, phase):
        self.after(0, lambda: self._switch_phase(phase))

    def _switch_phase(self, phase):
        if not self.running or phase != self.active_phase:
            return
        next_phase = "phase2" if phase == "phase1" else "phase1"
        finished_name = self.phases[phase][0]
        next_name = self.phases[next_phase][0]
        self.bell()
        self._start_phase(next_phase)
        messagebox.showinfo(f"{finished_name} complete", f"Now starting: {next_name}", parent=self)
        self.log(f"Transition: {finished_name} -> {next_name}")

    def _set_phase_state(self, phase, state):
        data = self.phase_data[phase]
        color = "#16a34a" if state == "RUNNING" else "#647184"
        data["status"].config(text=state, foreground=color)

    def stop_sequence(self, silent=False):
        if self.active_timer:
            self.active_timer.stop()
        was_running = self.running
        self.running = False
        self.active_phase = None
        self.deadline = None
        self.active_timer = None
        for data in self.phase_data.values():
            data["status"].config(text="STOPPED" if was_running else "READY", foreground="#647184")
            data["time"].config(text="00:00")
            data["progress"]["value"] = 0
        self.sequence_label.config(text="Sequence stopped. Press Start sequence to begin again.")
        if was_running and not silent:
            self.log("Sequence stopped by user")

    def _refresh_display(self):
        now = datetime.now()
        self.clock_label.config(text=now.strftime("%A, %d %B  •  %H:%M:%S"))
        if self.running and self.deadline:
            remaining = max(0, (self.deadline - now).total_seconds())
            current = self.phase_data[self.active_phase]
            current["time"].config(text=self._format_seconds(remaining))
            current["progress"]["value"] = max(0, min(100, remaining / self.phase_duration * 100))
        self.after(250, self._refresh_display)

    @staticmethod
    def _format_seconds(seconds):
        total_seconds = int(seconds)
        days, remainder = divmod(total_seconds, 86400)
        hours, remainder = divmod(remainder, 3600)
        minutes, seconds = divmod(remainder, 60)
        if days:
            return f"{days}d {hours:02d}:{minutes:02d}"
        if hours:
            return f"{hours:02d}:{minutes:02d}:{seconds:02d}"
        return f"{minutes:02d}:{seconds:02d}"

    def _close(self):
        self.stop_sequence(silent=True)
        self.destroy()


if __name__ == "__main__":
    app = TimerApp()
    app.mainloop()