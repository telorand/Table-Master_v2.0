import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Matthew on 12/5/2015.
 */
public class CSVFileWriter {
    private static final String COMMA_DELIMITER = ",";
    private static final String NEWLINE_SEPARATOR = "\n";
    private static final String BLANK_LINE = ",,,\n";

    private static final String FILE_HEADER = "First Name,Last Name,Organization,Table";

    public static void writeCSVFile(String fileName, TableMaster tableMaster) throws IOException{
        BufferedWriter fileWriter;

            fileWriter = new BufferedWriter(new FileWriter(fileName));
            fileWriter.append(FILE_HEADER);
            fileWriter.append(NEWLINE_SEPARATOR);

            for(Table table : tableMaster){
                if(table.isFull()) {
                    for (Group group : table) {
                        for (Person person : group) {
                            fileWriter.append(person.getCSVPerson());
                            fileWriter.append(COMMA_DELIMITER);
                            fileWriter.append(String.valueOf(table.getTableNumber()));
                            fileWriter.append(NEWLINE_SEPARATOR);
                        }
                    }
                }
                else {
                    for (Group group : table){
                        for (Person person : group){
                            fileWriter.append(person.getCSVPerson());
                            fileWriter.append(COMMA_DELIMITER);
                            fileWriter.append(String.valueOf(table.getTableNumber()));
                            fileWriter.append(NEWLINE_SEPARATOR);
                        }
                    }
                    for (int i = 0; i < table.chairsLeft(); i++){
                        fileWriter.append("Empty,Chair,,");
                        fileWriter.append(String.valueOf(table.getTableNumber()));
                        fileWriter.append(NEWLINE_SEPARATOR);
                    }
                }
                fileWriter.append(BLANK_LINE);
            }

            fileWriter.flush();
            fileWriter.close();
    }
}
