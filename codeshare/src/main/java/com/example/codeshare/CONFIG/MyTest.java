package com.example.codeshare.CONFIG;


import com.example.codeshare.MODEL.ExecuteResponse;
import com.example.codeshare.SERVICE.CodeExecutionerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class  MyTest {

    @Bean
    public CommandLineRunner testExecution(CodeExecutionerService service){
        return args -> {
            ExecuteResponse response=service.execute("python","print(42)");
            System.out.println(response.getStdOut());
        };
    }
}
