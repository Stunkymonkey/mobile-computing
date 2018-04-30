#!/usr/bin/python
import RPi.GPIO as GPIO
import time


GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)

SLEEP = 0.1

data = "Hello World!\n"


def main():
    index = 0
    c_i = 0
    while True:
        asc = ord(data[index])
        # print(asc)
        byte = "{0:b}".format(asc)
        while (len(byte) < 7):
            byte = "0" + byte
        # print("sender: " + str(byte))
        if (byte[c_i] == "1"):
            GPIO.output(23, GPIO.LOW)
            # print("send: " + str(1))
        else:
            GPIO.output(23, GPIO.HIGH)
            # print("send: " + str(0))
        time.sleep(SLEEP)
        if c_i == 6:
            index = (index + 1) % len(data)
        c_i = (c_i + 1) % 7


if __name__ == '__main__':
    try:
        main()
    finally:  
        GPIO.cleanup()