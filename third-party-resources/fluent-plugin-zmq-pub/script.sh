#!/bin/sh

if [ $# -eq 0 ];then
	echo 'usage: script <install | build>'
elif [ $1 = 'install' ]; then
	gem build fluent-plugin-zmq-pub.gemspec
elif [ $1 = 'build' ]; then
	sudo td-agent-gem install fluent-plugin-zmq-pub-0.0.4.gem
fi

