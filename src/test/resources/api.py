#!
import sys
import csv
import json
import http.client
import urllib.parse
import os
from string import Template

def read_config(config_file):
    with open(config_file, 'r') as file:
        return json.load(file)

def read_csv(filename):
    with open(filename, mode='r', newline='') as file:
        reader = csv.DictReader(file)
        return list(reader)

def read_template(filename):
    with open(filename, mode='r') as file:
        return file.read()

def fill_template(template_str, data_dict):
    template = Template(template_str)
    return template.safe_substitute(data_dict)

def send_request(host, path, method, body, headers=None, use_https=True):
    conn_class = http.client.HTTPSConnection if use_https else http.client.HTTPConnection
    conn = conn_class(host)
    conn.request(method, path, body=body, headers=headers or {})
    response = conn.getresponse()
    print(f"Status: {response.status}, Reason: {response.reason}")
    print("Response:", response.read().decode())
    conn.close()

def main():
    if len(sys.argv) != 2:
            print("Usage: python script.py config.json")
            sys.exit(1)

    config_file = sys.argv[1]

    if not os.path.isfile(config_file):
        print(f"Config file '{config_file}' does not exist.")
        sys.exit(1)

    config = read_config(config_file)

    # Extract values from config
    csv_file = config.get("CSV_FILE")
    json_template_file = config.get("JSON_TEMPLATE_FILE")
    api_host = config.get("API_HOST")
    api_path = config.get("API_PATH")
    api_method = config.get("API_METHOD", "POST").upper()
    use_https = config.get("USE_HTTPS", True)

    if not all([csv_file, json_template_file, api_host, api_path]):
        print("Missing required config values.")
        sys.exit(1)

    csv_data = read_csv(csv_file)
    json_template = read_template(json_template_file)

    for row in csv_data:
        filled_json_str = fill_template(json_template, row)
        json_body = json.dumps(json.loads(filled_json_str))  # Validate and prettify

        headers = {
            "Content-Type": "application/json",
            # "Authorization" : "Bearer " + token,
            "Content-Length": str(len(json_body))
        }

        print(f"Sending request for: {json_body}")
        send_request(api_host, api_path, api_method, json_body, headers, use_https)

if __name__ == "__main__":
    # python3 api.py user_group_api.json
    main()
