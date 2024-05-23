# Schedule Library imported
import schedule
import sys
from fsm_irrigation import *


AIO_FEED_IDs = ["scheduler1", "scheduler2", "scheduler3"]
AIO_USERNAME = "tienngo"
AIO_KEY = "aio_crag45SIJwceJtWSksTyWqYR1oKC"
JSONFILE = 'scheduler_data.json'


def connected(client):
    print("Ket noi thanh cong ...")
    for topic in AIO_FEED_IDs:
        client.subscribe(topic)


def subscribe(client, userdata, mid, granted_qos):
    print("Subscribe thanh cong ...")


def disconnected(client):
    print("Ngat ket noi ...")
    sys.exit(1)

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

time.sleep(1)

# Functions setup
def sudo_placement():
    print("Get ready for Sudo Placement at Geeksforgeeks")


def good_luck():
    print("Good Luck for Test")


def work():
    current_time = time.localtime()
    current_hour = current_time.tm_hour
    current_minute = current_time.tm_min
    current_second = current_time.tm_sec
    print("Current time is:", f"{current_hour:02d}:{current_minute:02d}:{current_second:02d}")
    print("Study and work hard")


def bedtime():
    print("It is bed time go rest")


def geeks():
    print("Shaurya says Geeksforgeeks")


# Task scheduling


# Every day at 12am or 00:00 time bedtime() is called.
schedule.every().day.at("00:00").do(bedtime)

# After every 5 to 10mins in between run work()
scheduler_list, isRunning_list = create_schedule_list()


def run_fsm(id):
    if fsm_irrigation_run(scheduler_list[id], client):
        schedule.clear('mytag')


schedule.every(0).seconds.do(run_fsm, 0).tag('mytag')

# Every monday good_luck() is called
schedule.every(10).seconds.do(good_luck)

# Every tuesday at 18:00 sudo_placement() is called
schedule.every().tuesday.at("18:00").do(sudo_placement)

# Loop so that the scheduling task
# keeps on running all time.
while True:
    # Checks whether a scheduled task
    # is pending to run or not
    schedule.run_pending()
    time.sleep(1)
