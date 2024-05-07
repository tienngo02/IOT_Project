# Schedule Library imported
import schedule
import time


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
schedule.every(0).seconds.do(work)

# Every monday good_luck() is called
schedule.every().monday.do(good_luck)

# Every tuesday at 18:00 sudo_placement() is called
schedule.every().tuesday.at("18:00").do(sudo_placement)

# Loop so that the scheduling task
# keeps on running all time.
while True:
    # Checks whether a scheduled task
    # is pending to run or not
    schedule.run_pending()
    time.sleep(1)
