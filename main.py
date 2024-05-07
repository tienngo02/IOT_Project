import sys
from time import struct_time
from Adafruit_IO import MQTTClient
import time
import random
from fsm_irrigation import *
import json
import sched

# create a sample json

# a = {"cycle": 5,
#     "flow1": 20,
#     "flow2": 10,
#     "flow3": 20,
#     "isActive": True,
#     "schedulerName": "LỊCH TƯỚI 1",
#     "startTime": "18:30",
#     "stopTime": "18:40"
# }

# Convert JSON to String

# y = json.dumps(a, ensure_ascii=False)

AIO_FEED_IDs = ["scheduler1", "scheduler2", "scheduler3"]
AIO_USERNAME = "tienngo"
AIO_KEY = "aio_jfJp97YlbDLMvuDLFoNSWRzwBEkU"
JSONFILE = 'scheduler_data.json'

scheduler_list, isRunning_list = create_schedule_list()


def write_JSON_file(id, payload):
    try:
        data = None
        with open(JSONFILE, 'r', encoding='utf-8') as file:
            data = json.load(file)
            if id < len(data):
                if payload:
                    try:
                        data[id] = json.loads(payload)
                    except json.JSONDecodeError as e:
                        print("Error decoding payload JSON:", e)
                else:
                    print("Payload is empty.")
            else:
                print("Invalid schedule ID.")
        if data:
            with open(JSONFILE, 'w', encoding='utf-8') as file:
                json.dump(data, file, indent=4)
    except json.JSONDecodeError as e:
        print("Error decoding JSON:", e)


def check_scheduler_time():
    if not scheduler_list:
        print("The schedule list is empty.")
        return

    current_time = time.localtime()
    current_hour = current_time.tm_hour
    current_minute = current_time.tm_min
    print("Current time is:", f"{current_hour:02d}:{current_minute:02d}")

    for idx in range(len(scheduler_list)):
        start_hour, start_minute = map(int, scheduler_list[idx].startTime.split(':'))
        stop_hour, stop_minute = map(int, scheduler_list[idx].stopTime.split(':'))
        if not isRunning_list[idx]:
            if current_hour == start_hour and current_minute == start_minute:
                if scheduler_list[idx].isActive:
                    print(f"Scheduler '{scheduler_list[idx].schedulerName}' is active now.")
                    isRunning_list[idx] = True
                    # Call function to execute the schedule here
                else:
                    print(f"Scheduler '{scheduler_list[idx].schedulerName}' is not active.")
                    isRunning_list[idx] = False

        if current_hour == stop_hour and current_minute == stop_minute:
            if isRunning_list[idx]:
                print(f"Scheduler '{scheduler_list[idx].schedulerName}' is stop now.")
                isRunning_list[idx] = False
                # Call function to stop the schedule here


def connected(client):
    print("Ket noi thanh cong ...")
    for topic in AIO_FEED_IDs:
        client.subscribe(topic)


def subscribe(client, userdata, mid, granted_qos):
    print("Subscribe thanh cong ...")


def disconnected(client):
    print("Ngat ket noi ...")
    sys.exit(1)


def message(client, feed_id, payload):
    print("Nhan du lieu: " + payload + ", feed id:" + feed_id)
    if feed_id == "scheduler1":
        write_JSON_file(0, payload)
        scheduler_list[0].set_schedule()
        scheduler_list[0].print_data()
    if feed_id == "scheduler2":
        write_JSON_file(1, payload)
        scheduler_list[1].set_schedule()
        scheduler_list[1].print_data()
    if feed_id == "scheduler3":
        write_JSON_file(2, payload)
        scheduler_list[2].set_schedule()
        scheduler_list[2].print_data()


client = MQTTClient(AIO_USERNAME, AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()

while True:
    # counter = counter - 1
    # if counter <=0:
    #     counter = 10
    #     #todo
    #     print("Random data is publishing...")
    #     if sensor_type == 0:
    #         print("Temperature...")
    #         temp = random.randint(10, 20)
    #         client.publish("sensor1", temp)
    #         sensor_type = 1
    #     elif sensor_type == 1:
    #         print("Humidity...")
    #         humi = random.randint(50, 70)
    #         client.publish("sensor2", humi)
    #         sensor_type = 2
    #     elif sensor_type == 2:
    #         print("Light...")
    #         light = random.randint(100, 500)
    #         client.publish("sensor3", light)
    #         sensor_type = 0


    # readSerial(client)
    fsm_irrigation_run(scheduler_list[0])

    # time.sleep(1)
    # client.publish("button1", y)
