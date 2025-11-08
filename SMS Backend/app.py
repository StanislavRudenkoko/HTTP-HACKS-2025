from twilio.rest import Client
from twilio.twiml.messaging_response import MessagingResponse
from flask import Flask, request
from dotenv import load_dotenv
import os, psycopg, re

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

print(subscriptions_help_message)

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
            
            phone_num = request.values.get("From", None)
            match body[1]:
                # Show all subscriptions
                case "show":
                    cur.execute(f"SELECT id, name, status FROM subscription JOIN trashcans ON subscriptions.phone_num='{phone_num}';")
                    send_sql(resp)
                    
                case "add":
                    argument = re.escape(body[3])
                    match body[2]:
                        # Add a bin by ID
                        case "id":
                            cur.execute(f"INSERT INTO subscription(id, phone_num) VALUES ({argument}, {phone_num});")
                        # Add all bins in a building
                        case "building":
                            subscriptions_added = 0
                            cur.execute(f"SELECT id FROM trashcans WHERE LOWER(location) LIKE '%{argument}%';")
                            trashcans = cur.fetchall()
                            for trashcan in trashcans:
                                cur.execute(f"INSERT INTO subscription(id, phone_num) VALUES ({trashcan[0]}, '{phone_num})';")
                                subscriptions_added += 1
                                
                            resp.message(f"{subscriptions_added} subscriptions for building {argument} added successfully.")
                
                case "delete":
                    match body[2]:
                        # Remove subscription from all bins
                        case "all":
                            cur.execute(f"DELETE FROM subscription WHERE phone_num='{phone_num}';")
                        # Remove subscription from a specific bin
                        case _:
                            cur.execute(f"DELETE FROM subscription WHERE phone_num='{phone_num}' AND id={re.escape(body[2])}")
                            
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
    
    cur.execute(f"UPDATE trashcans SET status={status}, last_updated=NOW() WHERE id={id};")
    
    if status >= 90:
        cur.execute(f"SELECT location FROM trashcans WHERE id={id};")
        trashcan = cur.fetchone()[0]
        cur.execute(f"SELECT phone_num FROM subscription WHERE id={id};")
        phone_nums = cur.fetchall()
        for phone_num in phone_nums:
            client.messages.create(
                body=f"Trashcan {trashcan} is full!",
                from_="+19302033111",
                to=phone_num[0].replace("\\", "")
            )
    
    return "Data was succesfully stored."
