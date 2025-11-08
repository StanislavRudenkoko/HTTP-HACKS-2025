from twilio.rest import Client
from twilio.twiml.messaging_response import MessagingResponse
from flask import Flask, request
from dotenv import load_dotenv
import os, psycopg, re

load_dotenv()

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

# Handles everything for receiving sms messages
@app.route("/sms", methods=["GET", "POST"])
def sms_reply():

    body = request.values.get('Body', None)

    resp = MessagingResponse()

    return str(resp)

@app.route("/data", methods={"GET", "POST"})
def data_fetch():
    body = request.get_json()
    
    id = re.escape(body['id'])
    status = re.escape(body['status'])
    
    cur.execute(f"UPDATE trashcans SET status='{status}', last_updated=NOW() WHERE id='{id}'")
    
    return "Data was succesfully stored"
