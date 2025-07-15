package latihan;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        var main = new Main();
        main.startServer();
    }

    public void startServer() {
        var server = new WebServer(this, 9000);
        try {
            server.initialize();
            server.start();
            log("Server started on port 9000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String text) {
        var fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        var now = LocalDateTime.now().format(fmt);
        System.out.printf("%s> %s%n", now, text);
    }
}
