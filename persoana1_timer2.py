import tkinter as tk
from tkinter import messagebox


class TimerPeriodic(tk.LabelFrame):
    def __init__(self, parent):
        super().__init__(
            parent,
            text="Timer 2 - Periodic",
            padx=20,
            pady=15,
            font=("Arial", 12, "bold")
        )

        self.running = False
        self.elapsed = 0
        self.period = 0
        self.after_id = None

        tk.Label(
            self,
            text="Perioada în secunde:"
        ).pack()

        self.entry = tk.Entry(
            self,
            width=15,
            justify="center"
        )
        self.entry.pack(pady=8)

        self.label = tk.Label(
            self,
            text="00:00",
            font=("Arial", 25, "bold")
        )
        self.label.pack(pady=8)

        self.status = tk.Label(
            self,
            text="Oprit"
        )
        self.status.pack()

        buttons = tk.Frame(self)
        buttons.pack(pady=5)

        tk.Button(
            buttons,
            text="Start",
            width=8,
            command=self.start
        ).pack(side=tk.LEFT, padx=3)

        tk.Button(
            buttons,
            text="Stop",
            width=8,
            command=self.stop
        ).pack(side=tk.LEFT, padx=3)

        tk.Button(
            buttons,
            text="Reset",
            width=8,
            command=self.reset
        ).pack(side=tk.LEFT, padx=3)

    def start(self):
        if self.running:
            return

        try:
            if self.period == 0:
                self.period = int(self.entry.get())

            if self.period <= 0:
                raise ValueError

            self.running = True
            self.status.config(text="Rulează")

            self.update_timer()

        except ValueError:
            messagebox.showerror(
                "Eroare",
                "Introduceți o perioadă pozitivă."
            )

    def update_timer(self):
        if not self.running:
            return

        self.elapsed += 1

        minutes = self.elapsed // 60
        seconds = self.elapsed % 60

        self.label.config(
            text=f"{minutes:02d}:{seconds:02d}"
        )

        if self.elapsed % self.period == 0:
            messagebox.showinfo(
                "Timer 2",
                f"Acțiune periodică executată!\n"
                f"Perioada: {self.period} secunde."
            )

        self.after_id = self.after(
            1000,
            self.update_timer
        )

    def stop(self):
        self.running = False
        self.status.config(text="Oprit")

        if self.after_id is not None:
            self.after_cancel(self.after_id)
            self.after_id = None

    def reset(self):
        self.stop()

        self.elapsed = 0
        self.period = 0

        self.label.config(
            text="00:00"
        )

        self.status.config(
            text="Oprit"
        )