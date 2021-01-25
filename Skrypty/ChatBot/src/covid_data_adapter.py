from chatterbot.conversation import Statement
from chatterbot.logic import LogicAdapter
import requests


class CovidDataAdapter(LogicAdapter):

    def __init__(self, chatbot, **kwargs):
        super().__init__(chatbot, **kwargs)
        self.headers = {
            'x-rapidapi-key': "27cd501249mshca5152e96d89d42p148be5jsn5d5e64e7bd0f",
            'x-rapidapi-host': "covid-19-data.p.rapidapi.com"
        }

    def can_process(self, statement):
        words = ['covid', 'deaths', 'statistics', 'vaccination', 'daily', 'coronavirus']

        if any(x in statement.text.split() for x in words):
            return True
        else:
            return False

    def process(self, input_statement, additional_response_selection_parameters):
        countries = ["poland", "italy", "usa", "belgium"]

        text = input_statement.text.lower()

        if any(x in text.split() for x in countries):
            matched_country = None
            for country in countries:
                if country in input_statement.text.lower().split():
                    matched_country = country

            if matched_country is not None:
                statement = self.__prepare_latest_country_statistics(matched_country)
            else:
                statement = Statement(text="Unfortunately I don't have statistics about this country")
        elif "total" in input_statement.text.split():
            statement = self.__prepare_total_statistics()
        else:
            statement = Statement(text="I'm sorry. I don't understand.")

        return statement

    def __prepare_latest_country_statistics(self, country):
        url = "https://covid-19-data.p.rapidapi.com/country"

        querystring = {"name": country}

        response = requests.get(url, headers=self.headers, params=querystring)

        if response.status_code != 200:
            statement = Statement(text="I'm sorry. I cannot find right statistics now")
        else:
            data = response.json()
            statement = Statement(text="Covid statistics in {},\nTotal deaths: {},\nTotal recovered: {},\n".format(country.capitalize(), data[0]["deaths"], data[0]["recovered"]))

        return statement

    def __prepare_total_statistics(self):
        url = "https://covid-19-data.p.rapidapi.com/totals"
        response = requests.get(url, headers=self.headers)
        data = response.json()
        statement = Statement(text="Total deaths: {},\nTotal recovered: {},\n".format(data[0]["deaths"], data[0]["recovered"]))

        return statement
