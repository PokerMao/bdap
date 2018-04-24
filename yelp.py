'''
python yelp.py #number

'''

from yelpapi import YelpAPI
import json
import sys
import time

key = "api-key"
yelp_api = YelpAPI(key)

result = []

temp = open("business"+sys.argv[1]+".json","wt")

data = json.load(open(sys.argv[1]))
    
for line in data:
	phone = "+1" + line["PHONE"]
	if "_" in phone:
		continue
	response = yelp_api.phone_search_query(phone = phone)
	if response is not None:
		result.append(response)
		temp.write(json.dumps(response) + '\n')
	time.sleep(1)

with open("business_" + sys.argv[1] + ".json", 'w') as outfile:
    json.dump(result, outfile)
