import argparse
import json
import re
from datetime import datetime, timedelta

import requests


def get_workflow_runs(url, token):
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


def delete_workflow_run(run_id, token):
    url = f"https://api.github.com/repos/Hamza417/Inure/actions/runs/{run_id}"
    headers = {'Authorization': f"token {token}"}
    response = requests.delete(url, headers=headers)
    if response.status_code != 204:
        print(f"Error: {response.text}")
    else:
        print(f"Deleted workflow run {run_id}")


def main(token):
    old_date = datetime.now() - timedelta(days=14)
    next_url = f"https://api.github.com/repos/Hamza417/Inure/actions/runs"

    while next_url:
        workflow_runs, next_url = get_workflow_runs(next_url, token)
        print(f"Found {len(workflow_runs)} workflow runs")

        for run in workflow_runs:
            created_at = datetime.strptime(run['created_at'], '%Y-%m-%dT%H:%M:%SZ')
            if created_at < old_date:
                delete_workflow_run(run['id'], token)

    print("Done")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("token", help="GitHub token")
    args = parser.parse_args()
    main(args.token)
