#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import _thread


GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)

SLEEP = 0.1
CHARS = 4
time.sleep(SLEEP)

data = "Hello World!\n"


def send(offset):
    time.sleep(CHARS * offset)
    index = 0
    c_i = 0
    counter = 0
    while True:
        char = data[index]
        if (offset == 1):
            char = char.upper()
        asc = ord(char)
        byte = "{0:b}".format(asc)
        while (len(byte) < 7):
            byte = "0" + byte
        if (byte[c_i] == "1"):
            GPIO.output(23, GPIO.HIGH)
            # print("id:", offset, 1)
        else:
            GPIO.output(23, GPIO.LOW)
            # print("id:", offset, 0)
        time.sleep(SLEEP)
        if c_i == 6:
            index = (index + 1) % len(data)
        c_i = (c_i + 1) % 7
        counter += 1
        if (counter == CHARS):
            time.sleep(CHARS * SLEEP)
            counter = 0


def main():
    try:
        _thread.start_new_thread(send, (0,))
        _thread.start_new_thread(send, (1,))
    except Exception as err:
        print(err)
        print("Error: unable to start thread")

    while 1:
        pass


if __name__ == '__main__':
    try:
        main()
    finally:
        GPIO.cleanup()
