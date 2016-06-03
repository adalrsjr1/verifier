package com.adalrsjr.processor_unit.fluentd.parsers

interface IParser {
	byte[] serialize(Object msg) 
	Object deserialize(byte[] msg)
}
