package com.example.codeshare.SERVICE;


import com.example.codeshare.MODEL.ExecuteResponse;
import com.example.codeshare.MODEL.ExecutionResultMessage;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

@Service
@AllArgsConstructor
public class RoomExecutionService {

    private final ObjectMapper objectMapper;
    private final RoomManager roomManager;
    private final CodeExecutionerService codeExecutionerService;


    @Async("codeExecutionExecutor")
    public void runAndBrodcast(String roomId,String language,String code) throws IOException {

        try {
            // this will run in the another thread
            ExecuteResponse response = codeExecutionerService.execute(language, code);


            // get all the websocket session sfrom the room manager
            Set<WebSocketSession> sessions = roomManager.getSessions(roomId);

            // convert this into an output messsage
            ExecutionResultMessage message = new ExecutionResultMessage();
            message.setType("run");
            message.setResponse(response);

            // convert this into a json map
            String json = objectMapper.writeValueAsString(message);

            // brodcast to other websocket session except that to sender just like brodcast


            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }

        }


        catch (Exception e) {
            // catches JSON serialization failures or anything from execute() itself —
            // logged here because @Async methods swallow exceptions silently otherwise
            System.out.println("runAndBroadcast failed for room " + roomId + ": " + e.getMessage());
        }



    }

}
