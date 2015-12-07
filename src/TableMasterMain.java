import javax.swing.*;

/**
 * Created by Matthew on 12/2/2015.
 */
public class TableMasterMain {
    public static void main(String[] args) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        TableMasterGUI newGui = new TableMasterGUI();
    }
}
