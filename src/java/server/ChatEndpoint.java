package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dominio.Message;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.*;
import javax.websocket.server.*;

@ServerEndpoint(value = "/chat/{username}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {
    private Session session;
    private static final Set<ChatEndpoint> CHATENDPOINTS = new CopyOnWriteArraySet<>();
    private static final HashMap<String, String> USERS = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {
        this.session = session;
        CHATENDPOINTS.add(this);
        USERS.put(session.getId(), username);
        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException, EncodeException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        Message msg = new Message();
        msg.setFrom(USERS.get(session.getId()));
        msg.setContent(jsonObject.get("content").getAsString());
        msg.setTo(jsonObject.get("username").getAsString());
        System.out.println(message);
        if (msg.getTo().equals("")) {
            broadcast(msg);
        } else {
            sendMessageToUser(msg.getTo(), msg);
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        CHATENDPOINTS.remove(this);
        Message message = new Message();
        message.setFrom(USERS.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(Message message) throws IOException, EncodeException {
        CHATENDPOINTS.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                }
            }
        });
    }

    private static void sendMessageToUser(String username, Message message) throws IOException, EncodeException {
        CHATENDPOINTS.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    if (USERS.get(endpoint.session.getId()).equals(username)) {
                        endpoint.session.getBasicRemote().sendObject(message);
                        return;
                    }
                } catch (IOException | EncodeException e) {
                }
            }
        });
    }
}