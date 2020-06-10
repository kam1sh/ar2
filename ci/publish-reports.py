#!/usr/bin/env python3
from subprocess import run
from os import getenv

# generate report slug
slug = getenv("JOB_NAME").replace("/", "_")
print(f"Report slug: {slug}")
# sync report to reports container
print("Synchronizing report to server...")
# TODO sync to folder with slug
# TODO sync to folder 'latest'
