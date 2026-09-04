package com.example.codeshare.SERVICE;

import com.example.codeshare.MODEL.ExecuteResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionerService {

    private static final int TIMEOUT_SECONDS = 10;
    private static final String PYTHON_IMAGE = "python:3.11-slim";
    private static final String JAVA_IMAGE = "eclipse-temurin:17-jdk-alpine";

    public ExecuteResponse execute(String language, String code) {
        try {

            Path workDir = Files.createTempDirectory("exec-" + UUID.randomUUID());
            return switch (language.toLowerCase()) {
                case "python" -> runPython(workDir, code);
                case "java" -> runJava(workDir,code);
                default -> new ExecuteResponse("", "Unsupported language: " + language, -1);
            };
        } catch (IOException e) {
            return new ExecuteResponse("", "Internal error: " + e.getMessage(), -1);
        }
    }


    // since the java is not interpreter lang so we first compile it and then execute it (compiler driven)
    private ExecuteResponse runJava(Path workDir, String code) throws IOException {
        String className = extractPublicClassName(code);
        if (className == null) {
            return new ExecuteResponse("", "Could not find a public class in submitted code", -1);
        }

        // generate a random container name
        String containerName=UUID.randomUUID().toString().replace("-","");
        // resolve the path
        Path javaFile = workDir.resolve(className + ".java");
        // write the content into the file
        Files.writeString(javaFile, code);
        ProcessBuilder pb=dockerCommand(workDir,JAVA_IMAGE,containerName,"java","main.java");
        return runProcess(pb,containerName);
    }

    private String extractPublicClassName(String code) {
        // naive regex — good enough for Phase 1, not production-grade parsing
        var matcher = java.util.regex.Pattern.compile("public\\s+class\\s+(\\w+)").matcher(code);
        return matcher.find() ? matcher.group(1) : null;
    }





    private ExecuteResponse runPython(Path workDir, String code) throws IOException {
        System.out.println("This is working");
        Path scriptFile = workDir.resolve("script.py");
        Files.writeString(scriptFile, code);
        String containerName="exec-"+UUID.randomUUID().toString().replace("-","");
        ProcessBuilder pb=dockerCommand(workDir,PYTHON_IMAGE,containerName,"python","script.py");
        return runProcess(pb,containerName);
    }



    private ProcessBuilder dockerCommand(Path workDir, String image,String containerName, String... entrypointArgs){
        var command= new ArrayList<String>();
        command.add("docker");
        command.add("run");
        command.add("--rm");
        command.add("--name");
        command.add(containerName);
        command.add("--memory=128m");
        command.add("--cpus=0.5");
        command.add("--pids-limit=50");
        command.add("--network=none");
        command.add("-v");
        command.add(toDockerMountPath(workDir)+":/sandbox");
        command.add("-w");
        command.add("/sandbox");
        command.add(image);
        command.addAll(Arrays.asList(entrypointArgs));
        return new ProcessBuilder(command);
    }


    private String toDockerMountPath(Path path) {
        String p = path.toAbsolutePath().toString().replace("\\", "/");
        // C:/Users/... -> //c/Users/...  (Docker Desktop's expected format on Windows)
        if (p.length() > 1 && p.charAt(1) == ':') {
            p = "/" + Character.toLowerCase(p.charAt(0)) + p.substring(2);
        }
        return p;
    }


    private ExecuteResponse runProcess(ProcessBuilder pb,String containerName) throws IOException {

        pb.redirectErrorStream(false);
        Process process = pb.start();

        try {
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                try {
                    new ProcessBuilder("docker", "kill", containerName).start().waitFor(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                process.destroyForcibly();
                return new ExecuteResponse("", "Execution timed out after " + TIMEOUT_SECONDS + "s", -1);
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());
            return new ExecuteResponse(stdout, stderr, process.exitValue());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ExecuteResponse("", "Execution interrupted", -1);
        }
    }
}
