package org.scoutkit.scoutscore;

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

import org.scoutkit.util.ScoutDerbyHelper;

public class ScoutScore extends JFrame {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    private static Properties prop;

    private static String statSQL = "SELECT team,COUNT(MATCHNO) AS ENTRIES ,\n" +
                                        "AVG(CAST(A AS FLOAT)) AS AVG_A ,AVG(CAST(B AS FLOAT)) AS AVG_B,\n" +
                                        "AVG(CAST(C AS FLOAT)) AS AVG_C ,AVG(CAST(D AS FLOAT)) AS AVG_D\n" +
                                        "FROM matches\n" +
                                        "GROUP BY team";

    private ScoutDerbyHelper dhelper;

    private JTextArea sqlBox;
    private JButton update;
    private JButton reset;
    private JTable table;
    private JTable stattable;

    public ScoutScore() {
        super("ScoutScore: FRC Scouter Data Analysis");

        statSQL = (prop.containsKey("statSQL")) ? prop.getProperty("statSQL") : statSQL;

        dhelper = new ScoutDerbyHelper("scoutDB");

        sqlBox = new JTextArea(statSQL);
        sqlBox.setBorder(new BevelBorder(BevelBorder.RAISED));
        sqlBox.setLineWrap(true);

        update = new JButton("Update");
        reset = new JButton("Reset");

        update.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateStats();
                    }
                }
        );

        reset.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sqlBox.setText(statSQL);
                        updateStats();
                    }
                }
        );

        buildGUI();
        // this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // this.addWindowListener( new WindowAdapter() {
        //
        //     @Override
        //     public void windowClosing(WindowEvent e) {
        //         int confirm = JOptionPane.showOptionDialog(null, "Are You Sure to Close Application?", "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        //         if (confirm == 0) {
        //            dhelper.closeDB();
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
        panel.add(reset, gbc);
        gbc.gridx = 1;
        panel.add(update, gbc);
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
        panel.add(new JScrollPane(sqlBox));

        // It creates and displays the table
        ResultSet raw = dhelper.printEntries();
        table = new JTable(buildTableModel(raw));
        table.setEnabled(false);
        panel.add(new JScrollPane(table));

        ResultSet stats = dhelper.printStats(statSQL);
        stattable = new JTable(buildTableModel(stats));
        stattable.setEnabled(false);
        panel.add(new JScrollPane(stattable));

        dhelper.closeDB();
        return panel;
    }

    private void updateStats() {
        dhelper.openDB();
        ResultSet rs = dhelper.printEntries();
        table.setModel(buildTableModel(rs));
        ResultSet stats = dhelper.printStats(sqlBox.getText());
        if(stats != null){
          stattable.setModel(buildTableModel(stats));
        } else {
          JOptionPane.showMessageDialog(this, dhelper.getLastError(), "SQL Error", JOptionPane.WARNING_MESSAGE);
        }
        dhelper.closeDB();
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

        ScoutScore app = new ScoutScore();
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
