import org.apache.commons.csv.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 10/5/2015.
 */
public class TableMaster implements TableMasterInterface<Person,TableMaster.Table> {
    private static final int DEFAULT_TABLE_SEATING = 8;
    // The following is better, because I will need to access specific Tables without iterating through the whole list.
    private ArrayList<Table> tables;
    private File csvFile;
    private int numberOfTables;
    private List<Person> peopleList;
    private int numberOfSponsorTables;
    private int nonSponsorIndex = numberOfSponsorTables; // Table indices start at 0, so this is correct.

    TableMaster(int numberOfSponsorTables) {
        if (FilenameUtils.getExtension(csvFile.getPath()).equalsIgnoreCase("csv")) {
            peopleList = readCSVFile(csvFile);
            numberOfTables = (int) (peopleList.size() / 8 + .5); // Round up
            setNumberOfSponsorTables(numberOfSponsorTables);
            tables = new ArrayList<>(numberOfTables);
            addTables(this.numberOfSponsorTables);
            loadParty(peopleList);
        }
        else {
            // Could also throw an exception, instead.
            System.out.println("That is not a valid CSV file. Please try a different one.");
        }
    }

    protected class Table{
        private ArrayList<Person> chairs; //An ArrayList that will contain Person objects. Represents the chairs (or Person's) around the table.
        private int maxNumberOfChairs; //Defined by the size of the ArrayList. Represents all chairs, open or filled.
        private String sponsorName; //The name of the sponsor.
        boolean isSponsor = false;

        Table() {
            this.chairs = new ArrayList<>(DEFAULT_TABLE_SEATING);
            this.maxNumberOfChairs = DEFAULT_TABLE_SEATING;
        }
        Table(int numberOfChairs){
            this.chairs = new ArrayList<>(numberOfChairs);
            this.maxNumberOfChairs = numberOfChairs;
        }

        protected int getNumberOfFilledChairs(){
            return this.chairs.size();
        }
        protected int chairsLeft() {
            return maxNumberOfChairs - getNumberOfFilledChairs();
        }
        protected String getSponsorName() {
            return sponsorName;
        }
        boolean isFull(){
            return chairs.size() == maxNumberOfChairs;
        }
    }

    public void initializeTableMaster(File csvFile, int numberOfSponsorTables) {

    }

    public void setCSVFile(File csvFile){
        this.csvFile = csvFile;
    }

    private List<Person> readCSVFile(File csvFile){
        return CSVFileReader.readCsvFile(csvFile);
    }

    /**
     * This is an overload of the autoFillChair() method, but will probably be the primary method used.
     * @param person The Person being seated.
     */
    public void autoFillChair(Person person){
        autoFillChair(null, person);
    }

    /**
     * Places a Person in the first available chair.
     * @param initIndex Initial index value for searching for a Table with open chairs.
     * @param person The Person being seated.
     */
    // Could also catch a NullPointerException to represent all tables being full if it is not used for anything else.
    public void autoFillChair(Integer initIndex, Person person){
        Integer index = (initIndex == null)? 0 : initIndex;

        if (index < getNumberOfTables()) {
            Table currentTable = tables.get(index);

            if (!currentTable.isFull()) {
                currentTable.chairs.add(person);

            } else {
                index++;
                autoFillChair(index, person); // Recursive call.
            }
        }
        else {
            System.out.println("All tables are full! Please add chairs to a table to add anyone else.");
            //throw new ContainerFullException("All tables are full! Please add chairs to a table to add anyone else.");
        }
    }

    public void autoFillAllChairs(){
        loadParty(peopleList);
    }
    //
    private boolean partyCheck(Table table, Person initPerson){
        int count = 0;
        for (Person person : table.chairs) {
            if (table.getNumberOfFilledChairs() > 0) {
                if (initPerson.getOrganization().equalsIgnoreCase(person.getOrganization()))
                    count++;
                else
                    count = (count - 1 < 0)? 0 : --count;
            }
        }
        return (count >= table.getNumberOfFilledChairs()/2);
    }

