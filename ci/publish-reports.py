#!/usr/bin/env python3
from subprocess import run
from os import getenv

identity    = getenv("REPORTS_SSH_KEYFILE")
host        = getenv("REPORTS_HOST")
user        = getenv("REPORTS_SSH_USER")
slug        = getenv("JOB_NAME")

slug = slug.replace("/", "-")
# sync report to reports container
print("Synchronizing report with server...")
run(["rsync", "-av", "-e", f"ssh -i {identity}", "build/reports/allure-report/", f"{host}:{slug}/"])
run(["ssh", "-i", identity, "ln", "-s", slug, "latest"])
print(f"Report available at http://{host}/{slug}")
