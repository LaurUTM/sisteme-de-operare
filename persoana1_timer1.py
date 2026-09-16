import tkinter as tk
from tkinter import messagebox


class TimerInterval(tk.LabelFrame):
    def __init__(self, parent):
        super().__init__(
            parent,
            text="Timer 1 - Interval",
            padx=20,
            pady=15,
            font=("Arial", 12, "bold")
        )

        self.running = False
        self.remaining = 0
        self.after_id = None

        tk.Label(
            self,
            text="Interval în secunde:"
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
            if self.remaining == 0:
                self.remaining = int(self.entry.get())

            if self.remaining <= 0:
                raise ValueError

            self.running = True
            self.update_timer()

        except ValueError:
            messagebox.showerror(
                "Eroare",
                "Introduceți un număr pozitiv de secunde."
            )

    def update_timer(self):
        if not self.running:
            return

        minutes = self.remaining // 60
        seconds = self.remaining % 60

        self.label.config(
            text=f"{minutes:02d}:{seconds:02d}"
        )

        if self.remaining == 0:
            self.running = False

            messagebox.showinfo(
                "Timer 1",
                "Intervalul de timp a expirat!"
            )

            return

        self.remaining -= 1

        self.after_id = self.after(
            1000,
            self.update_timer
        )

    def stop(self):
        self.running = False

        if self.after_id is not None:
            self.after_cancel(self.after_id)
            self.after_id = None

    def reset(self):
        self.stop()

        self.remaining = 0

        self.label.config(
            text="00:00"
        )