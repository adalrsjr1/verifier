package com.adalrsjr.processor_unit.processor.hoafautomaton

import groovy.transform.Immutable
import groovy.util.logging.Slf4j

import com.adalrsjr.processor_unit.fluentd.pubsub.IPublisher
import com.adalrsjr.processor_unit.processor.ConditionOperator;
import com.adalrsjr.processor_unit.processor.IProcessorUnit
import com.adalrsjr.processor_unit.processor.IProcessorUnitEvent
import com.adalrsjr.processor_unit.processor.IProcessorUnitListener

@Immutable
class ResponseTimeEvent implements IProcessorUnitEvent {
	String uuid
	int responseTime
}

@Slf4j
class ResponseTimeProcessorUnit implements IProcessorUnit {
	
	private Set<IProcessorUnitListener> listeners = [] as Set
	
	private Map<String, Integer> state
	private def value
	private ConditionOperator operator 
	
	
	private IPublisher publisher
	
	ResponseTimeProcessorUnit(def value, ConditionOperator operator) {
		this.value = value
		this.operator = operator
		state = new LinkedHashMap<>()
	}
	
	private boolean evaluate(def calculated, def value, ConditionOperator operator) {
		if(operator == ConditionOperator.EQ)
			return calculated == value
		if(operator == ConditionOperator.NEQ)
			return calculated != value
		if(operator == ConditionOperator.GEQ)
			return calculated >= value
		if(operator == ConditionOperator.LEQ)
			return calculated <= value
		if(operator == ConditionOperator.GT)
			return calculated > value
		if(operator == ConditionOperator.GT)
			return calculated < value
	}
	
	@Override
	public def process(Map message) {
		
		if(message.containsKey("uuid")) {
			String uuid = message["uuid"]
			if(!state.containsKey(uuid)) {
				state[uuid] = message.containsKey("timestamp") ? message["timestamp"].toInteger() : System.currentTimeSeconds()
			}
			else {
				int diffSeconds = message.containsKey("timestamp") ? message["timestamp"].toInteger() : System.currentTimeSeconds()
				diffSeconds -= state[uuid]
				state.remove(uuid)
				if(evaluate(diffSeconds, value, operator))
					notifyListeners(new ResponseTimeEvent(uuid, diffSeconds))
			}
		}
	}

	@Override
	public void publish(Map object) {
		if(publisher) {
			publisher.publish(object)
		}
		else {
			log.warn "neet to set a publisher at ${this}"
		}
	}

	@Override
	public IPublisher getPublisher() {
		publisher
	}

	@Override
	public void setPublisher(IPublisher publisher) {
		this.publisher = publisher
	}

	@Override
	public void cleanup() {
		state.clear()
	}

	public void registerListener(IProcessorUnitListener listener) {
		listeners << listener
	}

	public void unregisterListener(IProcessorUnitListener listener) {
		listeners.remove(listener)
	}

	public void notifyListeners(IProcessorUnitEvent event) {
		for(listener in listeners) {
			listener.beNotified(event)
		}
	}

