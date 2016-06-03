#!/usr/bin/env ruby

require 'ffi-rzmq'
require 'msgpack'
require 'json'

context = ZMQ::Context.new
subscriber = context.socket ZMQ::SUB
subscriber.connect "tcp://localhost:5557"
subscriber.setsockopt ZMQ::SUBSCRIBE, 'zmq.fluentd2'


loop do
	key = ''
	subscriber.recv_string key, 0

	content = ''
	subscriber.recv_string content, 0

	# upck = MessagePack.unpack(content)
	upck = JSON.parse(content)

	puts "[#{key}] #{upck}"
end

