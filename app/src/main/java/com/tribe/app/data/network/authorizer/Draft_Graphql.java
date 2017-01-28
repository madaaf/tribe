package com.tribe.app.data.network.authorizer;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;

public class Draft_Graphql extends Draft_10 {

    public Draft_Graphql() {

    }

    public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakeData) throws InvalidHandshakeException {
        int v = readVersion(handshakeData);
        return v == 13 ? HandshakeState.MATCHED : HandshakeState.NOT_MATCHED;
    }

    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        super.postProcessHandshakeRequestAsClient(request);
        request.put("Sec-WebSocket-Version", "13");
        request.put("Sec-WebSocket-Protocol", "graphql");
        return request;
    }

    public Draft copyInstance() {
        return new Draft_Graphql();
    }
}