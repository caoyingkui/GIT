from bs4 import BeautifulSoup
import json
import collections


class Issue:
    def __init__(self, page):
        self.status = True
        self.id = ""
        self.title = ""
        self.details = {}
        self.description = ""
        self.attachments = {}
        self.issue_links = {}
        self.comments = []

        self.init_id(page)
        self.init_title(page)
        self.init_details(page)
        self.init_description(page)
        self.init_attachments(page)
        self.init_issue_links(page)
        self.init_comment(page)

    def init_id(self, page):
        self.id = page.find('a', {'id': 'key-val'}).text.strip()

    def init_title(self, page):
        self.title = page.find('h1', {'id': 'summary-val'}).text.strip()

    def init_details(self, page):
        detail = page.find('ul', {'id': 'issuedetails'})

        self.details['type'] = detail.find('span', {'id': 'type-val'}).text.strip()
        self.details['status'] = detail.find('span', {'id': 'status-val'}).text.strip()
        self.details['priority'] = detail.find('span', {'id': 'priority-val'}).text.strip()
        self.details['resolution'] = detail.find('span', {'id': 'resolution-val'}).text.strip()
        self.details['affect_versions'] = detail.find('span', {'id': 'versions-val'}).text.strip()

        self.details['fix_versions'] = []
        for a in detail.find('span', {'id': 'fixfor-val'}).find_all('a'):
            self.details['fix_versions'].append(a.text.strip())

        self.details['components'] = []
        for a in detail.find('span', {'id': 'components-val'}).find_all('a'):
            self.details['components'].append(a.text.strip())

        self.details['labels'] = ""  # detail.find('span', {'class': 'labels'}).text.strip()

    def init_description(self, page):
        des = page.find('div', {'id':'descriptionmodule'})
        if des:
            self.description = des.find('div', {'class': 'mod-content'}).get_text().strip()

    def init_attachments(self, page):
        atta = page.find('ol', {'id': 'file_attachments'})
        if atta:
            for li in atta.find_all('li'):
                dt = li.find('dt', {'class': 'attachment-title'})
                patch = dt.find('a').text.strip()
                url = 'https://issues.apache.org' + dt.find('a')['href'].strip()
                self.attachments[patch] = url

    def init_issue_links(self, page):
        # TODO
        pass

    def init_comment(self, page):
        comment_container = page.find('div', {'id': 'issue_actions_container', 'class': 'issuePanelContainer'})
        if comment_container:
            for div in comment_container.find_all('div', class_='issue-data-block activity-comment twixi-block expanded'):
                comment = dict()
                comment['id'] = div['id']
                if comment['id'] == 'comment-12508577':
                    v = 2
                comment['date'] = div.find('time')['datetime']
                comment['author'] = div.find(['span', 'a'], class_='user-hover user-avatar').text.strip()
                comment['content'] = div.find('div', {'class': 'action-body flooded'}).get_text()
                self.comments.append(comment)

    def toString(self):
        if self.status is True:
            json_object = collections.OrderedDict()
            json_object['id'] = self.id
            json_object['title'] = self.title
            json_object['details'] = self.details
            json_object['description'] = self.description
            json_object['attachments'] = self.attachments
            json_object['issue_links'] = self.issue_links
            json_object['comments'] = self.comments
            s = json.dumps(json_object, indent=4)
            return s
        else:
            return None


if __name__ == '__main__':
    pass


