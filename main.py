"""
main.py - interfata grafica (Tkinter) cu TABURI SEPARATE pentru fiecare
timer al echipei.

Cele 3 cerinte ale lucrarii, fiecare pe tabul ei:
1. Reactie la un interval de timp     -> tab "Interval"    -> IntervalTask
2. Reactie la un timp exact           -> tab "Timp exact"  -> ExactTimeTask
3. Reactie cu o perioada indicata     -> tab "Perioada"    -> PeriodTask
Plus un tab "Jurnal" cu toate evenimentele la un loc.
"""

import tkinter as tk
from tkinter import ttk
from datetime import datetime, timedelta

from interval_task import IntervalTask
from exact_time_task import ExactTimeTask
from period_task import PeriodTask
from repeating_exact_task import RepeatingExactTask


class TimerApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Aplicatie Timere - Lucrare de laborator")
        self.geometry("480x420")
        self.resizable(False, False)

        self.period_counter = 0
        self.repeating_counter = 0
        self.interval_task = None
        self.exact_task = None
        self.period_task = None
        self.repeating_task = None

        self._build_ui()

    def _build_ui(self):
        notebook = ttk.Notebook(self)
        notebook.pack(fill="both", expand=True, padx=10, pady=10)

        tab_interval = ttk.Frame(notebook)
        tab_exact = ttk.Frame(notebook)
        tab_period = ttk.Frame(notebook)
        tab_repeating = ttk.Frame(notebook)
        tab_log = ttk.Frame(notebook)

        notebook.add(tab_interval, text="1. Interval")
        notebook.add(tab_exact, text="2. Timp exact")
        notebook.add(tab_period, text="3. Perioada")
        notebook.add(tab_repeating, text="4. Exact+Perioada")
        notebook.add(tab_log, text="Jurnal")

        self._build_tab_interval(tab_interval)
        self._build_tab_exact(tab_exact)
        self._build_tab_period(tab_period)
        self._build_tab_repeating(tab_repeating)
        self._build_tab_log(tab_log)

    # ---- Tab 1: Interval ----
    def _build_tab_interval(self, parent):
        tk.Label(parent, text="Reactie la un interval de timp",
                 font=("Segoe UI", 11, "bold")).pack(pady=15)
        tk.Label(parent, text="Timerul se declanseaza o singura data, dupa\n"
                               "un numar de secunde de la apasarea butonului.",
                 justify="center").pack(pady=5)

        frame = tk.Frame(parent)
        frame.pack(pady=15)
        tk.Label(frame, text="Secunde:").grid(row=0, column=0, padx=5)
        self.interval_entry = tk.Entry(frame, width=6)
        self.interval_entry.insert(0, "3")
        self.interval_entry.grid(row=0, column=1)
        self.interval_btn = tk.Button(frame, text="Porneste", command=self.porneste_interval)
        self.interval_btn.grid(row=0, column=2, padx=10)

        self.interval_status = tk.Label(parent, text="Status: neinceput",
                                         fg="gray", font=("Segoe UI", 10))
        self.interval_status.pack(pady=15)

    # ---- Tab 2: Timp exact ----
    def _build_tab_exact(self, parent):
        tk.Label(parent, text="Reactie la un timp exact",
                 font=("Segoe UI", 11, "bold")).pack(pady=15)
        tk.Label(parent, text="Timerul se declanseaza o singura data,\n"
                               "la ora si minutul specificate.",
                 justify="center").pack(pady=5)

        frame = tk.Frame(parent)
        frame.pack(pady=15)
        tk.Label(frame, text="Ora:").grid(row=0, column=0, padx=5)
        self.ora_entry = tk.Entry(frame, width=4)
        acum_plus = datetime.now() + timedelta(minutes=1)
        self.ora_entry.insert(0, str(acum_plus.hour))
        self.ora_entry.grid(row=0, column=1)

        tk.Label(frame, text="Minut:").grid(row=0, column=2, padx=5)
        self.minut_entry = tk.Entry(frame, width=4)
        self.minut_entry.insert(0, str(acum_plus.minute))
        self.minut_entry.grid(row=0, column=3)

        self.exact_btn = tk.Button(frame, text="Porneste", command=self.porneste_exact)
        self.exact_btn.grid(row=0, column=4, padx=10)

        self.exact_status = tk.Label(parent, text="Status: neinceput",
                                      fg="gray", font=("Segoe UI", 10))
        self.exact_status.pack(pady=15)

    # ---- Tab 3: Perioada ----
    def _build_tab_period(self, parent):
        tk.Label(parent, text="Reactie cu o perioada indicata",
                 font=("Segoe UI", 11, "bold")).pack(pady=15)
        tk.Label(parent, text="Timerul se repeta la fiecare N secunde,\n"
                               "pana este oprit manual.",
                 justify="center").pack(pady=5)

        frame = tk.Frame(parent)
        frame.pack(pady=15)
        tk.Label(frame, text="Perioada (sec):").grid(row=0, column=0, padx=5)
        self.period_entry = tk.Entry(frame, width=6)
        self.period_entry.insert(0, "2")
        self.period_entry.grid(row=0, column=1)

        self.period_btn = tk.Button(frame, text="Porneste", command=self.porneste_period)
        self.period_btn.grid(row=0, column=2, padx=10)

        self.period_stop_btn = tk.Button(frame, text="Opreste", command=self.opreste_period,
                                          state="disabled")
        self.period_stop_btn.grid(row=0, column=3, padx=5)

        self.period_counter_label = tk.Label(parent, text="Executii: 0",
                                              fg="gray", font=("Segoe UI", 10))
        self.period_counter_label.pack(pady=15)

    # ---- Tab 4: Exact + Perioada (combinat) ----
    def _build_tab_repeating(self, parent):
        tk.Label(parent, text="Timp exact + perioada (combinat)",
                 font=("Segoe UI", 11, "bold")).pack(pady=15)
        tk.Label(parent, text="Porneste la ora/minutul specificate, apoi se\n"
                               "repeta la fiecare N secunde pana e oprit.",
                 justify="center").pack(pady=5)

        frame = tk.Frame(parent)
        frame.pack(pady=15)
        tk.Label(frame, text="Ora:").grid(row=0, column=0, padx=5)
        self.rep_ora_entry = tk.Entry(frame, width=4)
        acum_plus = datetime.now() + timedelta(minutes=1)
        self.rep_ora_entry.insert(0, str(acum_plus.hour))
        self.rep_ora_entry.grid(row=0, column=1)

        tk.Label(frame, text="Minut:").grid(row=0, column=2, padx=5)
        self.rep_minut_entry = tk.Entry(frame, width=4)
        self.rep_minut_entry.insert(0, str(acum_plus.minute))
        self.rep_minut_entry.grid(row=0, column=3)

        tk.Label(frame, text="Perioada (sec):").grid(row=0, column=4, padx=5)
        self.rep_period_entry = tk.Entry(frame, width=5)
        self.rep_period_entry.insert(0, "5")
        self.rep_period_entry.grid(row=0, column=5)

        self.repeating_btn = tk.Button(parent, text="Porneste", command=self.porneste_repeating)
        self.repeating_btn.pack(pady=5)

        self.repeating_stop_btn = tk.Button(parent, text="Opreste",
                                             command=self.opreste_repeating, state="disabled")
        self.repeating_stop_btn.pack(pady=5)

        self.repeating_counter_label = tk.Label(parent, text="Executii: 0",
                                                  fg="gray", font=("Segoe UI", 10))
        self.repeating_counter_label.pack(pady=10)

    # ---- Tab Jurnal ----
    def _build_tab_log(self, parent):
        tk.Label(parent, text="Jurnal evenimente",
                 font=("Segoe UI", 11, "bold")).pack(pady=10)
        self.log_text = tk.Text(parent, height=14, state="disabled")
        self.log_text.pack(fill="both", expand=True, padx=10, pady=5)

    def _log(self, mesaj):
        ora_curenta = datetime.now().strftime("%H:%M:%S")
        self.log_text.configure(state="normal")
        self.log_text.insert("end", f"[{ora_curenta}] {mesaj}\n")
        self.log_text.see("end")
        self.log_text.configure(state="disabled")

    # ---- Callback-uri apelate din firele Timer (thread-safe prin self.after) ----

    def _on_interval(self, mesaj):
        self.after(0, lambda: (
            self.interval_status.config(text="Status: declansat!", fg="green"),
            self._log(f"IntervalTask: {mesaj}")
        ))

    def _on_exact(self, mesaj):
        self.after(0, lambda: (
            self.exact_status.config(text="Status: declansat!", fg="green"),
            self._log(f"ExactTimeTask: {mesaj}")
        ))

    def _on_period(self, mesaj):
        def actualizeaza():
            self.period_counter += 1
            self.period_counter_label.config(text=f"Executii: {self.period_counter}")
            self._log(f"PeriodTask: {mesaj}")
        self.after(0, actualizeaza)

    def _on_repeating(self, mesaj):
        def actualizeaza():
            self.repeating_counter += 1
            self.repeating_counter_label.config(text=f"Executii: {self.repeating_counter}")
            self._log(f"RepeatingExactTask: {mesaj}")
        self.after(0, actualizeaza)

    # ---- Actiuni butoane ----

    def porneste_interval(self):
        try:
            secunde = float(self.interval_entry.get())
        except ValueError:
            self._log("Eroare: numar de secunde invalid pentru IntervalTask")
            return
        self.interval_status.config(text="Status: in asteptare...", fg="orange")
        self.interval_task = IntervalTask(secunde, self._on_interval,
                                           f"Au trecut {secunde} secunde de la start!")
        self.interval_task.start()
        self._log(f"IntervalTask pornit - se declanseaza peste {secunde} secunde")

    def porneste_exact(self):
        try:
            ora = int(self.ora_entry.get())
            minut = int(self.minut_entry.get())
        except ValueError:
            self._log("Eroare: ora/minut invalide pentru ExactTimeTask")
            return
        self.exact_status.config(text="Status: in asteptare...", fg="orange")
        self.exact_task = ExactTimeTask(ora, minut, self._on_exact, 0, "Salut, Salut, Salut!!!")
        self.exact_task.start()
        self._log(f"ExactTimeTask pornit - se declanseaza la ora {ora:02d}:{minut:02d}:00")

    def porneste_period(self):
        try:
            perioada = float(self.period_entry.get())
        except ValueError:
            self._log("Eroare: perioada invalida pentru PeriodTask")
            return
        self.period_counter = 0
        self.period_counter_label.config(text="Executii: 0")
        self.period_task = PeriodTask(perioada, self._on_period, "Sound played")
        self.period_task.start()
        self.period_btn.config(state="disabled")
        self.period_stop_btn.config(state="normal")
        self._log(f"PeriodTask pornit - se repeta la fiecare {perioada} secunde")

    def opreste_period(self):
        if self.period_task:
            self.period_task.cancel()
        self.period_btn.config(state="normal")
        self.period_stop_btn.config(state="disabled")
        self._log("PeriodTask oprit manual")

    def porneste_repeating(self):
        try:
            ora = int(self.rep_ora_entry.get())
            minut = int(self.rep_minut_entry.get())
            perioada = float(self.rep_period_entry.get())
        except ValueError:
            self._log("Eroare: valori invalide pentru RepeatingExactTask")
            return
        self.repeating_counter = 0
        self.repeating_counter_label.config(text="Executii: 0")
        self.repeating_task = RepeatingExactTask(ora, minut, perioada, self._on_repeating,
                                                  0, "Reamintire periodica!")
        self.repeating_task.start()
        self.repeating_btn.config(state="disabled")
        self.repeating_stop_btn.config(state="normal")
        self._log(f"RepeatingExactTask pornit - incepe la {ora:02d}:{minut:02d}, "
                   f"se repeta la {perioada} secunde")

    def opreste_repeating(self):
        if self.repeating_task:
            self.repeating_task.cancel()
        self.repeating_btn.config(state="normal")
        self.repeating_stop_btn.config(state="disabled")
        self._log("RepeatingExactTask oprit manual")

    def on_close(self):
        for task in (self.interval_task, self.exact_task, self.period_task, self.repeating_task):
            if task:
                task.cancel()
        self.destroy()


def main():
    app = TimerApp()
    app.protocol("WM_DELETE_WINDOW", app.on_close)
    app.mainloop()


if __name__ == "__main__":
    main()
