#!/usr/bin/python3
import RPi.GPIO as GPIO
import time
from sys import stdout

GPIO.setmode(GPIO.BCM)
GPIO.setup(17, GPIO.IN)

WAIT = 0.01


def main():
    result = ""
    next_recv_time = time.time() + (WAIT / 2.0)
    while True:
        while time.time() < next_recv_time:
            time.sleep(WAIT * 0.01)
        result += str(int(GPIO.input(17)))
        next_recv_time += WAIT
        # print("recieve: ", result)
        if (len(result) >= 7):
            stdout.write(chr(int(result, 2)))
            stdout.flush()
            # print(chr(int(result, 2)))
            result = ""


if __name__ == '__main__':
    main()
