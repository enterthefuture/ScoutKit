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
import org.scoutkit.util.ScoutDerbyHelper;

public class ScoutMaster extends JFrame {

    public static final int DEFAULT_SERVER_PORT = 15555;

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    private static Properties prop;

    private static String critAKey = "A";
    private static String critBKey = "B";
    private static String critCKey = "C";
    private static String critDKey = "D";

    private static String statSQL = "SELECT team,COUNT(MATCHNO) AS ENTRIES ,\n" +
                                        "AVG(CAST(A AS FLOAT)) AS AVG_A ,AVG(CAST(B AS FLOAT)) AS AVG_B,\n" +
                                        "AVG(CAST(C AS FLOAT)) AS AVG_C ,AVG(CAST(D AS FLOAT)) AS AVG_D\n" +
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
        super("ScoutMaster: FRC Scouter Manager");

        this.serverPort = serverPort;

        mserver = new ScoutServer();

        statSQL = (prop.containsKey("statSQL")) ? prop.getProperty("statSQL") : statSQL;

        critAKey = (prop.containsKey("critAKey")) ? prop.getProperty("critAKey") : critAKey;
        critBKey = (prop.containsKey("critBKey")) ? prop.getProperty("critBKey") : critBKey;
        critCKey = (prop.containsKey("critCKey")) ? prop.getProperty("critCKey") : critCKey;
        critDKey = (prop.containsKey("critDKey")) ? prop.getProperty("critDKey") : critDKey;

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
        //
        // this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // this.addWindowListener( new WindowAdapter() {
        //
        //     @Override
        //     public void windowClosing(WindowEvent e) {
        //         int confirm = JOptionPane.showOptionDialog(null, "Are You Sure to Close Application?", "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        //         if (confirm == 0) {
        //             dhelper.closeDB();
        //            System.exit(EXIT_SUCCESS);
        //         }
        //     }
        // });

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
        return panel;
    }

    private JMenu getMainMenu() {
        JMenu menu = new JMenu("File");

        JMenuItem exit = new JMenuItem("Exit");

        exit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // dhelper.closeDB();
                        System.exit(EXIT_SUCCESS);
                    }
                }
        );

        menu.add(exit);

        return menu;
    }

    private JPanel getTextAreaPanel() {
        dhelper.openDB();
        JPanel panel = new JPanel(new GridLayout(3,1));
        panel.add(new JScrollPane(outputBox));

        // It creates and displays the table
        ResultSet rs = dhelper.printEntries();
        table = new JTable(buildTableModel(rs));
        table.setEnabled(false);
        panel.add(new JScrollPane(table));

        ResultSet statrs = dhelper.printStats(statSQL);
        stattable = new JTable(buildTableModel(statrs));
        stattable.setEnabled(false);
        panel.add(new JScrollPane(stattable));

        dhelper.closeDB();
        return panel;
    }

    private void commitStats() {
        dhelper.openDB();
        MatchPair match = new MatchPair();

        System.out.println("---Committing---");
        for(Map.Entry<MatchPair, HashMap<String, Integer>> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());

            match = entry.getKey();

            HashMap<String, Integer> teamstats = entry.getValue();
            int high = teamstats.get(critAKey);
            int low = teamstats.get(critBKey);
            int barThrow = teamstats.get(critCKey);
            int barCatch = teamstats.get(critDKey);
            dhelper.insertEntry(match.team, match.match, high, low, barThrow, barCatch );
        }

        ResultSet rs = dhelper.printEntries();
        table.setModel(buildTableModel(rs));

        ResultSet statrs = dhelper.printStats(statSQL);
        stattable.setModel(buildTableModel(statrs));
        stats =
          new HashMap<MatchPair, HashMap<String, Integer>>();
        outputBox.setText("");
        dhelper.closeDB();
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
                        teamstats.put(critBKey, 0);
                        teamstats.put(critAKey, 0);
                        teamstats.put(critCKey, 0);
                        teamstats.put(critDKey, 0);
                        //System.out.println("New Team "+event.teamNo);
                    }

                    int oldstat = teamstats.get(event.attribute);
                    teamstats.put(event.attribute, oldstat + event.value);
                    stats.put(match, teamstats);

                    // outputBox.append(stats.toString() + "\n\n");

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
        prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("ScoutKit.properties");
        try {
            prop.load(stream);
        } catch( IOException ex ) {
            ex.printStackTrace();
        }

        int port = (prop.containsKey("port")) ? Integer.parseInt(prop.getProperty("port")) : ScoutMaster.DEFAULT_SERVER_PORT;

        new ScoutMaster(port).runApp();
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
