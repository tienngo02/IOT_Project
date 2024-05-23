import sys
from time import struct_time
from Adafruit_IO import MQTTClient
import time
from fsm_irrigation import *
import json
import schedule

# create a sample json

# a = {"cycle": 5,
#     "flow1": 20,
#     "flow2": 10,
#     "flow3": 20,
#     "area": 0,
#     "isActive": True,
#     "schedulerName": "LỊCH TƯỚI 1",
#     "startTime": "18:30",
#     "stopTime": "18:40"
# }

# Convert JSON to String

# y = json.dumps(a, ensure_ascii=False)

AIO_FEED_IDs = ["scheduler1", "scheduler2", "scheduler3"]
AIO_USERNAME = "tienngo"
AIO_KEY = "aio_crag45SIJwceJtWSksTyWqYR1oKC"
JSONFILE = 'scheduler_data.json'
FSM_TASK_TAG = 'fsm_task_tag'

scheduler_list, is_running_list = create_schedule_list()

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

    client.publish("check", payload)


client = MQTTClient(AIO_USERNAME, AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()


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


def run_schedule_fsm(id):
    if fsm_irrigation_run(scheduler_list[id], client):
        schedule.clear(FSM_TASK_TAG)
        is_running_list[id] = False


def check_scheduler_time():
    if not scheduler_list:
        print("The schedule list is empty.")
        return

    current_time = time.localtime()
    current_hour = current_time.tm_hour
    current_minute = current_time.tm_min

    for idx in range(len(scheduler_list)):
        start_hour, start_minute = map(int, scheduler_list[idx].startTime.split(':'))
        stop_hour, stop_minute = map(int, scheduler_list[idx].stopTime.split(':'))
        if not is_running_list[idx]:
            if current_hour == start_hour and current_minute == start_minute:
                if scheduler_list[idx].isActive:
                    print(f"Scheduler '{scheduler_list[idx].schedulerName}' is active now.")
                    schedule.every(0).seconds.do(run_schedule_fsm, idx).tag(FSM_TASK_TAG)
                    is_running_list[idx] = True
                else:
                    print(f"Scheduler '{scheduler_list[idx].schedulerName}' is not active.")
                    is_running_list[idx] = False

        if current_hour == stop_hour and current_minute == stop_minute:
            if is_running_list[idx]:
                print(f"Scheduler '{scheduler_list[idx].schedulerName}' is stop now.")
                global cycle, status
                status = IDLE
                cycle = 0
                schedule.clear(FSM_TASK_TAG)
                is_running_list[idx] = False


schedule.every(30).seconds.do(check_scheduler_time)
schedule.every(7).seconds.do(read_serial_sensor, client)

while True:
    schedule.run_pending()
    time.sleep(1)
