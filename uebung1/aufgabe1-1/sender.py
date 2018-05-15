#!/usr/bin/python
import RPi.GPIO as GPIO
import time


GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)

WAIT = 0.01

data = "Hello World!\n"


def output(bit):
    if (bit == "1"):
        GPIO.output(23, GPIO.LOW)
        # print("send: " + str(1))
    else:
        GPIO.output(23, GPIO.HIGH)
        # print("send: " + str(0))


def get_byte_string(char):
    # ascii integer
    asc = ord(char)
    # print(asc)
    byte = "{0:b}".format(asc)
    # prepend missing zeros
    while (len(byte) < 7):
        byte = "0" + byte
    return byte


def main():
    next_send_time = time.time()
    # index of byte in chars
    index = 0
    # index of bit in byte
    c_i = 0
    while True:
        byte = get_byte_string(data[index])
        # print("sender: " + str(byte))
        while time.time() < next_send_time:
            time.sleep(WAIT * 0.01)
        output(byte[c_i])
        next_send_time += WAIT
        if c_i == 6:
            index = (index + 1) % len(data)
        c_i = (c_i + 1) % 7


if __name__ == '__main__':
    try:
        main()
    finally:
        GPIO.cleanup()
