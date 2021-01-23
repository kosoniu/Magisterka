import chatterbot
from chatterbot import ChatBot
from chatterbot.trainers import ChatterBotCorpusTrainer
from chatterbot.trainers import ListTrainer
from chatterbot.comparisons import levenshtein_distance
from chatterbot.response_selection import get_first_response


class Conversation:

    def __init__(self):
        self.bot = ChatBot(
            'Marian',
            storage_adapter='chatterbot.storage.SQLStorageAdapter',
            database_uri='sqlite:///db.sqlite3',
            logic_adapters=[
                {
                    "import_path": "chatterbot.logic.BestMatch",
                    "statement_comparison_function": chatterbot.comparisons.levenshtein_distance,
                    "response_selection_method": chatterbot.response_selection.get_first_response,
                },
                "chatterbot.logic.MathematicalEvaluation"
            ]
        )

        self.corpus_trainer = ChatterBotCorpusTrainer(self.bot)

        self.corpus_trainer.train("../resources/training.json",
                                  "chatterbot.corpus.english.greetings")

    def get_answer(self, question):
        return self.bot.get_response(question)
