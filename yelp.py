from yelpapi import YelpAPI
import json
import sys

key = "your yelp-api key"
yelp_api = YelpAPI(key)

result = {}

with open(sys.argv[1],'r') as infile:
    phones = infile.read().split(',')
    
for obj in phones:
    phone = "+1" + json.loads(obj)["PHONE"]
    response = yelp_api.phone_search_query(phone = phone)
    result.update(response)

with open(sys.argv[1]+'business.json', 'w') as outfile:
    json.dump(result, outfile)
