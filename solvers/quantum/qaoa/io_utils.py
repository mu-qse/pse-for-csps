import json


def read_from_file(path: str):
    with open(path, 'r') as file:
        return json.load(file)

def write_to_file(value, path: str):
    with open(path, 'w') as file:
        return json.dump(value, file)
