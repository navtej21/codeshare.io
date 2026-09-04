package com.example.codeshare.CONFIG;

import com.example.codeshare.ENUM.MessageType;
import com.example.codeshare.MODEL.RoomMessage;
import com.example.codeshare.SERVICE.CodeExecutionerService;
import com.example.codeshare.SERVICE.RoomExecutionService;
import com.example.codeshare.SERVICE.RoomManager;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;


// since the data we are transimiting is of json/ string we extend with the textwebsocketclass
@Component
@AllArgsConstructor
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final RoomManager roomManager;
    private final ObjectMapper objectMapper;
    private final RoomExecutionService roomExecutionService;





    // this is the crux of the websocket handling
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
       RoomMessage incoming= objectMapper.readValue(message.getPayload(),RoomMessage.class);

       if (MessageType.JOIN_ROOM.equals(incoming.getType())){
           roomManager.joinRoom(incoming.getRoomId(),session);
       }
       else if(MessageType.EDIT.equals(incoming.getType())){
           brodcastToRoomExceptSender(session,incoming.getRoomId(), incoming.getContent());
       }

       else if(MessageType.RUN.equals(incoming.getType())){
          roomExecutionService.runAndBrodcast(incoming.getRoomId(),incoming.getLanguage(),incoming.getContent());
       }
    }


    private void brodcastToRoomExceptSender(WebSocketSession webSocketSession,String roomId,String content){

        // retrieve all the websocketsessions
        Set<WebSocketSession> sessions=roomManager.getSessions(roomId);
        RoomMessage outgoing=new RoomMessage(MessageType.EDIT,roomId,content,null);

        for(WebSocketSession session:sessions){
            if(!session.getId().equals(webSocketSession.getId()) && session.isOpen()){
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(outgoing)));
                } catch (IOException e) {
                    // a send failure to one client shouldn't stop delivery to the rest
                }
            }
        }
    }




    // specifically after closing the connection
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        roomManager.removeSession(session);
    }
}
