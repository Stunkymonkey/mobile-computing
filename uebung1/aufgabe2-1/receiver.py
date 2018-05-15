#!/usr/bin/python3

import serial
import time

ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)

WAIT = 0.01

result0 = []
result1 = []
result2 = []
mean0 = 0
mean1 = 0
mean2 = 0


def main():
    next_recv_time = time.time() + (WAIT / 2.0)
    # counter where to store the values
    count = 0
    # amount of values
    output = 0
    while True:
        while time.time() < next_recv_time:
            time.sleep(WAIT * 0.01)
        line = ser.readline()
        next_recv_time += WAIT
        line = 1024 - int(line.rstrip())
        # put the value in the corresponding array
        if (count == 0):
            result0.append(line)
        if (count == 1 or count == 2):
            result1.append(line)
        if (count == 3):
            result2.append(line)
        # print(line)
        count = (count + 1) % 4
        # when having read 1000 values we calculate the border between the
        # means
        if (output == 999):
            if (len(result0) > 0):
                mean0 = sum(result0) / len(result0)
            if (len(result1) > 0):
                mean1 = sum(result1) / len(result1)
            if (len(result2) > 0):
                mean2 = sum(result2) / len(result2)
            print("first border: ", ((mean0 - mean1) / 2) + mean1)
            print("second border:", ((mean1 - mean2) / 2) + mean2)
        output = (output + 1) % 1000


if __name__ == '__main__':
    try:
        main()
    finally:
        ser.close()
