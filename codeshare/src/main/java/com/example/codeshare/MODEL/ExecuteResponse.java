package com.example.codeshare.MODEL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteResponse {

    private String stdOut;
    private String stdErr;
    private int exitCode;
}
