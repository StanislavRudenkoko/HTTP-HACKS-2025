# This is the code for raspberry pi4

from fastapi import FastAPI
import threading, time, RPi.GPIO as GPIO, requests

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
TRIG = 23
ECHO = 24
GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)

app = FastAPI()
DISTANCE = None  # store latest distance

def measure_distance():
    GPIO.output(TRIG, True)
    time.sleep(0.00001)
    GPIO.output(TRIG, False)
    start_time = time.time()
    stop_time = time.time()
    while GPIO.input(ECHO) == 0:
        start_time = time.time()
    while GPIO.input(ECHO) == 1:
        stop_time = time.time()
    distance = (stop_time - start_time) * 34300 / 2
    return round(distance, 1)

# Background task
def distance_updater():
    global DISTANCE
    while True:
        dist = measure_distance()
        
        DISTANCE = dist
        print(f"New distance: {DISTANCE} cm")
        
        time.sleep(0.05)

@app.on_event("startup")
def start_background_task():
    thread = threading.Thread(target=distance_updater, daemon=True)
    thread.start()

@app.get("/distance")
def get_distance():
    return {"distance_cm": DISTANCE}
