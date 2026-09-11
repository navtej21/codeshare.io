package com.example.codeshare.MODEL;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PressenceMessage {
    private String type;
    private int count;
}
