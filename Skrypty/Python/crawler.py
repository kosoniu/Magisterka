import bs4
import requests

headers = {'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0'}

result = requests.get("https://allegro.pl/listing?string=flask%20development&bmatch=engag-dict45-dd-uni-1-2-1023", headers=headers)
amazon_page = result.text #open('amazon_flask.html','r')
soup = bs4.BeautifulSoup(amazon_page, features="lxml")

titles = soup.findAll('a',{"class":"_w7z6o _uj8z7 meqh_en mpof_z0 mqu1_16 _9c44d_2vTdY"})

rows = soup.findAll('div',{"class":"mpof_ki myre_zn _9c44d_1Hxbq"})

all_rows = True
previous_title = None
for row in rows:
    titles = row.findAll('h2',{"class":"mgn2_14 m9qz_yp mqu1_16 mp4t_0 m3h2_0 mryx_0 munh_0 mp4t_0 m3h2_0 mryx_0 munh_0"})
    prices = row.findAll('span',{"class":"_1svub _lf05o"})
    print(titles[0].text + ', ' + prices[0].text)