	public static void main(String[] args) {
		ResponseTimeProcessorUnit processorUnit = new ResponseTimeProcessorUnit(0, ConditionOperator.GEQ)
		
		processorUnit.registerListener({event ->
			log.info event.toString()
		})
		
		List reqRes = [
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34709", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"29a4992f9fe8125522850271de4f8397"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38313", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"86dca8f9490237fa39281ea50ea78b52"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38313", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"86dca8f9490237fa39281ea50ea78b52"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37263", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=preto&price=25-36",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bea90b2c28e9cb97a160ef03e5b2b78a"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37263", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=preto&price=25-36",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bea90b2c28e9cb97a160ef03e5b2b78a"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37263", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bea90b2c28e9cb97a160ef03e5b2b78a"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34709", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"29a4992f9fe8125522850271de4f8397"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33712", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"c209163f3e310b6c0a9a2c6489e22a8e"],
			[action:"GET", host_dst:"172.017.000.010", host_src:"172.017.000.001", port_dst:"08110", port_src:"33716", req_protocol:"HTTP/1.1", request:"/frontend/public/products?user=Usuario4",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"dfe0c74a3f265edd7b90e43990e70b00"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34713", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario4",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"ed21bc65a1a2b27646215808e98a27e5"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34713", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario4",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"ed21bc65a1a2b27646215808e98a27e5"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38317", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario4",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"5315f02ae01b106a9763ab08e5cd141a"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38317", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario4",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"5315f02ae01b106a9763ab08e5cd141a"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.008", port_dst:"38317", port_src:"08090", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"5315f02ae01b106a9763ab08e5cd141a"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37267", req_protocol:"HTTP/1.1", request:"/products/public/query?product=calcado&color=laranja&price=100-400",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"ebbecc2ff7604c453e5ed39ee9d4fa76"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37267", req_protocol:"HTTP/1.1", request:"/products/public/query?product=calcado&color=laranja&price=100-400",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"ebbecc2ff7604c453e5ed39ee9d4fa76"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37267", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"ebbecc2ff7604c453e5ed39ee9d4fa76"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34713", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"ed21bc65a1a2b27646215808e98a27e5"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33716", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"dfe0c74a3f265edd7b90e43990e70b00"],
			[action:"GET", host_dst:"172.017.000.010", host_src:"172.017.000.001", port_dst:"08110", port_src:"33720", req_protocol:"HTTP/1.1", request:"/frontend/public/products?user=Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"abcd43eee9c9f6bce781261953bad742"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34717", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"15fface5020ecd9656f0feff6b003e52"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34717", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"15fface5020ecd9656f0feff6b003e52"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38321", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"30c74fb787fe4aebb696904695122661"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38321", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"30c74fb787fe4aebb696904695122661"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.008", port_dst:"38321", port_src:"08090", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"30c74fb787fe4aebb696904695122661"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37271", req_protocol:"HTTP/1.1", request:"/products/public/query?product=calcado&color=cinza&price=0-30",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"d67dfda04a2be71c7bf1b1e423c7f182"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37271", req_protocol:"HTTP/1.1", request:"/products/public/query?product=calcado&color=cinza&price=0-30",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"d67dfda04a2be71c7bf1b1e423c7f182"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37271", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"d67dfda04a2be71c7bf1b1e423c7f182"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34717", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"15fface5020ecd9656f0feff6b003e52"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33720", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"abcd43eee9c9f6bce781261953bad742"],
			[action:"GET", host_dst:"172.017.000.010", host_src:"172.017.000.001", port_dst:"08110", port_src:"33724", req_protocol:"HTTP/1.1", request:"/frontend/public/products?user=Usuario10",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"da56910e9cfdd3202aca15bcd589f301"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34721", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario10",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bd4618614d1000edb6172883051e6b09"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34721", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario10",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bd4618614d1000edb6172883051e6b09"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38325", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario10",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"28e8ba1f3729d861f72ea3ab7f53c4f0"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38325", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario10",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"28e8ba1f3729d861f72ea3ab7f53c4f0"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.008", port_dst:"38325", port_src:"08090", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"28e8ba1f3729d861f72ea3ab7f53c4f0"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37275", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=preto&price=22-52",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"b3b67fa23d271fc984b3dc03a6e8f06f"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37275", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=preto&price=22-52",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"b3b67fa23d271fc984b3dc03a6e8f06f"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37275", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"b3b67fa23d271fc984b3dc03a6e8f06f"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34721", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bd4618614d1000edb6172883051e6b09"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33724", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"da56910e9cfdd3202aca15bcd589f301"],
			[action:"GET", host_dst:"172.017.000.010", host_src:"172.017.000.001", port_dst:"08110", port_src:"33728", req_protocol:"HTTP/1.1", request:"/frontend/public/products?user=Usuario5",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"0c4a4df48a930b56e7d71ec5a34b8257"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34725", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario5",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"464d010df95a26d64fc60fe43c9c8fb9"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34725", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario5",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"464d010df95a26d64fc60fe43c9c8fb9"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38329", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario5",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"6e68940dd538769f25afc324acf4ab41"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38329", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario5",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"6e68940dd538769f25afc324acf4ab41"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.008", port_dst:"38329", port_src:"08090", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"6e68940dd538769f25afc324acf4ab41"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37279", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=carmesim&price=51-184",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"a2b7263a0af42b6ed8b3997be2216667"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37279", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=carmesim&price=51-184",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"a2b7263a0af42b6ed8b3997be2216667"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37279", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"a2b7263a0af42b6ed8b3997be2216667"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34725", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"464d010df95a26d64fc60fe43c9c8fb9"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33728", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"0c4a4df48a930b56e7d71ec5a34b8257"],
			[action:"GET", host_dst:"172.017.000.010", host_src:"172.017.000.001", port_dst:"08110", port_src:"33732", req_protocol:"HTTP/1.1", request:"/frontend/public/products?user=Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"f8c3d303b667784148f7c66bc4d9845c"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34729", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"1bcff605b97529d50b72350e3e08ad5b"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34729", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"1bcff605b97529d50b72350e3e08ad5b"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38333", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"1def933688fa9fe86b2737a034b56d48"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38333", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario1",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"1def933688fa9fe86b2737a034b56d48"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.008", port_dst:"38333", port_src:"08090", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"1def933688fa9fe86b2737a034b56d48"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37283", req_protocol:"HTTP/1.1", request:"/products/public/query?product=calcado&color=cinza&price=0-30",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"49ab60e1d57a951d13d3bff4b7e6be6c"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37283", req_protocol:"HTTP/1.1", request:"/products/public/query?product=calcado&color=cinza&price=0-30",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"49ab60e1d57a951d13d3bff4b7e6be6c"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37283", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"49ab60e1d57a951d13d3bff4b7e6be6c"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34729", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"1bcff605b97529d50b72350e3e08ad5b"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33732", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"f8c3d303b667784148f7c66bc4d9845c"],
			[action:"GET", host_dst:"172.017.000.010", host_src:"172.017.000.001", port_dst:"08110", port_src:"33736", req_protocol:"HTTP/1.1", request:"/frontend/public/products?user=Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bc5fcb0018cecacba559dc512740091b"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34733", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"9caebed71ad77fb2fa8a4aefd69110d5"],
			[action:"GET", host_dst:"172.017.000.009", host_src:"172.017.000.010", port_dst:"08100", port_src:"34733", req_protocol:"HTTP/1.1", request:"/match/public/login/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"9caebed71ad77fb2fa8a4aefd69110d5"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38337", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"7d7e5f1368e579204823ba3df688e929"],
			[action:"GET", host_dst:"172.017.000.008", host_src:"172.017.000.009", port_dst:"08090", port_src:"38337", req_protocol:"HTTP/1.1", request:"/profiles/public/user/Usuario2",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"7d7e5f1368e579204823ba3df688e929"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.008", port_dst:"38337", port_src:"08090", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"7d7e5f1368e579204823ba3df688e929"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37287", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=preto&price=25-36",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"7990d841ae253a1ec1cf27a8bdad4d13"],
			[action:"GET", host_dst:"172.017.000.007", host_src:"172.017.000.009", port_dst:"08080", port_src:"37287", req_protocol:"HTTP/1.1", request:"/products/public/query?product=roupa&color=preto&price=25-36",timestamp:"1465248871", topic:"trace.log.zmq", uuid:"7990d841ae253a1ec1cf27a8bdad4d13"],
			[host_dst:"172.017.000.009", host_src:"172.017.000.007", port_dst:"37287", port_src:"08080", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"7990d841ae253a1ec1cf27a8bdad4d13"],
			[host_dst:"172.017.000.010", host_src:"172.017.000.009", port_dst:"34733", port_src:"08100", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"9caebed71ad77fb2fa8a4aefd69110d5"],
			[host_dst:"192.168.201.254", host_src:"172.017.000.010", port_dst:"33736", port_src:"08110", res_msg:"OK", res_protocol:"HTTP/1.1", response:"200", timestamp:"1465248871", topic:"trace.log.zmq", uuid:"bc5fcb0018cecacba559dc512740091b"]
		]
		
		reqRes.each { m ->
			processorUnit.process(m)
		}
	}
}
