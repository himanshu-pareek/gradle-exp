#!/bin/bash

set -euo pipefail

git fetch origin main --depth=1

BASE_FILE="code-coverage-demo/coverage/base.csv"

if git cat-file -e origin/main:"${BASE_FILE}"; then
	git show origin/main:"${BASE_FILE}" > "${BASE_FILE}"
else
	echo "Basefile does not exist"
fi

