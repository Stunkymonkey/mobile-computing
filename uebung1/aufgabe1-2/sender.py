#!/usr/bin/python3
import RPi.GPIO as GPIO
import time
import _thread

GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)
GPIO.setup(24, GPIO.OUT)
GPIO.output(23, GPIO.LOW)
GPIO.output(24, GPIO.LOW)

WAIT = 0.01
CHARS = 4


def reset(thread_id):
    if (thread_id == 0):
        GPIO.output(23, GPIO.LOW)
    else:
        GPIO.output(24, GPIO.LOW)


def output(bit, thread_id):
    if (bit == "1"):
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
    next_send_time = time.time() + (CHARS * WAIT * 7 * thread_id)
    bit_c = 0
    index = 0
    counter = 0
    data = str("hello from sender" + str(int(thread_id + 1)))
    byte = get_byte_string(data[index], thread_id)
    while True:
        while time.time() < next_send_time:
            time.sleep(WAIT * 0.01)
        output(byte[bit_c], thread_id)
        next_send_time += WAIT
        # print("send:    ", byte)
        # print("thread:", thread_id, "index:", index, "bit:", bit_c)
        if bit_c >= 6:
            index = (index + 1) % len(data)
            byte = get_byte_string(data[index], thread_id)
        bit_c = (bit_c + 1) % 7
        counter += 1
        if counter >= CHARS * 7:
            time.sleep(WAIT * 0.75)
            reset(thread_id)
            next_send_time = next_send_time + (CHARS * WAIT * 7)
            counter = 0


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
