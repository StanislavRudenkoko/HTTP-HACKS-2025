from fastapi import FastAPI
import threading, time, RPi.GPIO as GPIO, requests

# --- GPIO setup ---
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
TRIG1 = 23
ECHO1 = 24
TRIG2 = 21
ECHO2 = 20
Count = 0
MIN = 20
EmptyCountReset = 20
Full = False
CountStartTime = time.time()


GPIO.setup(TRIG1, GPIO.OUT)
GPIO.setup(ECHO1, GPIO.IN)
GPIO.setup(TRIG2, GPIO.OUT)
GPIO.setup(ECHO2, GPIO.IN)

# --- FastAPI setup ---
app = FastAPI()
DISTANCE = None  # latest distance reading

def measure_distance(trig, echo):
    GPIO.output(trig, True)
    time.sleep(0.00001)
    GPIO.output(trig, False)

    start_time = time.time()
    stop_time = time.time()

    while GPIO.input(echo) == 0:
        start_time = time.time()
    while GPIO.input(echo) == 1:
        stop_time = time.time()

    distance = (stop_time - start_time) * 34300 / 2
    return round(distance, 1)

def getStatus(dist1, dist2):
    global Count, MIN, CountStartTime, EmptyCountReset
    countMax = 5
    print("Count: ",Count)
    if(time.time() - CountStartTime >= EmptyCountReset and Count < countMax):
        Count = 0
        CountStartTime = time.time()

    if (Count >= countMax):
        return 100
    
    if(dist1 <= MIN and dist2 <= MIN):
        Count += 1

    return 0

# --- Background task ---
def distance_updater():
    global DISTANCE, Full
    sleepTime = 1

    while (True):
        try:
            payload = {"id": "2", "status": "0"}
            response = requests.post("https://dd4568ea74c4.ngrok-free.app/data", json=payload)
            print("Server responded with:", response.status_code, response.text)
            break

        except Exception as e:
            print("Failed to send data:", e)

        time.sleep(1)

    while True:
        dist1 = measure_distance(TRIG1, ECHO1)
        dist2 = measure_distance(TRIG2, ECHO2)

        DISTANCE = round((dist1 + dist2) / 2, 2)
        print(f"Sensor1: {dist1} cm,")
        print(f"Sensor2: {dist2} cm,")

        try:
            status = getStatus(dist1, dist2)
            payload = {"id": "2", "status": status}
            print("Status: ",status)

            if(status == 100):
                response = requests.post("https://dd4568ea74c4.ngrok-free.app/data", json=payload)
                print("Server responded with:", response.status_code, response.text)
                sleepTime = 20


        except Exception as e:
            print("Failed to send data:", e)

        time.sleep(sleepTime)

# --- Startup event to launch background thread ---
@app.on_event("startup")
def start_background_task():
    thread = threading.Thread(target=distance_updater, daemon=True)
    thread.start()

@app.get("/distance")
def get_distance():
    return {"distance_cm": DISTANCE}
