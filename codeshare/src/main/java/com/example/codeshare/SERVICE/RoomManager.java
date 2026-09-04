package com.example.codeshare.SERVICE;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RoomManager {


    // to create a room Map with multiple WebSocketSessions - > everyone currently connected to that roo,
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomIds=new ConcurrentHashMap<>();

    private final ConcurrentHashMap<WebSocketSession,String> sessionToRoom=new ConcurrentHashMap<>();


    // carry a set of testcases
    private final ConcurrentHashMap<String, List<String>> roomTestCases=new ConcurrentHashMap<>();



    // create a join room Session Method
    public void joinRoom(String roomId,WebSocketSession session){
        Set<WebSocketSession> sessions=roomIds.computeIfAbsent(roomId,key->ConcurrentHashMap.newKeySet());
        sessions.add(session);
        System.out.println(roomIds);
        System.out.println(sessionToRoom);
        sessionToRoom.put(session,roomId);
    }


    // get the sessions ids of the room numebr
    public Set<WebSocketSession> getSessions(String roomId){
        return roomIds.get(roomId);
    }


    // removing a room session Method
    public void removeSession(WebSocketSession session) {
        if (sessionToRoom.containsKey(session)) {

            String roomId = sessionToRoom.get(session);

            Set<WebSocketSession> sessions = roomIds.get(roomId);

            sessions.remove(session);

            sessionToRoom.remove(session);

            if (sessions.isEmpty()) {
                roomIds.remove(roomId, sessions);
            }
        }
    }


    // set test cases for that particular roomId
    public void setTestCases(String roomId,List<String> testCases){
        roomTestCases.put(roomId,testCases);
    }


    // get the test cases for that particular roomId
    public List<String> getTestCases(String roomId){
        return roomTestCases.getOrDefault(roomId, Collections.emptyList());
    }

}
