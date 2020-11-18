from flask import Flask, render_template
import sqlite3

app = Flask(__name__)
app.config.from_object(__name__)

books = (
    [0,"Harry Potter"],
    [1,"Wiedźmin"],
    [2,"Trylogia Husycka"]
)

def init_db():
    conn = sqlite3.connect('oreilly.sqlite')
    conn.execute("DROP TABLE IF EXISTS books;")
    conn.execute("CREATE VIRTUAL TABLE books USING fts5(id,title);")
    cur = conn.cursor()
    for book in books:
        cur.execute('INSERT INTO books(id,title) VALUES("'+ book[0] +','+ book[1] +'");')

    conn.commit()
    conn.close()

@app.route('/')
def index():
    return 'Hello world'


#@app.route('/<path:path>/')
#def page(path):
#    return pages.get_or_404(path).html

@app.route('/delete/<int:id>/')
def delete(id):
    conn = sqlite3.connect('oreilly.sqlite')
    conn.execute('SELECT * FROM books WHERE ')
    return
    # return render_template('page.html', page=page)

@app.route('/list/')
def get():
    return render_template('page.html', page=page)


if __name__ == '__main__':
    init_db()
    app.run(host="0.0.0.0", port=5000)