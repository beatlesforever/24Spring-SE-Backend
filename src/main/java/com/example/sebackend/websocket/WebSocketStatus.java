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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint("/webSocket/status/{username}")
public class WebSocketStatus {

    public static  final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();

    private String username;

    //调用@OnOpen注解的方法，当客户端连接成功时调用
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        this.username = username;
        clients.put(username, session);
    }

    //调用@OnClose注解的方法，当客户端断开连接时调用
    @OnClose
    public void onClose() {
        clients.remove(username);
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
        //判断是否为管理员
        if (Objects.equals(username, "admin")) {
            Session session = clients.get("admin");
            if (session != null) {
                session.getAsyncRemote().sendText(message.toJson());
            }
        } else {
            //返回没有权限
            Response response = new Response(403, "Forbidden", null);
            Session session = clients.get(username);
            if (session != null) {
                session.getAsyncRemote().sendText(response.toJson());
            }
        }
    }


}
