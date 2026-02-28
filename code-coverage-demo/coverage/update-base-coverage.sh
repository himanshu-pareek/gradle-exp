#!/bin/bash

set -euo pipefail

BASE_NEW="code-coverage-demo/coverage/base_new.csv"
BASE_FILE="code-coverage-demo/coverage/base.csv"

if [ -f "$BASE_NEW" ]; then
	echo "Updating base coverage wrom improved results..."
	mv "$BASE_NEW" "$BASE_FILE"
	git config --global user.name "GitHub Actions Bot"
	git config --global user.email "actions@github.com"

	git add "$BASE_FILE"
	git commit -m "ci: Update coverage baseline" || {
		echo "No coverage baseline changes to commit."
		exit 0
	}

	git checkout -- . || true
	git pull --rebase --autostash origin master
	git push origin HEAD:refs/heads/master
else
	echo "No improved coverage baseline found. Nothing to update."
fi

