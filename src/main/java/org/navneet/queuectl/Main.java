package org.navneet.queuectl;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Database.createTable();
        System.out.println("Table created (or already existed)");
        System.out.println(System.getProperty("user.dir"));
    }
}