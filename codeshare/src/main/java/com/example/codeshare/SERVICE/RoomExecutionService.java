package com.example.codeshare.SERVICE;


import com.example.codeshare.MODEL.ExecuteResponse;
import com.example.codeshare.MODEL.ExecutionResultMessage;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class RoomExecutionService {

    private final ObjectMapper objectMapper;
    private final RoomManager roomManager;
    private final CodeExecutionerService codeExecutionerService;


    @Async("codeExecutionExecutor")
    public void runAndBrodcast(String roomId,String language,String code) {

        try {

            System.out.println("runAndBroadcast CALLED for room: " + roomId + ", language: " + language);
            // this will run in the another thread
            ExecuteResponse response = codeExecutionerService.execute(language, code);



            // get all the websocket session sfrom the room manager
            Set<WebSocketSession> sessions = roomManager.getSessions(roomId);

            // get all the testcases from the roomId if available;
            List<String> testCases=roomManager.getTestCases(roomId);
            List<Boolean> testResults=new ArrayList<>();


            // before that we capture the single result
            String result=response.getStdOut()== null ? "":response.getStdOut().trim();


            // check whether the result is matching with that of the testcases
            for(String testcase:testCases){
                if(result.equals(testcase.trim())){
                    testResults.add(true);
                }
                else{
                    testResults.add(false);
                }
            }



            // convert this into an output messsage
            ExecutionResultMessage message = new ExecutionResultMessage();
            message.setType("run");
            message.setResponse(response);
            message.setTestResults(testResults);

            // convert this into a json map
            String json = objectMapper.writeValueAsString(message);

            // brodcast to other websocket session except that to sender just like brodcast

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                    System.out.println("This is the entire message scope"+message);
                    System.out.println("Sent to session: " + session.getId());
                }
                else{
                    System.out.println("Session not open, skipping: " + session.getId());
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
