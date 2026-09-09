import tkinter as tk
from timer import CountdownTimer
from timer2 import Stopwatch


def main():
  root = tk.Tk()
  app = CountdownTimer(root)
  app2 = Stopwatch(root)
  root.mainloop()


if __name__ == "__main__":
  main()