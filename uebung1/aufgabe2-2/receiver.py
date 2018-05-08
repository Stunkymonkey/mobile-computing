#!/usr/bin/python3

import serial
import time
from sys import stdout

ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

WAIT = 0.01


def main():
    next_recv_time = time.time() + (WAIT / 2.0)
    import numpy as np
    result1 = ""
    result2 = ""
    tmp = 0
    chiping1 = np.array([1, -1])
    chiping2 = np.array([1, 1])
    array = np.array([0, 0])
    index = 0
    while True:
        while time.time() < next_recv_time:
            time.sleep(WAIT * 0.01)
        line = ser.readline()
        next_recv_time += WAIT
        line = 1024 - int(line.rstrip())
        # print(line)

        if (line >= 377):
            tmp = 2
        elif (line <= 128):
            tmp = -2
        else:
            tmp = 0
        array[index] = tmp
        index = (index + 1) % 2
        # print("line:", line, "tmp:", tmp)
        if index == 0:
            # print(array)
            c_result1 = np.dot(array, chiping1) / 2
            c_result2 = np.dot(array, chiping2) / 2
            c_result1 = (0 if c_result1 == -1 else 1)
            c_result2 = (0 if c_result2 == -1 else 1)
            result1 += str(int(c_result1))
            result2 += str(int(c_result2))
        # print("recieve: ", result)
        if (len(result1) >= 7):
            # stdout.write(chr(int(result1, 2)))
            # stdout.write(chr(int(result2, 2)))
            # stdout.flush()
            print(chr(int(result1, 2)))
            print(chr(int(result2, 2)))
            result1 = ""
            result2 = ""


if __name__ == '__main__':
    try:
        main()
    finally:
        ser.close()
