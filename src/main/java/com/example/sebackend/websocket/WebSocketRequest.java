package com.example.sebackend.websocket;

import com.example.sebackend.entity.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint("/webSocket/request/{username}")
public class WebSocketRequest {

    public static  final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
//    @Autowired
    //无参构造函数
//    public WebSocketRequest(@Qualifier("clients") ConcurrentHashMap<String, Session> clients) {
//        this.clients = clients;
//    }
    //无参构造函数
//    public WebSocketRequest() {
//        this.clients = null;
//    }

    //    private Session session;
    private String username="";

    //调用@OnOpen注解的方法，当客户端连接成功时调用
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        this.username = username;
        log.info("有新连接加入：" + username );
        clients.put(username, session);
    }

    //调用@OnClose注解的方法，当客户端断开连接时调用
    @OnClose
    public void onClose() {
        clients.remove(username);
//        WebSocket.onlineCount--;
    }

    //调用@OnMessage注解的方法，当客户端发送消息时调用
    @OnMessage
    public void onMessage(String message) {
    }

    //调用@OnError注解的方法，当发生错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket发生错误：" + throwable.getMessage());
    }

    //发送消息
    public void sendMessage(String username, Response message) throws JsonProcessingException {
        // 向所有连接websocket的客户端发送消息
        // 可以修改为对某个客户端发消息
        //message转换成json
        Session session = clients.get(username);
        if (session != null) {
            session.getAsyncRemote().sendText(message.toJson());
        }

    }


}
