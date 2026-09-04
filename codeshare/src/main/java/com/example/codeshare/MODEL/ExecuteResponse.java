package com.example.codeshare.MODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteResponse {

    private String stdOut;
    private String stdErr;
    private int exitCode;
}
