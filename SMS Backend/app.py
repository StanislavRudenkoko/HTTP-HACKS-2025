from twilio.rest import Client
from twilio.twiml.messaging_response import MessagingResponse
from flask import Flask, request
from dotenv import load_dotenv
import os, psycopg, re, threading, atexit, time, datetime
from routing_script import get_route_text_from_cursor

load_dotenv()

help_message = """
Type a command name followed by arguments to run a command (ex. SHOW SE2)\n
SHOW - Options for show command
SUBSCRIPTIONS - Options for subscriptions
ROUTE - Options for routes
"""

show_help_message = """
SHOW:
ALL - Displays the status of all bins
BUILDING {BUILDING} - Displays the status of all bins in that building
{ID} - Displays status of the bin
"""

subscriptions_help_message = """
SUBSCRIPTIONS:
SHOW - Displays bins you are subscribed to
ADD {BIN_ID} - Subscribe to a bin
ADD BUILDING {BUILDING} - Subscribe to all bins in that building
DELETE ALL - Unsubscribe from all bins
DELETE {BIN_ID} - Unsubscribe from a bin
"""

routing_help_message = """
ROUTE:
ALL - Displays optimal route for all filled trashcans
{BUILDING} - Displays optimal route for all filled trashcans in a building
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
# def send_notifs():
#     while not exit_event.is_set():
#         for trashcan in fetch_full_trashcans(db):
#             time_since_last_update = time.time() - trashcan[7].timestamp()
#             if time_since_last_update < 30:
#                 continue
            
#             cur.execute(f"SELECT phone FROM subscriptions WHERE trashcan_id={trashcan[0]}")
#             phones = cur.fetchall()
#             for phone in phones:
#                 cur.execute(f"UPDATE trashcans SET last_updated=NOW() WHERE id={trashcan[0]}")
#                 client.messages.create(
#                     body=f"Trashcan {trashcan[1]} is full!",
#                     from_="+19302033111",
#                     to=phone[0].replace("\\", "")
#                 )

#         time.sleep(10)
        
# # Start the messaging loop
# x = threading.Thread(target=send_notifs, daemon=True)
# x.start()

# Makes sure the SQL connection does not time out by pinging the server every 20 seconds
# def keep_alive():
#     while not exit_event.is_set():
#         cur.execute("SELECT * FROM subscriptions")
#         time.sleep(5)
        
# x = threading.Thread(target=keep_alive, daemon=True)
# x.start()

@atexit.register
def stop_server():
    exit_event.set()
    # x.join()
    cur.close()
    db.commit()

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=False)

def send_sql(resp: MessagingResponse) -> None:
    message = ""
    for trashcan in cur.fetchmany(5):
        id = trashcan[0]
        name = trashcan[1].replace("-", " ")
        fill_level = trashcan[7]
        
        status = "full" if fill_level == 100 else "empty"
        
        message += f"Trashcan {id} ({name}) is currently {status}.\n"
    
    if message == "":
        message = "Sorry, no results found."
    
    resp.message(message)

# Handles everything for receiving sms messages
@app.route("/sms", methods=["GET", "POST"])
def sms_reply() -> str:

    body = request.values.get('Body', None).lower().split()
    resp = MessagingResponse()
    
    match body[0]:
        case "?":
            if len(body) == 1:
                resp.message(help_message)
                return str(resp)
            
            match body[1]:
                case "show":
                    resp.message(show_help_message)
                case "subscriptions":
                    resp.message(subscriptions_help_message)
                case "route":
                    resp.message(routing_help_message)
                case _:
                    resp.message(help_message)
                    
        case "show":
            if len(body) == 1:
                resp.message(show_help_message)
                return str(resp)
            
            match body[1]:
                # Show all trashcans
                case "all":
                    cur.execute("SELECT * FROM smart_trashcan.trashcans ORDER BY id ASC;")
                    send_sql(resp)
                # Show all trashcans in a specified location
                case "building":
                    cur.execute(f"SELECT * FROM trashcans WHERE LOWER(location) LIKE '%{re.escape(body[2])}%';")
                    send_sql(resp)
                # Show specified trashcan
                case _:
                    cur.execute(f"SELECT * FROM trashcans WHERE id='{int(body[1])}';")
                    send_sql(resp)
        
        case "subscriptions":
            if len(body) == 1:
                resp.message(subscriptions_help_message)
                return str(resp)
            
            phone = request.values.get("From", None)
            match body[1]:
                # Show all subscriptions
                case "show":
                    cur.execute(f"SELECT trashcans.* FROM smart_trashcan.subscriptions JOIN smart_trashcan.trashcans ON trashcans.id=smart_trashcan.subscriptions.trashcan_id AND subscriptions.phone='{phone.replace("+","")}';")
                    send_sql(resp)
                    
                case "add":
                    match body[2]:
                        # Add all bins in a building
                        case "building":
                            if len(body) < 4:
                                resp.message(subscriptions_help_message)
                                return str(resp)
                            argument = None
                            try:
                                argument = int(body[3])
                            except TypeError:
                                resp.message("Invalid argument for add by building.")
                                return str(resp)
                            subscriptions_added = 0
                            cur.execute(f"SELECT id FROM trashcans WHERE LOWER(location) LIKE '%{argument}%';")
                            trashcans = cur.fetchall()
                            for trashcan in trashcans:
                                cur.execute(f"INSERT INTO subscriptions(trashcan_id, phone) VALUES ({trashcan[0]}, '{phone})';")
                                subscriptions_added += 1
                                
                            resp.message(f"{subscriptions_added} subscriptions for building {argument} added successfully.")
                        # Add a bin by ID
                        case _:
                            if len(body) < 3:
                                resp.message(subscriptions_help_message)
                                return str(resp)
                            try:
                                cur.execute(f"INSERT INTO subscriptions(trashcan_id, phone) VALUES ({int(body[2])}, {phone});")
                                resp.message(f"Subscription for trashcan {int(body[2])} added successfully.")
                            except TypeError:
                                resp.message("Invalid argument for add by id.")
                                return str(resp)
                case "delete":
                    match body[2]:
                        # Remove subscription from all bins
                        case "all":
                            cur.execute(f"DELETE FROM subscriptions WHERE phone='{phone}';")
                            resp.message("All subscriptions deleted successfully.")
                        # Remove subscription from a specific bin
                        case _:
                            cur.execute(f"DELETE FROM subscriptions WHERE phone='{phone}' AND trashcan_id={re.escape(body[2])}")
                            resp.message(f"Subscription for bin {body[2]} deleted successfully.")
                         
        case "route":
            building = None
            match body[1]:
                case "all":
                    ...
                case _:
                    building = re.escape(body[1])
            resp.message(get_route_text_from_cursor(cur, building))
                            
        # Unrecognized message
        case _:
            resp.message("Sorry, I did not understand that. Please make sure you are typing your command correctly, or type ? to see a list of options.")
    db.commit()
    return str(resp)

# Handles everything for receiving data from bins
@app.route("/data", methods={"GET", "POST"})
def data_fetch() -> str:
    body = request.get_json()
    print(body)
    try:
        id = int(body['id'])
        status = int(body['status'])
        print(status)
        cur.execute(f"UPDATE smart_trashcan.trashcans SET status={status} WHERE id={id};")

        if status == 100:
            cur.execute(f"SELECT location FROM smart_trashcan.trashcans WHERE id={id};")

            trashcan = cur.fetchone()[0]
            cur.execute(f"SELECT phone FROM smart_trashcan.subscriptions WHERE trashcan_id={id};")
            phones = cur.fetchall()

            for phone in phones:
                client.messages.create(
                    body=f"Trashcan {trashcan} is full!",
                    from_=os.environ["TWILIO_NUMBER"],
                    to=phone[0].replace("\\", "")
                )
        db.commit()
        return "Data was succesfully stored."
    except TypeError:
        raise Exception("Invalid data.") 

