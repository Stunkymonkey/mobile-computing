#!/usr/bin/python3
import RPi.GPIO as GPIO
import time
import _thread

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)
GPIO.setup(24, GPIO.OUT)

WAIT = 0.01


def reset(thread_id):
    if (thread_id == 0):
        GPIO.output(23, GPIO.LOW)
    else:
        GPIO.output(24, GPIO.LOW)


def output(bit, thread_id):
    if (bit == 1):
        if (thread_id == 0):
            GPIO.output(23, GPIO.LOW)
        else:
            GPIO.output(24, GPIO.LOW)
        # print("id:", offset, 1)
    else:
        if (thread_id == 0):
            GPIO.output(23, GPIO.HIGH)
        else:
            GPIO.output(24, GPIO.HIGH)
        # print("id:", offset, 0)


def get_byte_string(char, thread_id):
    if (thread_id == 1):
        char = char.upper()
    asc = ord(char)
    byte = "{0:b}".format(asc)
    while (len(byte) < 7):
        byte = "0" + byte
    return byte


def send(thread_id):
    next_send_time = time.time()
    reset(thread_id)
    bit_c = 0
    index = 0
    chip_counter = 0
    chiping = ([1, 0] if thread_id else [1, 1])
    data = str("hello from sender" + str(int(thread_id + 1)))
    byte = get_byte_string(data[index], thread_id)
    while True:
        while time.time() < next_send_time:
            time.sleep(WAIT * 0.01)
        bit = int(byte[bit_c] == "1") ^ chiping[chip_counter]
        output(bit, thread_id)
        next_send_time += WAIT
        # print("send:    ", byte, bit)
        # print("thread:", thread_id, "index:", index, "bit:", bit_c, "byte:", byte, "bit:", bit)
        if bit_c >= 6 and chip_counter == 1:
            index = (index + 1) % len(data)
            byte = get_byte_string(data[index], thread_id)
        chip_counter = (chip_counter + 1) % 2
        if chip_counter == 0:
            bit_c = (bit_c + 1) % 7


def main():
    try:
        _thread.start_new_thread(send, (0,))
        _thread.start_new_thread(send, (1,))
    except Exception as err:
        print(err)
        print("Error: unable to start thread")

    while 1:
        time.sleep(0.1)


if __name__ == '__main__':
    try:
        main()
    finally:
        GPIO.cleanup()
