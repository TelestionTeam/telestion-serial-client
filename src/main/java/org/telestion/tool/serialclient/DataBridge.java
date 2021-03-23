package org.telestion.tool.serialclient;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.telestion.api.message.JsonMessage;
import org.telestion.core.connection.SerialData;
import org.telestion.core.connection.TcpConn;

public final class DataBridge extends AbstractVerticle {

	private TcpConn.Participant participant = null;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		getVertx().eventBus().consumer("tcpBroadcast", h -> {
			JsonMessage.on(TcpConn.Participant.class, h, p -> {
				this.participant = p;
			});
			JsonMessage.on(TcpConn.Data.class, h, data -> {
				getVertx().eventBus().publish("serialOutgoing", new SerialData(data.data()).json());
			});
		});

		getVertx().eventBus().consumer("serialIncoming", h -> {
			JsonMessage.on(SerialData.class, h, d -> {
				getVertx().eventBus().publish("tcpOutgoing", new TcpConn.Data(participant, d.data()).json());
			});
		});

		startPromise.complete();
	}
}
