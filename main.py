import tkinter as tk
from datetime import datetime, timedelta
from tkinter import messagebox, ttk

from clock import DelayTimer


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
        self._build_header()
        self._build_timer_grid()
        self._build_controls()
        self._build_log()
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

    def _build_header(self):
        header = ttk.Frame(self, style="App.TFrame", padding=(28, 24, 28, 12))
        header.pack(fill="x")
        ttk.Label(header, text="Our Timer App", style="Title.TLabel").pack(anchor="w")
        ttk.Label(header, text="A simple timer application.", style="Subtitle.TLabel").pack(anchor="w", pady=(3, 11))
        self.clock_label = ttk.Label(header, style="Clock.TLabel")
        self.clock_label.pack(anchor="w")

    def _build_timer_grid(self):
        grid = ttk.Frame(self, style="App.TFrame", padding=(22, 5, 22, 14))
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

    def _build_log(self):
        log_frame = ttk.Frame(self, style="App.TFrame", padding=(28, 0, 28, 18))
        log_frame.pack(fill="x")
        ttk.Label(log_frame, text="Activity", style="CardText.TLabel").pack(anchor="w", pady=(0, 4))
        self.log_box = tk.Text(log_frame, height=4, state="disabled", bg="#ffffff", fg="#475569", relief="flat", font=("Courier", 9), padx=9, pady=7)
        self.log_box.pack(fill="x")

    def _build_controls(self):
        controls = ttk.Frame(self, style="App.TFrame", padding=(28, 2, 28, 14))
        controls.pack(fill="x")
        buttons = ttk.Frame(controls, style="App.TFrame")
        buttons.pack()
        ttk.Button(buttons, text="Start sequence", style="Primary.TButton", command=self.start_sequence).pack(side="left", padx=5)
        ttk.Button(buttons, text="Stop", style="Stop.TButton", command=self.stop_sequence).pack(side="left", padx=5)
        self.sequence_label = ttk.Label(controls, text="Ready to start Phase 1", style="Subtitle.TLabel")
        self.sequence_label.pack(pady=(9, 0))

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