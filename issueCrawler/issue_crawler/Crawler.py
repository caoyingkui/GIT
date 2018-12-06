from bs4 import BeautifulSoup
from selenium import webdriver
import urllib.request
from issue_crawler.Issue import Issue
import os
import random
import time

driver = webdriver.PhantomJS(executable_path=r'F:\phantomjs_2_1_1\bin\phantomjs.exe')
driver.set_window_size(1920, 1080)


class Crawler:
    def __init__(self):
        pass

    def get_page(self, url):
        # page = urllib.request.urlopen(url)
        try:
            driver.get(url)
            driver.find_element_by_id('comment-tabpanel').click()
            page = driver.page_source
            result = BeautifulSoup(page, 'html5lib')
        except Exception as e:
            result = None

        time.sleep(random.randint(1, 3) + 2)
        return result


def crawl(project, max_id):
    crawler = Crawler()
    for issue_id in range(1, max_id):
        file_name = '../issue/' + project + '-' + str(issue_id) + '.json'
        if os.path.exists(file_name):
            continue
        print(os.path.abspath(file_name))
        url = 'https://issues.apache.org/jira/browse/' + project + '-' + str(issue_id)
        page = crawler.get_page(url)
        if page is not None:
            try:
                issue = Issue(page)
                f = open(file_name, 'w', encoding='utf-8')
                f.write(issue.toString())
                f.close()
            except Exception as e:
                f = open('../error.txt', 'a')
                f.write(project + "-" + str(issue_id) + "\n")
                continue


if __name__ == "__main__":
    crawl('SOLR', 13000)
    crawl('LUCENE', 8589)


