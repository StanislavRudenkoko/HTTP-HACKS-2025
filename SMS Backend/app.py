from twilio.rest import Client
from twilio.twiml.messaging_response import MessagingResponse
from flask import Flask, request
from dotenv import load_dotenv
import os, psycopg, re, threading, atexit
import time
from routing_script import fetch_full_trashcans

load_dotenv()

help_message = """
Type a command name followed by arguments to run a command (ex. SHOW ALL)\n
? SHOW - Options for show command
? SUBSCRIPTIONS - Options for subscriptions
"""

show_help_message = """
SHOW:
ALL - Displays the status of all bins
{BUILDING} - Displays the status of all bins in that building
"""

subscriptions_help_message = """
SUBSCRIPTIONS:
SHOW - Displays bins you are subscribed to
ADD ID {BIN_ID} - Subscribe to a bin
ADD BUILDING {BUILDING} - Subscribe to all bins in that building
DELETE ALL - Unsubscribe from all bins
DELETE {BIN_ID} - Unsubscribe from a bin
"""

account_sid = os.environ["TWILIO_ACCOUNT_SID"]
auth_token = os.environ["TWILIO_AUTH_TOKEN"]

client = Client(account_sid, auth_token)
app = Flask(__name__)


db = psycopg.connect(
    host =       os.environ["SQL_HOST"],
    port =       os.environ["SQL_PORT"],
    dbname =     'trash_db',
    user =       os.environ["SQL_USER"],
    password =   os.environ["SQL_PASSWORD"]
)

cur = db.cursor()

cur.execute("SET search_path TO smart_trashcan;")


exit_event = threading.Event()
def send_notifs():
    while not exit_event.is_set():
        for trashcan in fetch_full_trashcans(db):
            time_since_last_update = time.time() - trashcan[7].timestamp()
            if time_since_last_update < 1200:
                continue
            
            cur.execute(f"UPDATE trashcans SET last_updated=NOW() WHERE id={trashcan[0]}")
            cur.execute(f"SELECT phone FROM subscriptions WHERE trashcan_id='{trashcan[0]}'")
            phones = cur.fetchall()
            for phone in phones:
                client.messages.create(
                    body=f"Trashcan {trashcan[1]} is full!",
                    from_="+19302033111",
                    to=phone[0].replace("\\", "")
                )
        
        time.sleep(20)
        
# Start the messaging loop
x = threading.Thread(target=send_notifs, daemon=True)
x.start()


def stop_server():
    exit_event.set()
    x.join()
atexit.register(stop_server)

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=False)

def send_sql(resp: MessagingResponse) -> None:
    message = ""
    for trashcan in cur.fetchall():
        id = trashcan[0]
        name = trashcan[1]
        status = trashcan[3]
        
        message += f"{id}. Trashcan {name} is currently {status}.\n"
        
    resp.message(message)

# Handles everything for receiving sms messages
@app.route("/sms", methods=["GET", "POST"])
def sms_reply() -> str:

    body = request.values.get('Body', None).lower().split()
    resp = MessagingResponse()
    
    match body[0]:
        case "?":
            resp.message(help_message)
        case "show":
            if len(body) == 1:
                resp.message(show_help_message)
                return str(resp)
            
            match body[1]:
                # Show all trashcans
                case "all":
                    cur.execute("SELECT * FROM trashcans;")
                    send_sql(resp)
                # Show all trashcans in a specified location
                case _:
                    cur.execute(f"SELECT * FROM trashcans WHERE LOWER(location) LIKE '%{re.escape(body[1])}%';")
                    send_sql(resp)
        
        case "subscriptions":
            if len(body) == 1:
                resp.message(subscriptions_help_message)
                return str(resp)
            
            phone = request.values.get("From", None)
            match body[1]:
                # Show all subscriptions
                case "show":
                    cur.execute(f"SELECT trashcan_id, name, status FROM subscriptions JOIN trashcans ON subscriptions.phone='{phone}';")
                    send_sql(resp)
                    
                case "add":
                    argument = int(body[3])
                    match body[2]:
                        # Add a bin by ID
                        case "id":
                            cur.execute(f"INSERT INTO subscriptions(trashcan_id, phone) VALUES ({argument}, {phone});")
                            resp.message(f"Subscription for trashcan {argument} added successfully.")
                        # Add all bins in a building
                        case "building":
                            subscriptions_added = 0
                            cur.execute(f"SELECT id FROM trashcans WHERE LOWER(location) LIKE '%{argument}%';")
                            trashcans = cur.fetchall()
                            for trashcan in trashcans:
                                cur.execute(f"INSERT INTO subscriptions(trashcan_id, phone) VALUES ({trashcan[0]}, '{phone})';")
                                subscriptions_added += 1
                                
                            resp.message(f"{subscriptions_added} subscriptions for building {argument} added successfully.")
                
                case "delete":
                    match body[2]:
                        # Remove subscription from all bins
                        case "all":
                            cur.execute(f"DELETE FROM subscriptions WHERE phone='{phone}';")
                        # Remove subscription from a specific bin
                        case _:
                            cur.execute(f"DELETE FROM subscriptions WHERE phone='{phone}' AND trashcan_id={re.escape(body[2])}")
                            
        # Unrecognized message
        case _:
            resp.message("Sorry, I didn't understand that. Please make sure you are typing your command correctly, or type ? to see a list of options.")

    return str(resp)

# Handles everything for receiving data from bins
@app.route("/data", methods={"GET", "POST"})
def data_fetch() -> str:
    body = request.get_json()
    
    id = int(body['id'])
    status = int(body['status'])
    
    print(f"{id}, {status}")
    
    cur.execute(f"UPDATE trashcans SET status={status} WHERE id={id};")
    
    return "Data was succesfully stored."

