
import tkinter as tk

from persoana1_timer1 import TimerInterval
from persoana1_timer2 import TimerPeriodic
from persoana2_timer3 import TimerOra
from persoana2_timer4 import TimerCountdown


class TimerApplication:
    def __init__(self, root):
        self.root = root

        self.root.title("Planificarea activității proceselor")
        self.root.geometry("900x600")
        self.root.resizable(False, False)

        title = tk.Label(
            root,
            text="Sistem de planificare cu timere",
            font=("Arial", 24, "bold")
        )
        title.pack(pady=20)

        subtitle = tk.Label(
            root,
            text="Laboratorul 1 - Timer",
            font=("Arial", 13)
        )
        subtitle.pack()

        timers_frame = tk.Frame(root)
        timers_frame.pack(pady=25)

        timer1 = TimerInterval(timers_frame)
        timer1.grid(row=0, column=0, padx=15, pady=15)

        timer2 = TimerPeriodic(timers_frame)
        timer2.grid(row=0, column=1, padx=15, pady=15)

        timer3 = TimerOra(timers_frame)
        timer3.grid(row=1, column=0, padx=15, pady=15)

        timer4 = TimerCountdown(timers_frame)
        timer4.grid(row=1, column=1, padx=15, pady=15)

        footer = tk.Label(
            root,
            text="Aplicație realizată în Python",
            font=("Arial", 10)
        )
        footer.pack(side=tk.BOTTOM, pady=15)


root = tk.Tk()

app = TimerApplication(root)

root.mainloop()
