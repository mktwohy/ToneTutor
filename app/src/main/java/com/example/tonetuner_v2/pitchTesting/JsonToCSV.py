# based off of https://www.geeksforgeeks.org/convert-json-to-csv-in-python/

import json
import csv

with open('output.json') as json_file:
	tests = json.load(json_file)

# newline='' fixes extra newline issue
# https://stackoverflow.com/questions/16271236/python-3-3-csv-writer-writes-extra-blank-rows

with open('PitchTests.csv', 'w', newline='') as csv_file:
    csv_writer = csv.writer(csv_file)

    header = tests[0].keys()
    csv_writer.writerow(header)

    for test in tests:
    	csv_writer.writerow(test.values())
