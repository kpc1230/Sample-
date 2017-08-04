#!/usr/bin/python

import sys, urllib2, json

GET_REQUEST_BASE_URL = 'https://manifesto.atlassian.io/api/env/'

BOLD = '\033[1m'
RESET = '\033[0m'

def getDataAsJson(environment):
	try:
		return json.load(urllib2.urlopen(GET_REQUEST_BASE_URL+environment))
	except urllib2.HTTPError, e:
		print('%i %s: %s' % (e.code, e.msg, e.fp.read()))
		print('Oops, did you mispell the environment? \'%s\'' % environment)
		sys.exit(1)

def getVersion(environment, product):
	manifestoData = getDataAsJson(environment)
	# https://manifesto.atlassian.io/doc/rest.html
	#
	# {
	# 	hash: '<hash>',
	# 	details:
	# 	{
	# 		products: [{ artifact: '<product>', version: '<version>' }...],
	# 		plugins: [{ artifact: '<plugin>', version: '<version>' }...],
	# 	}
	# }
	details = manifestoData['details']
	products = details['products']
	plugins = details['plugins']

	version = None
	for p in products:
		if p['artifact'] == product:
			version = p['version']
			break
	if version != None:
		return version

	# maybe it's a plugin
	for p in plugins:
		if p['artifact'] == product:
			version = p['version']
			break
	if version != None:
		return version
	else:
		print('Could not find a version for \'%s\'' % product)
		print('Oops, did you mispell the product? \'%s\'' % product)
		sys.exit(1)

def writePropertiesFile(propertiesFile, version):
	with open(propertiesFile, 'w') as file:
		file.write('jira.version='+version)
	print('Wrote to \'%s\'' % propertiesFile)


if len(sys.argv) == 4:
	# first arg is the script name itself, i.e. write_jira_version_to_properties.py
	propertiesFile = sys.argv[1]
	environment = sys.argv[2]
	product = sys.argv[3]
	version = getVersion(environment, product)
	print('The current version of \'%s\' is \'%s\'' % (environment, version))
	writePropertiesFile(propertiesFile, version)
else:
	print('Usage: '+BOLD+'python write_jira_version_to_properties.py'+RESET+' <properties_file> <environment> <product OR plugin>')
