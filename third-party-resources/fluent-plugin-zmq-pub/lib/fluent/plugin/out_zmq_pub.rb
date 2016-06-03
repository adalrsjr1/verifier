module Fluent
  class Fluent::ZmqPubOutput < Fluent::BufferedOutput
    Fluent::Plugin.register_output('zmq_pub', self)

    config_param :pubkey, :string
    config_param :bindaddr, :string, :default => 'tcp://*:5556'
    config_param :highwatermark, :integer, :default => 1000
    # Send multiple record with the same publish key at once
    config_param :bulk_send, :bool, :default => false

    def initialize
      super
      require 'ffi-rzmq'
	  require 'json'
      @mutex = Mutex.new
    end

    def configure(conf)
      super
    end
    
    def start
      super
      @context = ZMQ::Context.new()
      @publisher = @context.socket(ZMQ::PUB)
      @publisher.setsockopt(ZMQ::SNDHWM, @highwatermark)
      @publisher.bind(@bindaddr)
    end

    def format(tag, time, record)
      [tag,time,record].to_msgpack
	  # record.to_json
    end

    def write(chunk)
      records = { }
      #  to_msgpack in format, unpack in write, then to_msgpack again... better way?
      chunk.msgpack_each{ |record|
        pubkey_replaced = @pubkey.gsub(/\${(.*?)}/){ |s|
          case $1
          when 'tag'
            record[0]
          else
            record[2][$1]
          end
        }

		record[2]['topic'] = pubkey_replaced

        if @bulk_send
          records[pubkey_replaced] ||= []
          records[pubkey_replaced] << record
        else
          @mutex.synchronize { 
            # @publisher.send_string(pubkey_replaced + " " + record[2].to_msgpack,ZMQ::DONTWAIT)
            @publisher.send_string(record[2].to_json,ZMQ::DONTWAIT)
          }
        end
      }
      if @bulk_send
        @mutex.synchronize { 
          records.each{  |k,v|
            # @publisher.send_string(k + " " + v[2].to_msgpack,ZMQ::DONTWAIT)
            @publisher.send_string(v[2].to_json,ZMQ::DONTWAIT)
          }
        }
      end
        
    end
 
    def shutdown
      super
      @publisher.close
      @context.terminate
    end

  end

end
