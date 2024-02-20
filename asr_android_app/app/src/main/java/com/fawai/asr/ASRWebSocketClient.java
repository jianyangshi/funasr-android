package com.fawai.asr;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ASRWebSocketClient extends WebSocketClient {

    public ASRWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("ASRWebSocketClient", handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        Log.i("ASRWebSocketClient", message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