    // Currently has no check for people with seating preferences.
    // Perhaps it's not imperative to honor seating preference. Check with Ashley.
    // Searching tables to match a seating preference will incur at worst O(n) -- not ideal.
    // Is there a way to optimize the search for seating preference?
    // What if I reorder the peopleList to (try to) ensure seating preference is honored by moving records around?
    // The loadParty() generates the tempList based upon a queue/stack/deque structure, so the above idea should still work for most cases.
    private void loadParty(List<Person> listOfAttendees){
        int index = 1; // Starts at 1. Element 0 has header junk-info.
        int checkIndex = 1; // This will be compared with the index.
        int tableIndex = this.nonSponsorIndex; // Will automatically search through sponsored Tables, so this should be the starting point.
        List<Person> tempList; // Could this be implemented as a Stack or Deque? Would that be more efficient?
        Table currentTable;

        while (index < listOfAttendees.size()){
            // It's important to initialize the tempList each time the loop is run.
            tempList = new ArrayList<>(24);
            // Load up the tempList with people from the same organization.
            // Compare the first element's organization field with each subsequent one (adds first element by default).
            if (compareOrganizations(listOfAttendees.get(index), listOfAttendees.get(checkIndex))) {
                // If they match, add the compared person object to the tempList.
                tempList.add(listOfAttendees.get(checkIndex));
                // Increment the compared person's index by one.
                checkIndex++;
            }
            // Once the list has all the people form the same organization, start placing them.
            // Then clear the list for a fresh start.
            else {
                index = checkIndex; // Start the next tempList load-up where you stopped.
                // For the Person's in the tempList, see if their organization has a sponsor Table.
                currentTable = getSponsorTable(tempList.get(0).getOrganization()); // Can be null.
                // If currentTable is null, get the next available, non-sponsor table.
                currentTable = (currentTable == null)? tables.get(tableIndex) : currentTable;
                // Check if the organization name of the Person matches the sponsor name attached to the Table.
                // Place the remaining people in non-sponsored Table's
                //if (compareSponsors(currentTable, tempList.get(0))) {
                for (Person person : tempList) {
                    // Load up the current table until it's full (usually about 8 people).
                    if (!currentTable.isFull()) {
                        currentTable.chairs.add(person); // On the first run of the loop, this should be a sponsored Table if possible.
                    }
                    // Otherwise, start at the nearest, open, regular table and place the rest of the people
                    // (by looping back to the "if" statement and following the logic).
                    else {
                        while (currentTable.isFull()) { // Should only run once, but will be able to scale if I change the placement logic.
                            tableIndex++;
                            currentTable = tables.get(tableIndex);
                        }
                        currentTable.chairs.add(person); // This is important to capture the current person in the for loop when reaching "else"
                    }
                }
            }
        }
    }

    private Table getSponsorTable(String sponsor){
        Table nextSponsor = null;
        for (int i = 0; i < numberOfSponsorTables; i++){
            nextSponsor = tables.get(i);
            if (nextSponsor.getSponsorName().equalsIgnoreCase(sponsor)){
                return nextSponsor;
            }
        }
        return nextSponsor;
    }

    private boolean compareSponsors(Table table, Person person) {
        return table.getSponsorName().equalsIgnoreCase(person.getOrganization());
    }
    private boolean compareOrganizations(Person person1, Person person2){
        return person1.getOrganization().equalsIgnoreCase(person2.getOrganization());
    }

    @Override
    public void fillChair (Table selectedTable, Person person) {
        if (!selectedTable.isFull()) {
            selectedTable.chairs.add(person);
        }
        else {
            System.out.print("Table is full. Please select another.");
        }
    }

    /**
     * This method will replace a person already seated with another.
     * Displaced people are added to the nearest open Table.
     * This may be redundant/useless, since placing people together by organization is desirable.
     * @param selectedTable The table at which you'd like to place a Person.
     * @param selectedChair The chair in which you'd like to place the Person.
     * @param person The Person you are placing.
     */
    public void replaceChair(Table selectedTable, Person selectedChair, Person person) {
        int selectedChairIndex = selectedTable.chairs.indexOf(selectedChair);
        if (selectedChair.isFull()) {
            if (!selectedTable.isFull()) {
                selectedTable.chairs.add(selectedChairIndex, person);
            }
            else {
                selectedTable.chairs.add(selectedChairIndex, person);
                // The displaced person is removed from the Table and placed at the nearest open Table.
                autoFillChair(selectedTable.chairs.remove(selectedChairIndex + 1));
            }
        }
        else {
            selectedTable.chairs.add(person);
        }
    }

    @Override
    public Table addTable(){
        Table newTable = new Table();
        tables.add(newTable);
        numberOfTables = getNumberOfTables();

        return newTable;
    }

    /**
     * Same as the addTable() method, but sets the Table's sponsor info.
     * @param sponsorName Name of the organization sponsoring the Table.
     */
    public Table addTable(String sponsorName){
        Table addedTable = addTable();
        addedTable.isSponsor = true;
        addedTable.sponsorName = sponsorName;
        return addedTable;
    }

    /**
     * Quick way to add multiple Tables at once.
     * Does not return anything, since it creates multiple objects.
     * @param numberOfTables The number of Tables that will be created, each with default parameters.
     */
    public void addTables(int numberOfTables) {
        if(numberOfTables >= 0) {
            for (int i = 0; i < numberOfTables; i++) {
                addTable();
            }
        }
    }

