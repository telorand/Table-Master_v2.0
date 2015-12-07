import com.jgoodies.forms.layout.FormLayout;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Created by Matthew on 10/19/2015.
 */
public class TableMasterGUI extends JFrame {
    private JFormattedTextField NumberOfSponsors;
    private JTextField CSVPath;
    private JButton CSVSelector;
    private JTextField SponsorNames;
    private JButton cancelButton;
    private JButton goButton;
    private JPanel tmPanel;

    private TableMaster tableMaster;
    private int numberOfSponsors;
    private File csvPath;
    private ArrayDeque<String> sponsorNames;
    private final JFileChooser fc;
    private FileNameExtensionFilter csvFilter;

    public TableMasterGUI() {
        super("Table Master");
        setContentPane(tmPanel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        NumberFormat nf = NumberFormat.getIntegerInstance(); // Specify specific format here.
        NumberFormatter nff = new NumberFormatter(nf);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
        NumberOfSponsors.setFormatterFactory(factory);

        csvFilter = new FileNameExtensionFilter("Comma Separated Value (.csv)", "csv", "CSV");
        fc = new JFileChooser();
        fc.setFileFilter(csvFilter);

        //changeLaf(this);

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (CSVPath.getText().isEmpty() || (SponsorNames.getText().isEmpty()
                        ^ NumberOfSponsors.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(tmPanel, "Please enter all fields.");
                    if (CSVPath.getText().isEmpty()) {
                        CSVPath.grabFocus();
                        //CSVPath.setCaretPosition(0);
                    }
                    if (SponsorNames.getText().isEmpty() && NumberOfSponsors.getText().length() != 0) {
                        SponsorNames.grabFocus();
                        //SponsorNames.setCaretPosition(0);
                    }
                    if (NumberOfSponsors.getText().isEmpty() && SponsorNames.getText().length() != 0) {
                        NumberOfSponsors.grabFocus();
                    }
                } else {
                    if (NumberOfSponsors.getText().isEmpty())
                        numberOfSponsors = 0;
                    else
                        numberOfSponsors = Integer.parseInt(NumberOfSponsors.getText());
                    csvPath = new File(CSVPath.getText());
                    int sponsorLength;
                    String[] s = SponsorNames.getText().split("\\s*,\\s*");
                    if (SponsorNames.getText().isEmpty()) {
                        sponsorLength = 0;
                    } else {
                        sponsorLength = s.length;
                    }
                    if (numberOfSponsors != sponsorLength) {
                        JOptionPane.showMessageDialog(fc, "Number of sponsors don't match. Please try again.");
                    } else {
                        if (numberOfSponsors == 0) {
                            tableMaster = new TableMaster();
                        } else {
                            sponsorNames = new ArrayDeque<>(numberOfSponsors);
                            Collections.addAll(sponsorNames, s);
                            tableMaster = new TableMaster(numberOfSponsors, sponsorNames);
                        }
                        tableMaster.readCSVFile(csvPath);
                        tableMaster.autoFillAllChairs();
                        // TODO Generate graphic representation of Tables and Persons

                        int returnVal = fc.showSaveDialog(tmPanel);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                CSVFileWriter.writeCSVFile(fc.getSelectedFile().getAbsolutePath(), tableMaster);
                                JOptionPane.showMessageDialog(null, "Success!");
                                System.exit(1);
                            } catch (IOException z) {
                                JOptionPane.showMessageDialog(fc, "Error." +
                                        "\nCannot access file because it is being used by another process." +
                                        "\nPlease try again.");
                            }
                        } else {
                            JOptionPane.showMessageDialog(fc, "Invalid filename. Please try again.");
                        }
                    }
                }
            }
        });

        CSVSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(tmPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (FilenameUtils.getExtension(fc.getSelectedFile().getPath()).equalsIgnoreCase("csv")) {
                        csvPath = fc.getSelectedFile();
                        CSVPath.setText(csvPath.getAbsolutePath());
                    } else
                        JOptionPane.showMessageDialog(tmPanel, "Please select a valid CSV file.");
                }
            }
        });


        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        tmPanel = new JPanel();
        tmPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 5, new Insets(10, 10, 10, 10), -1, -1));
        NumberOfSponsors = new JFormattedTextField();
        NumberOfSponsors.setText("");
        NumberOfSponsors.setToolTipText("Number of tables that have been sponsored.");
        tmPanel.add(NumberOfSponsors, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(30, 20), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        tmPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(45, 14), null, 0, false));
        CSVPath = new JTextField();
        CSVPath.setText("");
        CSVPath.setToolTipText("Path to a valid CSV file.");
        tmPanel.add(CSVPath, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        CSVSelector = new JButton();
        CSVSelector.setHorizontalTextPosition(11);
        CSVSelector.setMargin(new Insets(2, 2, 2, 2));
        CSVSelector.setText("...");
        tmPanel.add(CSVSelector, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(30, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("CSV File");
        tmPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setHorizontalTextPosition(11);
        label2.setText("# of Sponsored Tables");
        label2.setVerticalAlignment(0);
        label2.setVerticalTextPosition(0);
        tmPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SponsorNames = new JTextField();
        SponsorNames.setName("Sponsors");
        SponsorNames.setText("");
        SponsorNames.setToolTipText("Separate names with commas. (eg. Org A, Org B, etc.)");
        tmPanel.add(SponsorNames, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Sponsors");
        tmPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setFocusable(false);
        tmPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setFocusable(false);
        label4.setRequestFocusEnabled(false);
        label4.setText("Table Master Setup");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        tmPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(200, 11), null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        tmPanel.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(5, 2, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        goButton = new JButton();
        goButton.setText("Go!");
        tmPanel.add(goButton, new com.intellij.uiDesigner.core.GridConstraints(5, 3, 3, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        tmPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 30), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        tmPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        label1.setLabelFor(CSVPath);
        label2.setLabelFor(NumberOfSponsors);
        label3.setLabelFor(SponsorNames);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return tmPanel;
    }
}
