from flask import Flask, request
from pymessenger.bot import Bot

ACCESS_TOKEN = 'EAAoDY79pIBMBADMQAoiK1MPwXHrD9B3uIjgk40w4rHnv90OPZCmla58ozghwjRuZBkpZBeipUqMwHvZC0FEmZBoljUwkCy783PHWxPNHjZCXr825sxCjPsCejq5AvUzVjga9FAJOZA1QOqrlrhKQKAZBD4BJZAbGomBEUdEqYYu4KCwZDZD' # Page Access Token
VERIFY_TOKEN = 'EAAoDY79pIBMBADMQAoiK1MPwXHrD9B3uIjgk40w4rHnv90OPZCmla58ozghwjRuZBkpZBeipUqMwHvZC0FEmZBoljUwkCy783PHWxPNHjZCXr825sxCjPsCejq5AvUzVjga9FAJOZA1QOqrlrhKQKAZBD4BJZAbGomBEUdEqYYu4KCwZDZD' # Verification Token

app = Flask(__name__)
bot = Bot(ACCESS_TOKEN)

@app.route("/", methods=['GET', 'POST'])
def receive_message():
    if request.method == 'GET':
        token_sent = request.args.get("hub.verify_token")
        return verify_fb_token(token_sent)
    else:
        output = request.get_json()
        for event in output['entry']:            
            if 'messaging' in event:
                messaging = event['messaging']
                for message in messaging:
                    if message.get('message'):
                        recipient_id = message['sender']['id']
                        if message['message'].get('text'):
                            response_sent_text = get_message()
                            send_message(recipient_id, response_sent_text)
                return "Message Processed"

def verify_fb_token(token_sent):
    if token_sent == VERIFY_TOKEN:
        return request.args.get("hub.challenge")
    return 'Invalid verification token'

def get_message():
    response = "Tez jestes do dupy"
    return response

def send_message(recipient_id, response):
    bot.send_text_message(recipient_id, response)
    return "success"

if __name__ == "__main__":
    app.run(host="0.0.0.0",port=5000)