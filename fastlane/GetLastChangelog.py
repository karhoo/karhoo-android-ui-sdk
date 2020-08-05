#!/usr/bin/python

import json

with open('changelog.json', 'r') as f:
    changelog = json.load(f)

print changelog[0]["notes"]
