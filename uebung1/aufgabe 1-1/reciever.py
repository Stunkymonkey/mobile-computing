#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import random

GPIO.setmode(GPIO.BCM)
GPIO.setup(17, GPIO.IN)

SLEEP = 1

time.sleep(SLEEP / 2)


def read():
    return bool(random.getrandbits(1))


def main():
    result = ""
    while True:
        result += str(int(GPIO.input(17)))
        # result += str(int(read()))
        time.sleep(SLEEP)
        if (len(result) == 7):
            print(chr(int(result, 2)))
            result = ""


if __name__ == '__main__':
    main()
