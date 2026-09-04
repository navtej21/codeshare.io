package com.example.codeshare.MODEL;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


// The Input Message A User Sends To The Server Before The Server Brodcasts To Other Users In The Room

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomMessage {
    private String type;
    private String roomId;
    private String content;
    private String language;
    private List<String> testCase;
}
