import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is a program that allows the user to specify a CSV file that contains names and organizations,
 * and the program will automatically add each person to a round table that seats 8.
 * Created originally for the Carbondale Chamber of Commerce to assist with event seating.
 * Created by Matthew on 10/5/2015.
 */
public class TableMaster implements TableMasterInterface<Person,Table>, Iterable<Table> {
    private static final int DEFAULT_TABLE_SEATING = 8;
    private static final int TABLE_BUFFER = 5;
    private File csv = null;
    private ArrayList<Table> tables;
    private int numberOfTables;
    private List<Person> peopleList;
    private int numberOfSponsorTables;
    private int nonSponsorStartingIndex = numberOfSponsorTables; // Table indices start at 0, so this is correct.

    // We have to read the CSV file after TableMaster instantiation. Otherwise we will get an error.
    TableMaster(int numberOfSponsorTables) {
        setNumberOfSponsorTables(numberOfSponsorTables);
        tables = new ArrayList<>(numberOfSponsorTables);
        addTables(this.numberOfSponsorTables);
        // How/When do you add sponsor names to Tables?
    }

    public void readCSVFile(File csvFile){
        if (FilenameUtils.getExtension(csvFile.getPath()).equalsIgnoreCase("csv")) {
            csv = csvFile;
            peopleList = CSVFileReader.readCsvFile(csvFile);
            // Number of tables should be the same as number of sponsor tables initially.
            numberOfTables = numberOfSponsorTables; // Round up
            this.tables.ensureCapacity((int)(peopleList.size() / DEFAULT_TABLE_SEATING + .5 + TABLE_BUFFER)); // Ensure the list is able to hold the maximum number of tables.
        }
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
                currentTable.add(person);

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
        if (csv != null)
            loadParty(peopleList);
        else
            System.out.println("You need to first read in a valid CSV file!");
    }

