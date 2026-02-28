#!/bin/bash

set -euo pipefail

get fetch origin master --depth=1

if git cat-file -e origin/master:code-coverage-demo/coverage/base.csv; then
	git show origin/master:code-coverage-demo/coverage/base.csv > code-coverage-demo/coverage/base.csv
else
	echo "Basefile does not exist"
fi

