package com.nr.instrumentation.vertx.reactive;

import java.util.logging.Level;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.DatastoreParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Segment;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class NRHandlerWrapper<T> implements Handler<AsyncResult<T>> {
	
	private Token token = null;
	private Handler<AsyncResult<T>> delegate = null;
	private Segment segment = null;
	private DatastoreParameters params = null;
	private static boolean isTransformed = false;
	
	public NRHandlerWrapper(Handler<AsyncResult<T>> d, Token t,Segment s, DatastoreParameters p) {
		delegate = d;
		token = t;
		segment = s;
		params = p;
		if(!isTransformed) {
			AgentBridge.instrumentation.retransformUninstrumentedClass(getClass());
			isTransformed = true;
		}
	}

	@Trace(async=true, excludeFromTransactionTrace=true)
	public void handle(AsyncResult<T> event) {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		if(segment != null) {
			if(params != null) {
				segment.reportAsExternal(params);
			}
			segment.end();
			NewRelic.getAgent().getLogger().log(Level.FINE, "ended segment for Vertx-Reactive: {0}",segment);
			segment = null;
		} else if(params != null) {
			NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
		}
		delegate.handle(event);
	}

}
