package io.vertx.reactivex.sqlclient;

import com.newrelic.agent.bridge.datastore.DatabaseVendor;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.vertx.reactive.NRHandlerWrapper;
import com.nr.instrumentation.vertx.reactive.Utils;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@Weave(type=MatchType.BaseClass)
public abstract class Query<T> {
	
	@NewField
	public String query;
	
	@NewField
	public DatabaseVendor vendor;
	
	@Trace(dispatcher=true)
	public void execute(Handler<AsyncResult<T>> handler) {
//		if(!(handler instanceof NRHandlerWrapper)) {
//			NRHandlerWrapper<T> wrapper = Utils.getWrapper(handler, query, vendor);
//			handler = wrapper;
//		}
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher=true)
	public Single<T> rxExecute() { 
		return Weaver.callOriginal();
	}
}
