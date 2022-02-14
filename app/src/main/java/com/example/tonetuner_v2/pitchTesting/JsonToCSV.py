# based off of https://www.geeksforgeeks.org/convert-json-to-csv-in-python/
# python files can't run inside Android Studio unfortunately.
# you'll have to open this in your favorite text editor, IDE, or run it in the terminal

import json
import csv
import os
import struct

import numpy as np
import scipy.io.wavfile as wavf
import functools

from typing import List

DIR = r"C:\Users\Twohy\Desktop\PitchTest_Output"


def main():
    with open('output.json') as json_file:
        tests = json.load(json_file)

    os.chdir(DIR)

    # newline='' fixes extra newline issue
    # https://stackoverflow.com/questions/16271236/python-3-3-csv-writer-writes-extra-blank-rows
    with open('PitchTests.csv', 'w', newline='') as csv_file:
        csv_writer = csv.writer(csv_file)

        header = tests[0].keys()
        csv_writer.writerow(header)

        i = 0
        for test in tests:
            wav_filename = create_filename(test, i)
            write_test_audio(test=test, filename=wav_filename)
            test['audio'] = os.path.join(DIR, wav_filename)
            csv_writer.writerow(test.values())
            i += 1


def write_test_audio(test, filename: str):
    sample_rate = test['sampleRate']
    data = np.array(test['audio']).astype('float32')
    data = np.pad(data, pad_width=(sample_rate, sample_rate))
    wavf.write(filename, sample_rate, data)


def get_bytes(data: List[int]) -> bytes:
    return struct.pack('%sf' % len(data), *data)


def create_filename(test, i: int) -> str:
    pitch = test['expectedPitch']
    fingerprint = test['fingerprint']
    size = test['bufferSize']
    return f"Test{i}_{size}_{pitch}hz_{fingerprint}.wav"


if __name__ == '__main__':
    main()
