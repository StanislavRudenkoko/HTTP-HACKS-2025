from twilio.rest import Client
from twilio.twiml.messaging_response import MessagingResponse
from flask import Flask, request
from dotenv import load_dotenv
import os

load_dotenv()

account_sid = os.environ["TWILIO_ACCOUNT_SID"]
auth_token = os.environ["TWILIO_AUTH_TOKEN"]

client = Client(account_sid, auth_token)
app = Flask(__name__)

@app.route("/sms", methods=["GET", "POST"])
def sms_reply():

    body = request.values.get('Body', None)

    resp = MessagingResponse()

    # if body == 'hello':
    #     resp.message("Hi!")

    # elif body == 'bye':
    #     resp.message("Goodbye")
    print(body)
    return str(resp)

