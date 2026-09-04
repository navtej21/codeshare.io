package com.example.codeshare.MODEL;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionResultMessage {

    private String type;
    private ExecuteResponse response;
    private List<Boolean> testResults;

}
