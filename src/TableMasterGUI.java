import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Matthew on 10/19/2015.
 */
public class TableMasterGUI {
    private JFormattedTextField NumberOfSponsors;
    private JTextField CSVPath;
    private JButton CSVSelector;
    private JTextField SponsorNames;
    private JButton cancelButton;
    private JButton goButton;

    private TableMaster tableMaster;
    private int numberOfSponsors;
    private File csvPath;
    private ArrayDeque<String> sponsorNames;

    public TableMasterGUI() {
        NumberFormat nf = NumberFormat.getIntegerInstance(); // Specify specific format here.
        NumberFormatter nff = new NumberFormatter(nf);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
        NumberOfSponsors.setFormatterFactory(factory);

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(CSVPath.getText() != null && SponsorNames.getText() != null
                        && NumberOfSponsors.getText() != null){
                    numberOfSponsors = Integer.parseInt(NumberOfSponsors.getText());
                    csvPath = new File(CSVPath.getText());
                    String[] s = SponsorNames.getText().split("\\s*,\\s*");
                    for (String sponsor : s){
                        sponsorNames.addLast(sponsor);
                    }
                    tableMaster = new TableMaster(numberOfSponsors,sponsorNames);
                    tableMaster.readCSVFile(csvPath);
                    tableMaster.autoFillAllChairs();
                    // TODO Generate graphic representation of Tables and Persons
                }
            }
        });
    }
}
