package latihan;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import javax.swing.*;

public class Main extends JFrame implements WindowListener {

    private final WebServer server;
    private JTextArea textArea;
    private JToggleButton toggleButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var main = new Main();
            main.createGUI();
            main.setVisible(true);
        });
    }

    public Main() {
        server = new WebServer(this, 9000);
    }

    private void createGUI() {
        var miExit = new JMenuItem("Exit");
        miExit.addActionListener(_ -> dispose());
        var mFile = new JMenu("File");
        mFile.add(miExit);
        var menuBar = new JMenuBar();
        menuBar.add(mFile);

        toggleButton = new JToggleButton("Start");
        toggleButton.setEnabled(false);
        toggleButton.addItemListener(e -> {
            switch (e.getStateChange()) {
                case ItemEvent.SELECTED -> {
                    server.start();
                    log("Server started");
                    toggleButton.setText("Stop");
                }
                case ItemEvent.DESELECTED -> {
                    server.stop();
                    log("Server stopped");
                    toggleButton.setText("Start");
                }
            }
        });
        var toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(toggleButton);

        textArea = new JTextArea();
        textArea.setEditable(false);
        var scrollPane = new JScrollPane(textArea);

        var size = new Dimension(640, 480);
        setTitle("Latihan Web Server");
        setSize(size);
        setMinimumSize(size);
        setLocationRelativeTo(null);
        setJMenuBar(menuBar);
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(this);
    }

    public void log(String text) {
        var fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        var now = LocalDateTime.now().format(fmt);
        textArea.append(String.format("%s> %s%n", now, text));
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
        if (server.isStarted()) {
            server.stop();
        }
        System.exit(0);
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
        try {
            server.initialize();
            toggleButton.setEnabled(true);
            log("Server ready");
        } catch (Exception exc) {
            log(exc.getMessage());
        }
    }
}