    // Currently has no check for people with seating preferences.
    // Perhaps it's not imperative to honor seating preference. Check with Ashley.
    // Searching tables to match a seating preference will incur at worst O(n) -- not ideal.
    // Is there a way to optimize the search for seating preference?
    // What if I reorder the peopleList to (try to) ensure seating preference is honored by moving records around?
    // The loadParty() generates the tempList based upon a queue/deque structure, so the above idea should still work for most cases.
    private void loadParty(List<Person> listOfAttendees) {
        // Keeps starting place when loading up a new tempList.
        int index = 1; // Starts at 1. Element 0 has header junk-info.
        // A comparator index for comparing with the element at the index.
        int checkIndex = 1; // This will be compared with the index.
        int furthestNonFullIndex = this.nonSponsorStartingIndex; // Will automatically search through sponsored Tables, so this should be the starting point.
        int nearestNonFullIndex = furthestNonFullIndex;
        int tableSubsetIndex = nearestNonFullIndex;
        ArrayDeque<Person> tempList = new ArrayDeque<>(24);
        Table currentTable;

        while (index < listOfAttendees.size()) {
            // This first if-block will load up the tempList with people from the same organization.
            // Compare the first element's organization field with each subsequent one (adds first element by default).
            if ((checkIndex != listOfAttendees.size()) && compareOrganizations(listOfAttendees.get(index), listOfAttendees.get(checkIndex))) {
                // If they match, add the compared person object to the tempList.
                tempList.add(listOfAttendees.get(checkIndex));
                // Increment the compared person's index by one.
                checkIndex++;
                // checkIndex != listOfAttendees.size() will check if the end of the list has been reached.
            }

            // Once the list has all the people from the same organization, start placing them.
            // tempList will be cleared as you go, which is why we don't call clear() or something similar.
            else {
                // If checkIndex == tempList.size(), this will cause the initial if-loop to terminate
                // after reaching the end of this block.
                index = checkIndex; // Start the next tempList load-up where you stopped.
                // For the Person's in the tempList, see if their organization has a sponsor Table.
                currentTable = getSponsorTable(tempList.getFirst().getOrganization()); // Can be null.
                // If currentTable is null (not affiliated with a sponsor), get the next available, non-sponsor table.
                if (currentTable == null) {
                    // We need to add a table if the current index is pointing at a blank space.
                    if (furthestNonFullIndex == tables.size()) {
                        Table table = addTable();
                        // This will produce the same result as the below if-statement,
                        // but it is much easier and quicker to perform at this step.
                        if (tempList.size() == 9)
                            increaseTableSeatMax(table);
                    }
                    // We need to start at nearestNonFullIndex so that we cycle back over tables
                    // that may still have room for people.
                    currentTable = getTableAt(nearestNonFullIndex);
                }
                // While this is similar to the above, it will cover both sponsor and non-sponsor tables.
                else if (currentTable.getMaxNumberOfChairs() == 8 // If currentTable has 8 chairs...
                        && (currentTable.chairsLeft() + 1 >= tempList.size() ^ currentTable.chairsLeft() >= tempList.size())) { // ...and increasing the number of chairs to 9 would allow the group to sit there...
                    increaseTableSeatMax(currentTable); // ...increase the number of chairs by 1
                }

                // While there's people still in the tempList...
                while (!tempList.isEmpty()) {
                    // Load up the current table until it's full (usually about 8 people).
                    // Remember: 8 % 8 == 0 % 8
                    // The following makes sure comparator returns a value between 1-8, or sometimes 1-9
                    // This is okay, since when the tempList is empty (eg. 0 % 8), the loop will exit
                    // before reaching this statement.
                    int comparator = (tempList.size() % currentTable.getMaxNumberOfChairs() == 0)?
                            currentTable.getMaxNumberOfChairs() : tempList.size() % currentTable.getMaxNumberOfChairs();
                    // When a table is full, this will return false.
                    if (currentTable.chairsLeft() >= comparator) {
                        // If the current table is empty, and the number of people left to place is greater
                        // than or equal to the number of chairs left at the table...
                        if (currentTable.isEmpty() && tempList.size() >= currentTable.getMaxNumberOfChairs()) {
                            while(!currentTable.isFull()) {
                                //currentTable.add(tempList.removeFirst());
                                fillChair(currentTable, tempList.removeFirst());
                            }
                        }
                        else {
                            //currentTable.add(tempList.removeFirst());
                            fillChair(currentTable, tempList.removeFirst());
                        }
                    }
                        // Otherwise, select the nearest, open, regular table and
                        // loop back to the placement logic.
                    else {
                        boolean movedNearestIndex = false;
                        // Loop through the tables until one that has space is found.
                        while (getTableAt(nearestNonFullIndex).isFull() && nearestNonFullIndex < furthestNonFullIndex) {
                            nearestNonFullIndex++;
                            tableSubsetIndex = nearestNonFullIndex;
                            movedNearestIndex = true;
                        }
                        // Select the currently indexed table in the subset.
                        // This is important to capture the initial table when the
                        // above while loop runs.
                        if (movedNearestIndex) {
                            currentTable = getTableAt(nearestNonFullIndex);
                        }
                        // This will attempt to select the next table in the subset.
                        // This could be a table starting at nearestNonFullIndex and up to
                        // the table at furthestNonFullIndex.
                        else if (tableSubsetIndex < furthestNonFullIndex) {
                            tableSubsetIndex++; // This could be equal to furthestNonFullIndex at most
                            currentTable = getTableAt(tableSubsetIndex);
                        }
                        // If we end up here, all the tables in the subset are
                        // unable to seat anyone currently in the tempList (but not necessarily full).
                        else {
                            furthestNonFullIndex++;
                            // If furthestNonFullIndex points to a space where no table yet exists,
                            // (and it probably does at this point), one will be created.
                            if (furthestNonFullIndex == tables.size()) {
                                Table table = addTable();
                                if (tempList.size() == 9) {
                                    increaseTableSeatMax(table);
                                }
                            }
                            currentTable = getTableAt(furthestNonFullIndex);
                        }
                    }
                }
            }
        }
    }

