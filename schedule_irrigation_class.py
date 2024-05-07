import json

JSONFILE = 'scheduler_data.json'

class schedule_irrigation:
    id = 0
    cycle = 0
    flow1 = 0
    flow2 = 0
    flow3 = 0
    isActive = False
    schedulerName = ''
    startTime = ''
    stopTime = ''

    def __init__(self, id):
        self.id = id
    def set_schedule(self):
        with open(JSONFILE, 'r', encoding='utf-8') as file:
            data = json.load(file)
            if self.id < len(data):
                schedule_data = data[self.id]
                self.cycle = schedule_data["cycle"]
                self.flow1 = schedule_data["flow1"]
                self.flow2 = schedule_data["flow2"]
                self.flow3 = schedule_data["flow3"]
                self.isActive = schedule_data["isActive"]
                self.schedulerName = schedule_data["schedulerName"]
                self.startTime = schedule_data["startTime"]
                self.stopTime = schedule_data["stopTime"]
            else:
                print("Invalid schedule.")

    def print_data(self):
        print("Scheduler Name:", self.schedulerName)
        print("Cycle:", self.cycle)
        print("Flow 1:", self.flow1)
        print("Flow 2:", self.flow2)
        print("Flow 3:", self.flow3)
        print("Is Active:", self.isActive)
        print("Start Time:", self.startTime)
        print("Stop Time:", self.stopTime)


def create_schedule_list():
    scheduler_list = []
    isRunning_list = []
    with open(JSONFILE, 'r', encoding='utf-8') as file:
        data = json.load(file)
        for idx in range(len(data)):
            scheduler_obj = schedule_irrigation(idx)
            scheduler_obj.set_schedule()
            scheduler_list.append(scheduler_obj)
            isRunning_list.append(False)
    return scheduler_list, isRunning_list