    @Override
    public Table removeTable(){
        Table removed = (getNumberOfTables() != 0)? tables.remove(numberOfTables - 1) : null;
        numberOfTables = getNumberOfTables();
        return removed;
    }

    /**
     * Quick way to remove multiple Tables at once.
     * Does not return anything, since it removes multiple Tables at once. Use with care.
     * @param numberOfTables The number of Tables that will be removed.
     */
    public void removeTables(int numberOfTables) {
        // While the removeTable() has a built-in check for trying to remove more Tables than exist,
        // the following check is important, because it minimizes the number of times this method will run to
        // the lesser of numberOfTables and table.size()
        int actualNumberOfTables = (numberOfTables > this.numberOfTables)? this.numberOfTables : numberOfTables;

        for(int i = 0; i < actualNumberOfTables; i++) {
            removeTable();
        }
    }

    public void setNumberOfSponsorTables(int numberOfSponsorTables){
        this.numberOfSponsorTables = numberOfSponsorTables;
    }

    //How do you access this method? How do you select and input a Table object?
    @Override
    public void makeSponsorTable(Table selectedTable, String sponsorName) {
        selectedTable.isSponsor = true;
        selectedTable.sponsorName = sponsorName;
    }

    @Override
    public int getNumberOfTables() {
        return this.tables.size();
    }

    // This method should be avoided. addTables() and removeTables() is safer.
    @Override
    public void setNumberOfTables(int numberOfTables) {
        if(numberOfTables >= this.numberOfTables) {
            int result = numberOfTables - this.numberOfTables;
            addTables(result);
        }
        else {
            // Warn the user that removing tables might displace seated people.
            // Add code later.
            int result = this.numberOfTables - numberOfTables;
            removeTables(result);
        }
    }

    // A few tables might have more than the standard 8 seats.
    @Override
    public int getMaxSeating() {
        int result = 0;

        for (Table table : this.tables) {
            result += table.maxNumberOfChairs;
        }
        return result;
    }

    @Override
    public void increaseGlobalTableSeatMax(int numberOfAddedSeats) {
        if (numberOfAddedSeats >= 0) {
            for (Table table : this.tables) {
                table.chairs.ensureCapacity(numberOfAddedSeats + table.maxNumberOfChairs);
                table.maxNumberOfChairs += numberOfAddedSeats;
            }
        }
    }

    public void increaseGlobalTableSeatMax(){
        increaseGlobalTableSeatMax(1);
    }

    @Override
    public void increaseTableSeatMax(int numberOfAddedSeats, Table table) {
        if (numberOfAddedSeats >= 0) {
            table.chairs.ensureCapacity(table.maxNumberOfChairs + numberOfAddedSeats);
            table.maxNumberOfChairs += numberOfAddedSeats;
        }
    }

    public void increaseTableSeatMax(Table table){
        increaseTableSeatMax(1, table);
    }

    @Override
    public boolean decreaseGlobalTableSeatMax(int numberOfRemovedSeats) {
        boolean result = false;
        if (numberOfRemovedSeats >= 0) {
            for (Table table : this.tables) {
                int chairsLeft = table.maxNumberOfChairs - numberOfRemovedSeats;
                chairsLeft = (chairsLeft < 0)? 0 : chairsLeft;
                if (chairsLeft > table.chairs.size()) {
                    table.maxNumberOfChairs = chairsLeft;
                }
                else {
                    for (int j = table.maxNumberOfChairs; j > chairsLeft; j--) {
                        table.chairs.remove(j - 1); //IndexOutOfBounds error if nothing is there.
                    }
                    table.maxNumberOfChairs -= numberOfRemovedSeats;
                }
            }
            result = true;
        }
        return result;
    }

    public boolean decreaseGlobalTableSeatMax(){
        return decreaseGlobalTableSeatMax(1);
    }

    @Override
    public boolean decreaseTableSeatMax(int numberOfRemovedSeats, Table table) {
        boolean result = false;
        if (numberOfRemovedSeats >= 0) {
            int chairsLeft = table.maxNumberOfChairs - numberOfRemovedSeats;
            chairsLeft = (chairsLeft < 0)? 0 : chairsLeft;
            for (int j = table.maxNumberOfChairs; j > chairsLeft; j--) {
                table.chairs.remove(j - 1);
            }
            table.maxNumberOfChairs = table.getNumberOfFilledChairs();
            result = true;
        }
        return result;
    }

    public boolean decreaseTableSeatMax(Table table) {
        return decreaseTableSeatMax(1, table);
    }
}