    private Table getTableAt(int index){
        return tables.get(index);
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
    public boolean fillChair (Table selectedTable, Person person) {
        if (!selectedTable.isFull()) {
            selectedTable.add(person);
            return true;
        }
        else {
            return false;
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

    // This needs to be able to select an entire organization at a table to swap with another.
    public void swapChairs(Table selectedTable, Person selectedChair, Person person) {
        int selectedChairIndex = selectedTable.getChairIndex(selectedChair);
        if (selectedChair.isFull()) {
            if (!selectedTable.isFull()) {
                selectedTable.add(selectedChairIndex, person);
            }
            else {
                selectedTable.add(selectedChairIndex, person);
                // The displaced person is removed from the Table and placed at the nearest open Table.
                autoFillChair(selectedTable.remove(selectedChairIndex + 1));
            }
        }
        else {
            selectedTable.add(person);
        }
    }

    @Override
    public void swapChairs(Table selectedTable, Person selectedGroup, Table destinationTable){

    }

    @Override
    public Table addTable(){
        Table newTable = new Table();
        tables.add(newTable);
        numberOfTables = getNumberOfTables();

        return newTable;
    }

    public Table addTable(int numberOfChairs){
        Table newTable = addTable();
        if (numberOfChairs >= 9){
            increaseTableSeatMax(newTable);
        }
        return newTable;
    }

    /**
     * Same as the addTable() method, but sets the Table's sponsor info.
     * @param sponsorName Name of the organization sponsoring the Table.
     */
    public Table addTable(String sponsorName){
        Table addedTable = addTable();
        makeSponsorTable(addedTable, sponsorName);
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
        selectedTable.setSponsorName(sponsorName);
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
            result += table.getMaxNumberOfChairs();
        }
        return result;
    }

    @Override
    public void increaseGlobalTableSeatMax(int numberOfAddedSeats) {
        for (Table table : this.tables) {
            increaseTableSeatMax(numberOfAddedSeats, table);
        }
    }

    public void increaseGlobalTableSeatMax(){
        increaseGlobalTableSeatMax(1);
    }

    @Override
    public void increaseTableSeatMax(int numberOfAddedSeats, Table table) {
        if (numberOfAddedSeats >= 0) {
            table.ensureCapacity(table.getMaxNumberOfChairs() + numberOfAddedSeats);
            table.setMaxNumberOfChairs(table.getMaxNumberOfChairs() + numberOfAddedSeats);
        }
    }

    public void increaseTableSeatMax(Table table){
        increaseTableSeatMax(1, table);
    }

    @Override
    public boolean decreaseGlobalTableSeatMax(int numberOfRemovedSeats) {
        boolean result = false;
        for (Table table : this.tables) {
            result = decreaseTableSeatMax(numberOfRemovedSeats, table);
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
            int chairsLeft = table.getMaxNumberOfChairs() - numberOfRemovedSeats;
            chairsLeft = (chairsLeft < 0)? 0 : chairsLeft;
            if (chairsLeft > table.getNumberOfFilledChairs()) {
                table.setMaxNumberOfChairs(chairsLeft);
            }
            else {
                for (int j = table.getMaxNumberOfChairs(); j > chairsLeft; j--) {
                    table.remove(j - 1); //IndexOutOfBounds error if nothing is there.
                }
                table.setMaxNumberOfChairs(table.getMaxNumberOfChairs() - numberOfRemovedSeats);
            }
            result = true;
        }
        return result;
    }

    public boolean decreaseTableSeatMax(Table table) {
        return decreaseTableSeatMax(1, table);
    }

    @Override
    public Iterator<Table> iterator() {
        return tables.iterator();
    }
}
