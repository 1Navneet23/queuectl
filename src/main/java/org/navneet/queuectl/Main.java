package org.navneet.queuectl;

public class Main {

    public static void main(String[] args) {

        ExecutionResult result =
                CommandExecutor.executeCommand("asdfasdf");

        System.out.println("Exit Code: " + result.getExitCode());
        System.out.println("Success: " + result.isSuccess());

        System.out.println("Output:");
        System.out.println(result.getOutput());
    }
}