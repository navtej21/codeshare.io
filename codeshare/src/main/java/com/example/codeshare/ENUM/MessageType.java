package com.example.codeshare.ENUM;

// There are 2 type of messages that can be passed in the room for the current sceario

import org.springframework.stereotype.Component;

@Component
public  class MessageType {
     public final static String JOIN_ROOM ="join_room";
     public final static  String EDIT="edit";
     public final static String RUN="run";
     public final static String SET_TEST_CASES="set_test_cases";
     public static final String PRESENCE_UPDATE = "presence_update";
}
