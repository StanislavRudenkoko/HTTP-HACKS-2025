from twilio.rest import Client
from twilio.twiml.messaging_response import MessagingResponse
from flask import Flask, request
from dotenv import load_dotenv
import os, psycopg, re

load_dotenv()

help_message = """
                OPTIONS:\n
                1. ? - Shows this help menu.\n
                2. SHOW ALL - Displays the status of all trashcans.\n
                3. SHOW {BUILDING} - Displays the status of all trashcans in that building.
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

cur.execute("SET search_path TO smart_trashcan")

def format_sql(resp: MessagingResponse) -> None:
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
            match body[1]:
                case "all":
                    cur.execute("SELECT * FROM trashcans")
                    format_sql(resp)

                case _:
                    cur.execute(f"SELECT * FROM trashcans WHERE LOWER(location) LIKE '%{body[1]}%'")
                    format_sql(resp)
        case _:
            resp.message("Sorry, I didn't understand that.\n" + help_message)

    return str(resp)


@app.route("/data", methods={"GET", "POST"})
def data_fetch() -> str:
    body = request.get_json()
    
    id = re.escape(body['id'])
    status = re.escape(body['status'])
    
    cur.execute(f"UPDATE trashcans SET status='{status}', last_updated=NOW() WHERE id='{id}'")
    
    return "Data was succesfully stored"
