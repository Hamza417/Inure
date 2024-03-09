import json
import re
from datetime import datetime, timedelta

import requests

token = ""


def set_personal_access_token():
    file_ = open('secrets.txt', 'r')
    global token
    token = file_.readline().strip()


def get_workflow_runs(url):
    headers = {'Authorization': f"token {token}"}
    response = requests.get(url, headers=headers)
    if response.status_code != 200:
        print(f"Error: {response.text}")
        return [], None
    link_header = response.headers.get('Link')
    next_url_ = None
    if link_header:
        match = re.search(r'<(https://api.github.com/[^>]+)>;\s*rel="next"', link_header)
        if match:
            next_url_ = match.group(1)
    return json.loads(response.text)['workflow_runs'], next_url_


def delete_workflow_run(run_id):
    url = f"https://api.github.com/repos/Hamza417/Inure/actions/runs/{run_id}"
    headers = {'Authorization': f"token {token}"}
    response = requests.delete(url, headers=headers)
    if response.status_code != 204:
        print(f"Error: {response.text}")
        return
    else:
        print(f"Deleted workflow run {run_id} with name {run['name']}")


old_date = datetime.now() - timedelta(days=14)
next_url = f"https://api.github.com/repos/Hamza417/Inure/actions/runs"
set_personal_access_token()

while next_url:
    workflow_runs, next_url = get_workflow_runs(next_url)
    for run in workflow_runs:
        created_at = datetime.strptime(run['created_at'], '%Y-%m-%dT%H:%M:%SZ')
        if created_at < old_date:
            delete_workflow_run(run['id'])
