#!/usr/bin/python3
import RPi.GPIO as GPIO
import time
import _thread

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)
GPIO.setup(24, GPIO.OUT)
GPIO.output(23, GPIO.LOW)
GPIO.output(24, GPIO.LOW)

WAIT = 0.01


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


def send(thread_id):
    next_send_time = time.time()
    state = True
    while True:
        while time.time() < next_send_time:
            time.sleep(WAIT * 0.01)
        output(str(int(state)), thread_id)
        # print("thread:", thread_id, "state:", state)
        state = not state
        # thread 0 is 2 times faster then thread 1
        # because we want to reach all states
        next_send_time = next_send_time + WAIT + (thread_id * WAIT)


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
