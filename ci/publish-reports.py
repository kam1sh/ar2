#!/usr/bin/env python3
import subprocess
from os import getenv
from pathlib import Path

identity    = getenv("REPORTS_SSH_KEYFILE")
host        = getenv("REPORTS_HOST")
user        = getenv("REPORTS_SSH_USER")
slug        = getenv("JOB_NAME")

def run(*args):
    try:
        subprocess.run(list(args), check=True)
    except subprocess.CalledProcessError as e:
        with open("error.log", "w") as fd:
            print(e, file=fd)
            print(Path(identity).read_text(), file=fd) # FIXME temporary
        raise

slug = slug.replace("/", "-")
# sync report to reports container
print("Synchronizing report with server...")
run("rsync", "-av", "-e", f"ssh -i {identity}", "build/reports/allure-report/", f"{user}@{host}:{slug}/")
run("ssh", "-i", identity, f"{user}@{host}", "ln", "-s", slug, "latest")
print(f"Report available at {slug}")
