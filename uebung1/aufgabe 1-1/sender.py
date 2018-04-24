#!/usr/bin/python
import RPi.GPIO as GPIO
import time


GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)

SLEEP = 1
time.sleep(SLEEP)

data = "Hello World!"


def main():
    index = 0
    c_i = 0
    while True:
        asc = ord(data[index])
        byte = "{0:b}".format(asc)
        if (byte[c_i] == "1"):
            GPIO.output(23, GPIO.HIGH)
            # print(1)
        else:
            GPIO.output(23, GPIO.LOW)
            # print(0)
        time.sleep(SLEEP)
        if c_i == 6:
            index = (index + 1) % len(data)
        c_i = (c_i + 1) % 7


if __name__ == '__main__':
    main()
