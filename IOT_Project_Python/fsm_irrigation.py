import time
from rs485 import *
from schedule_irrigation_class import *
from Adafruit_IO import MQTTClient

IDLE = 0
MIXER1 = 1
MIXER2 = 2
MIXER3 = 3
PUMP_IN = 4
SELECTOR = 5
PUMP_OUT = 6
NEXT_CYCLE = 7
END = 8

TIME_IRRIGATION = 10

status = IDLE
cycle = 0
count = 0


def fsm_irrigation_run(irrigation_sched, client):
    # current_time = time.localtime()
    # current_hour = current_time.tm_hour
    # current_minute = current_time.tm_min
    # current_second = current_time.tm_sec
    # print("Current time is:", f"{current_hour:02d}:{current_minute:02d}:{current_second:02d}")

    global status, cycle, count

    if status == IDLE:
        print("IDLE")
        status = MIXER1
        count = irrigation_sched.flow1
        print("CYCLE: " + str(cycle))
        print("MIXER1")
        setDeviceON(1)
        publish_notification(client, irrigation_sched.id)
    elif status == MIXER1:
        if count <= 0:
            setDeviceOFF(1)
            count = irrigation_sched.flow2
            status = MIXER2
            print("MIXER2")
            setDeviceON(2)
            publish_notification(client, irrigation_sched.id)
    elif status == MIXER2:
        if count <= 0:
            setDeviceOFF(2)
            count = irrigation_sched.flow3
            status = MIXER3
            print("MIXER3")
            setDeviceON(3)
            publish_notification(client, irrigation_sched.id)
    elif status == MIXER3:
        if count <= 0:
            setDeviceOFF(3)
            count = TIME_IRRIGATION
            status = PUMP_IN
            print("PUMP_IN")
            setDeviceON(7)
            publish_notification(client, irrigation_sched.id)
    elif status == PUMP_IN:
        if count <= 0:
            setDeviceOFF(7)
            status = SELECTOR
            print("SELECTOR")
            if irrigation_sched.area == -1:
                area_selector(cycle % 3)
                publish_notification(client, irrigation_sched.id, str(cycle % 3))
            else:
                area_selector(irrigation_sched.area)
                publish_notification(client, irrigation_sched.id, str(irrigation_sched.area))
    elif status == SELECTOR:
        count = TIME_IRRIGATION
        status = PUMP_OUT
        print("PUMP_OUT")
        setDeviceON(8)
        publish_notification(client, irrigation_sched.id)
    elif status == PUMP_OUT:
        if count <= 0:
            setDeviceOFF(8)
            status = NEXT_CYCLE
            print("NEXT_CYCLE")
            publish_notification(client, irrigation_sched.id)
    elif status == NEXT_CYCLE:
        cycle += 1
        if cycle >= irrigation_sched.cycle:
            print("THE END")
            status = END
            publish_notification(client, irrigation_sched.id)
            status = IDLE
            cycle = 0
            return True
        else:
            status = MIXER1
            count = irrigation_sched.flow1
            print("CYCLE: " + str(cycle))
            print("MIXER1")
            setDeviceON(1)
            publish_notification(client, irrigation_sched.id)

    count -= 1


def area_selector(area):
    if area == 0:
        print('AREA0')
        setDeviceON(4)
        setDeviceOFF(5)
        setDeviceOFF(6)
    elif area == 1:
        print('AREA1')
        setDeviceOFF(4)
        setDeviceON(5)
        setDeviceOFF(6)
    elif area == 2:
        print('AREA2')
        setDeviceOFF(4)
        setDeviceOFF(5)
        setDeviceON(6)


def publish_notification(client, id, extra=''):
    global status, cycle
    message = f"{id},{cycle},{status},{extra}"
    client.publish("notification", message)  # Adjust the feed/topic name as needed

