package org.scoutkit.scoutmaster;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


import org.scoutkit.util.Message;

public class ScoutMaster extends JFrame {

    public static final int DEFAULT_SERVER_PORT = 15555;

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    private static final String STAT_STRING = "SELECT team,COUNT(MATCHNO) AS ENTRIES ,\n" +
                                        "AVG(CAST(HIGH AS FLOAT)) AS AVG_HIGH ,AVG(CAST(LOW AS FLOAT)) AS AVG_LOW,\n" +
                                        "AVG(CAST(CATCH AS FLOAT)) AS AVG_CATCH ,AVG(CAST(THROW AS FLOAT)) AS AVG_THROW\n" +
                                        "FROM matches\n" +
                                        "GROUP BY team";
    
    private int serverPort;

    private ScoutServer mserver;
    ScoutDerbyHelper dhelper;
    
    private JTextArea outputBox;
    private JButton commit;
    private JButton clear;
    private JTable table;
    private JTable stattable;

    private class MatchPair {
        int match;
        int team;
        public String toString() {
            return team + ":" + match;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof MatchPair)) {
                return false;
            } else {
                MatchPair that = (MatchPair)obj;
                return (this.team == that.team) &&
                    (this.match == that.match);
            }
        }
        public int hashCode() {
            int hash = this.team;
            hash = hash*1000+match;
            return hash;
        }
    }
    
    HashMap<MatchPair, HashMap<String, Integer>> stats
            = new HashMap<MatchPair, HashMap<String, Integer>>();

    public ScoutMaster(int serverPort) {
        super("ScoutMaster: FRC Scout Data Manager");

        this.serverPort = serverPort;
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mserver = new ScoutServer();
        
        commit = new JButton("Commit");
        clear = new JButton("Clear");
        
        dhelper = new ScoutDerbyHelper("scoutDB");
        
        outputBox = new JTextArea();
        outputBox.setEditable(false);
        outputBox.setBorder(new BevelBorder(BevelBorder.RAISED));
        DefaultCaret caret = (DefaultCaret) outputBox.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        buildGUI();
        
        commit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        commitStats();
                    }
                }
        );
        
        clear.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        stats =
                          new HashMap<MatchPair, HashMap<String, Integer>>();
                        outputBox.setText("");
                    }
                }
        );
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener( new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(null, "Are You Sure to Close Application?", "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    dhelper.closeDB();
                   System.exit(EXIT_SUCCESS);
                }
            }
        });
        
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

    private JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        int row = 0;
        panel.setSize(650, 100);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(clear, gbc);
        gbc.gridx = 1;
        panel.add(commit, gbc);
        
//        gbc.fill = GridBagConstraints.NONE;
//        gbc.gridy = row++;
//        gbc.gridx = 0;
//        gbc.gridwidth = 2;
//        gbc.anchor = GridBagConstraints.CENTER;
//        panel.add(new JLabel("Database Configuration:"), gbc);
//        gbc.gridwidth = 1;
//
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.ipadx = 20;
//        gbc.gridy = row++;
//        gbc.gridx = 0;
//        panel.add(new JLabel("Database Name:"), gbc);
//        gbc.gridx = 1;
//        panel.add(dbName, gbc);
//
//        gbc.fill = GridBagConstraints.NONE;
//        gbc.gridy = row++;
//        gbc.gridx = 0;
//        gbc.gridwidth = 2;
//        gbc.anchor = GridBagConstraints.CENTER;
//        panel.add(commit, gbc);
//        gbc.gridwidth = 1;
//
        return panel;
    }

    private JMenu getMainMenu() {
        JMenu menu = new JMenu("File");

        JMenuItem exit = new JMenuItem("Exit");

        exit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dhelper.closeDB();
                        System.exit(EXIT_SUCCESS);
                    }
                }
        );

        menu.add(exit);

        return menu;
    }

    private JPanel getTextAreaPanel() {
        JPanel panel = new JPanel(new GridLayout(3,1));
        panel.add(new JScrollPane(outputBox));
        
        // It creates and displays the table
        ResultSet rs = dhelper.printEntries();
        table = new JTable(buildTableModel(rs));
        table.setEnabled(false);
        panel.add(new JScrollPane(table));

        ResultSet stats = dhelper.printStats(STAT_STRING);
        stattable = new JTable(buildTableModel(stats));
        stattable.setEnabled(false);
        panel.add(new JScrollPane(stattable));
        
        return panel;
    }

    private void commitStats() {
        MatchPair match = new MatchPair();
        
        System.out.println("---Committing---");
        for(Map.Entry<MatchPair, HashMap<String, Integer>> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());
            
            match = entry.getKey();

            HashMap<String, Integer> teamstats = entry.getValue();
            int high = teamstats.get("high");
            int low = teamstats.get("low");
            int barThrow = teamstats.get("throw");
            int barCatch = teamstats.get("catch");
            dhelper.insertEntry(match.team, match.match, high, low, barThrow, barCatch );
            
            ResultSet rs = dhelper.printEntries();
            table.setModel(buildTableModel(rs));
             
            ResultSet stats = dhelper.printStats(STAT_STRING);
            stattable.setModel(buildTableModel(stats));
        }
        stats =
          new HashMap<MatchPair, HashMap<String, Integer>>();
        outputBox.setText("");
    }
    
    private class ScoutServer extends Thread {

        private ServerSocket server;
        private Socket client;

        public ScoutServer() {
            try {
                server = new ServerSocket(serverPort);
            } catch (Exception e) {
                System.err.println(e);

                System.exit(EXIT_FAILURE);
            }
        }

        public void run() {
            while (true) {
                try {

                    HashMap<String, Integer> teamstats = new HashMap<String, Integer>();
                    MatchPair match = new MatchPair();
                    client = server.accept();
                    Message event = (Message) (new ObjectInputStream(client.getInputStream())).readObject();
                    outputBox.append(event + "\n");
                    
                    match.team = event.teamNo;
                    match.match = event.match;
                    
                    if (stats.containsKey(match)) {
                        teamstats = stats.get(match);
                        //System.out.println("Team "+event.teamNo);
                    } else {
                        teamstats.put("low", 0);
                        teamstats.put("high", 0);
                        teamstats.put("throw", 0);
                        teamstats.put("catch", 0);
                        //System.out.println("New Team "+event.teamNo);
                    }

                    int oldstat = teamstats.get(event.attribute);
                    teamstats.put(event.attribute, oldstat + event.value);
                    stats.put(match, teamstats);

                    outputBox.append(stats.toString() + "\n\n");

                    client.close();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }

    public void runApp() {
        mserver.start();
    }

    public static void main(String[] args) {
        (new ScoutMaster((args.length == 0) ? ScoutMaster.DEFAULT_SERVER_PORT : Integer.parseInt(args[0]))).runApp();
    }
    
    public static DefaultTableModel buildTableModel(ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();

            // names of columns
            Vector<String> columnNames = new Vector<String>();
            int columnCount = metaData.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
            }

            // data of the table
            Vector<Vector<Object>> data = new Vector<Vector<Object>>();
            while (rs.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    vector.add(rs.getObject(columnIndex));
                }
                data.add(vector);
            }
            return new DefaultTableModel(data, columnNames);
        } catch(SQLException sqle) {
            ScoutDerbyHelper.printSQLException(sqle);
            return null;
        }
    }
}
