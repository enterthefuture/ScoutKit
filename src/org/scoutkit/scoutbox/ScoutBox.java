package org.scoutkit.scoutbox;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scoutkit.util.Message;

public class ScoutBox extends JFrame {

    public static final int DEFAULT_SERVER_PORT = 15555;

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static int serverPort;

    private static Properties prop;

    private static String critAText = "A";
    private static String critBText = "B";
    private static String critCText = "C";
    private static String critDText = "D";

    private static String critAKey = "A";
    private static String critBKey = "B";
    private static String critCKey = "C";
    private static String critDKey = "D";
    //private MessageServer mserver;

    private JTextArea outputBox;

    private JTextField host;
    private JTextField port;
    private JTextField scoutID;
    private JTextField teamNo;
    private JTextField match;
    private JTextField comment;

    private JButton send;
    private JButton sendComment;
    private JButton clear;

    private JButton critA;
    private JButton critB;
    private JButton critC;
    private JButton critD;


    public ScoutBox(int serverPort) {
        super("ScoutBox: A FRC Scouting System");

        this.serverPort = serverPort;

        //mserver = new MessageServer();

        host = new JTextField("127.0.0.1", 15);
        port = new JTextField(Integer.toString(serverPort), 5);
        scoutID = new JTextField(10);
        teamNo = new JTextField(5);
        match = new JTextField(5);
        comment = new JTextField(15);

        outputBox = new JTextArea();
        outputBox.setEditable(false);

        ActionListener sendmsg = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendUp(e.getActionCommand());
            }
        };

        critAText = (prop.containsKey("critAText")) ? prop.getProperty("critAText") : critAText;
        critBText = (prop.containsKey("critBText")) ? prop.getProperty("critBText") : critBText;
        critCText = (prop.containsKey("critCText")) ? prop.getProperty("critCText") : critCText;
        critDText = (prop.containsKey("critDText")) ? prop.getProperty("critDText") : critDText;

        critAKey = (prop.containsKey("critAKey")) ? prop.getProperty("critAKey") : critAKey;
        critBKey = (prop.containsKey("critBKey")) ? prop.getProperty("critBKey") : critBKey;
        critCKey = (prop.containsKey("critCKey")) ? prop.getProperty("critCKey") : critCKey;
        critDKey = (prop.containsKey("critDKey")) ? prop.getProperty("critDKey") : critDKey;

        critA = new JButton("1. "+critAText);
        critA.setMnemonic(KeyEvent.VK_1);
        critA.setActionCommand("0");
        critA.addActionListener(sendmsg);

        critB = new JButton("2. "+critBText);
        critB.setMnemonic(KeyEvent.VK_2);
        critB.setActionCommand("1");
        critB.addActionListener(sendmsg);

        critC = new JButton("3. "+critCText);
        critC.setMnemonic(KeyEvent.VK_3);
        critC.setActionCommand("2");
        critC.addActionListener(sendmsg);

        critD = new JButton("4. "+critDText);
        critD.setMnemonic(KeyEvent.VK_4);
        critD.setActionCommand("3");
        critD.addActionListener(sendmsg);

        send = new JButton("Send");
        send.setMnemonic(KeyEvent.VK_S);

        clear = new JButton("Clear");
        clear.setMnemonic(KeyEvent.VK_L);

        sendComment = new JButton("Send Comment");
        sendComment.setMnemonic(KeyEvent.VK_C);
        sendComment.setActionCommand("comment");
        sendComment.addActionListener(sendmsg);

        outputBox.setBorder(new BevelBorder(BevelBorder.RAISED));

        clear.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        clearFields();
                    }
                }
        );

        buildGUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(650, 500);

        setVisible(true);
    }

    private void buildGUI() {
        JMenuBar menubar = new JMenuBar();

        menubar.add(getMainMenu());

        setJMenuBar(menubar);

        Container c = getContentPane();

        c.setLayout(new BorderLayout());

        c.add(getTextAreaPanel(), BorderLayout.CENTER);

        c.add(getMainPanel(), BorderLayout.SOUTH);
    }

    private JMenu getMainMenu() {
        JMenu menu = new JMenu("File");

        JMenuItem exit = new JMenuItem("Exit");

        exit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(EXIT_SUCCESS);
                    }
                }
        );

        menu.add(exit);

        return menu;
    }

    private JPanel getTextAreaPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JScrollPane(outputBox));
        return panel;
    }

    private JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        int row = 0;
        panel.setSize(650, 100);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(new JLabel("Server Settings:"), gbc);
        gbc.gridwidth = 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Remote Host:"), gbc);
        gbc.gridx = 1;
        panel.add(host, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Remote Port:"), gbc);
        gbc.gridx = 1;
        panel.add(port, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Your Name:"), gbc);
        gbc.gridx = 1;
        panel.add(scoutID, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(new JLabel("Data Input:"), gbc);
        gbc.gridwidth = 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Team Number:"), gbc);
        gbc.gridx = 1;
        panel.add(teamNo, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Match Number:"), gbc);
        gbc.gridx = 1;
        panel.add(match, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(critA, gbc);
        gbc.gridx = 1;
        panel.add(critB, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(critC, gbc);
        gbc.gridx = 1;
        panel.add(critD, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(new JLabel("Comment:"), gbc);
        gbc.gridwidth = 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(comment, gbc);
        gbc.gridx = 1;
        panel.add(sendComment, gbc);
        return panel;
    }

    private void sendUp(String stat) {
        try {
            Socket remoteHost = new Socket(host.getText(), Integer.parseInt(port.getText()));

            ObjectOutputStream output = new ObjectOutputStream(remoteHost.getOutputStream());
            Message m;
            if(stat == "comment") {
              m = new Message(scoutID.getText(), Integer.parseInt(teamNo.getText()), Integer.parseInt(match.getText()), stat, comment.getText());
            } else {
              m = new Message(scoutID.getText(), Integer.parseInt(teamNo.getText()), Integer.parseInt(match.getText()), stat, "1");
            }

            output.writeObject(m);

            output.flush();
            output.close();

            outputBox.append(m.toString() + "\n\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to send your message. Perhaps the destination's IP address, hostname, \nor tcp port number is incorrect?", "Network Communication Error", JOptionPane.WARNING_MESSAGE);

            System.err.println(e);
        }
    }


    private void clearFields() {
        teamNo.setText(null);
    }

    private class MessageServer extends Thread {

        private ServerSocket server;
        private Socket client;

        public MessageServer() {
            try {
                server = new ServerSocket(serverPort);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(ScoutBox.this, "Unable to create server socket.", "Local Host Server Error", JOptionPane.WARNING_MESSAGE);

                System.err.println(e);

                System.exit(EXIT_FAILURE);
            }
        }

        public void run() {
            while (true) {
                try {
                    client = server.accept();

                    outputBox.append((new ObjectInputStream(client.getInputStream())).readObject().toString() + "\n\n");

                    client.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ScoutBox.this, "Error accepting client connection.", "Network Communication Error", JOptionPane.WARNING_MESSAGE);

                    System.err.println(e);
                }
            }
        }
    }

    public void runApp() {
        //mserver.start();
    }

    public static void main(String[] args) {
        prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("ScoutKit.properties");
        try {
            prop.load(stream);
        } catch( IOException ex ) {
            ex.printStackTrace();
        }


        int port = (prop.containsKey("port")) ? Integer.parseInt(prop.getProperty("port")) : ScoutBox.DEFAULT_SERVER_PORT;
        new ScoutBox(port).runApp();
    }
}
