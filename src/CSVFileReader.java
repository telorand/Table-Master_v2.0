import org.apache.commons.csv.*;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Matthew on 10/11/2015.
 */
public class CSVFileReader {
    //CSV file header
    private static final String[] FILE_HEADER_MAPPING = {"First Name", "Last Name", "Organization"};

    //Person attributes
    private static final String FIRST_NAME = "First Name";
    private static final String LAST_NAME = "Last Name";
    private static final String ORGANIZATION = "Organization";

    private static class OrganizationCompare implements Comparator<Person> {
        @Override
        public int compare(Person p1, Person p2) {

            if (p1.getOrganization().equalsIgnoreCase(p2.getOrganization())) {
                return 0;
            }
            else if (p1.getOrganization().compareToIgnoreCase(p2.getOrganization()) > 0) {
                return 1;
            }
            else
                return -1;
        }
    }

    public static List<Person> readCsvFile(File selectedFile){
        FileReader fileReader;
        CSVParser parser;
        List<Person> people = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.EXCEL.withHeader(FILE_HEADER_MAPPING);

        try {
            fileReader = new FileReader(selectedFile);
            parser = new CSVParser(fileReader, csvFormat);
            List<CSVRecord> csvRecords = parser.getRecords();

            for (int i = 1; i < csvRecords.size(); i++){
                CSVRecord record = csvRecords.get(i);
                Person person = new Person(record.get(FIRST_NAME), record.get(LAST_NAME), record.get(ORGANIZATION));
                people.add(person);
            }
            Collections.sort(people, new OrganizationCompare());
            fileReader.close();
            parser.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return people;
    }
}
