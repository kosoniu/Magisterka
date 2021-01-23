from flask import Flask, request
from pymessenger.bot import Bot
from conversation import Conversation

ACCESS_TOKEN = 'EAAoDY79pIBMBAP3BBfOXZAYgeZCbmksQFhUVAi83MEUJv4Eg6HmInpr4XmvRTkwUyT7FsCcq9B6kZBXlMYzIyyrL9kYgI4j5JN0gZAZBBsewO9Yp2zB0F0miL1vK2MJOMl1KPAV5f1gURJq2i3W9FCBcz9ZB9NLPMao6PwyePSZCgZDZD' # Page Access Token
VERIFY_TOKEN = '1nOdhofpAS8FgHi730OnGnrxUe0_2r3PUVkAojf8BMyp7FSC7' # Verification Token

app = Flask(__name__)
bot = Bot(ACCESS_TOKEN)

conversation = Conversation()


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
                            send_message(recipient_id, message['message'].get('text'))
                return "Message Processed"


def verify_fb_token(token_sent):
    if token_sent == VERIFY_TOKEN:
        return request.args.get("hub.challenge")
    return 'Invalid verification token'


def get_message(question):
    response = conversation.get_answer(question)
    return response


def send_message(recipient_id, question):
    response = get_message(question)
    bot.send_text_message(recipient_id, str(response))
    return "success"


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)