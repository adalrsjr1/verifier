package com.adalrsjr.processor_unit.fluentd.parsers

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class JsonParser implements IParser {
	private static final JsonSlurper jsonParser = new JsonSlurper()
	private static final JsonBuilder jsonBuilder = new JsonBuilder()
	
	public byte[] serialize(Object msg) {
		jsonBuilder(msg)
		jsonBuilder.toString().bytes
	}

	public Object deserialize(byte[] msg) {
		def obj = jsonParser.parse(msg)
		return obj
	}
	
	public static void main(String[] args) {
		JsonParser parser = new JsonParser() 
		
		println new String(parser.serialize([teste:"1234", body:"asldfajçlj"]))
		println new String(parser.serialize(["tag", 1234]))
		println new String(parser.serialize(["tag", 1234, [teste:"1234", body:"asldfajçlj"]]))
	}

	
}
