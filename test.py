#!/usr/bin/env python3
"""Demo Sisteme de operare: procese (fork), pipe si thread-uri."""

import os
import sys
import threading
import time


def demo_fork():
    """Creeaza un proces copil si asteapta terminarea lui."""
    print("[fork] parinte PID =", os.getpid())
    sys.stdout.flush()  # altfel copilul mosteneste buffer-ul si retipareste linia

    pid = os.fork()
    if pid == 0:
        print("[fork] copil   PID =", os.getpid(), "PPID =", os.getppid())
        time.sleep(0.5)
        sys.stdout.flush()  # os._exit nu goleste buffer-ele
        os._exit(7)

    finished_pid, status = os.waitpid(pid, 0)
    print(f"[fork] copilul {finished_pid} s-a terminat cu codul {os.WEXITSTATUS(status)}")


def demo_pipe():
    """Comunicare intre parinte si copil printr-un pipe anonim."""
    r_fd, w_fd = os.pipe()

    if os.fork() == 0:
        os.close(r_fd)
        with os.fdopen(w_fd, "w") as w:
            w.write("mesaj de la copil\n")
        os._exit(0)

    os.close(w_fd)
    with os.fdopen(r_fd) as r:
        print("[pipe] parintele a citit:", r.readline().strip())
    os.wait()


def demo_threads():
    """Doua thread-uri incrementeaza un contor partajat, protejat de un lock."""
    counter = 0
    lock = threading.Lock()

    def worker(name, steps):
        nonlocal counter
        for _ in range(steps):
            with lock:
                counter += 1
        print(f"[thread] {name} a terminat {steps} pasi")

    threads = [
        threading.Thread(target=worker, args=(f"T{i}", 100_000))
        for i in range(2)
    ]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    print("[thread] contor final =", counter)


def main():
    if not hasattr(os, "fork"):
        print("fork() nu este disponibil pe acest sistem", file=sys.stderr)
        return 1

    demo_fork()
    demo_pipe()
    demo_threads()
    return 0


if __name__ == "__main__":
    sys.exit(main())
