import time
from rs485 import *
from schedule_irrigation_class import *

IDLE = 0
MIXER1 = 1
MIXER2 = 2
MIXER3 = 3
PUMP_IN = 4
SELECTOR = 5
PUMP_OUT = 6
NEXT_CYCLE = 7

status = IDLE
cycle = 0


def fsm_irrigation_run(irrigation_sched):
    current_time = time.localtime()
    current_hour = current_time.tm_hour
    current_minute = current_time.tm_min
    current_second = current_time.tm_sec
    print("Current time is:", f"{current_hour:02d}:{current_minute:02d}:{current_second:02d}")

    global status, cycle

    if status == IDLE:
        print("IDLE")
        status = MIXER1
    elif status == MIXER1:
        print("CYCLE: " + str(cycle))
        print("MIXER1")
        time.sleep(irrigation_sched.flow1)
        status = MIXER2
    elif status == MIXER2:
        print("MIXER2")
        time.sleep(irrigation_sched.flow2)
        status = MIXER3
    elif status == MIXER3:
        print("MIXER3")
        time.sleep(irrigation_sched.flow3)
        status = PUMP_IN
    elif status == PUMP_IN:
        print("PUMP_IN")
        time.sleep(2)
        status = SELECTOR
    elif status == SELECTOR:
        print("SELECTOR")
        status = PUMP_OUT
    elif status == PUMP_OUT:
        print("PUMP_OUT")
        time.sleep(2)
        status = NEXT_CYCLE
    elif status == NEXT_CYCLE:
        cycle += 1
        if cycle >= irrigation_sched.cycle:
            status = IDLE
            cycle = 0
            print("THE END")
        else:
            status = MIXER1
            print("NEXT_CYCLE")
