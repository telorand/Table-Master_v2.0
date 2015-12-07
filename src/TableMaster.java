import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

/**
 * This is a program that allows the user to specify a CSV file that contains names and organizations,
 * and the program will automatically add each person to a round table that seats 8.
 * Created originally for the Carbondale Chamber of Commerce to assist with event seating.
 * Created by Matthew on 10/5/2015.
 */
public class TableMaster implements TableMasterInterface<Table,Group>, Iterable<Table> {
    private static final int DEFAULT_TABLE_SEATING = 8;
    private static final int TABLE_BUFFER = 5;
    private File csv;
    private ArrayList<Table> tables;
    private int numberOfTables;
    private List<Person> peopleList;
    private int numberOfSponsorTables;
    private int nonSponsorStartingIndex; // Table indices start at 0, so this is correct.

    // We have to read the CSV file after TableMaster instantiation. Otherwise we will get an error.
    TableMaster() {
       /* setNumberOfSponsorTables(numberOfInitialTables);
        tables = new ArrayList<>(numberOfInitialTables);
        addTables(this.numberOfSponsorTables);
        nonSponsorStartingIndex = numberOfInitialTables;*/
        tables = new ArrayList<>();
        nonSponsorStartingIndex = 0;
        numberOfSponsorTables = 0;
        csv = null;
    }
    TableMaster(int numberOfSponsorTables, ArrayDeque<String> sponsorNames){
        setNumberOfSponsorTables(numberOfSponsorTables);
        tables = new ArrayList<>(numberOfSponsorTables);
        addSponsorTables(this.numberOfSponsorTables,sponsorNames);
        nonSponsorStartingIndex = numberOfSponsorTables;
        csv = null;
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

    public void autoFillAllChairs(){
        if (csv != null)
            loadParty(peopleList);
        else
            System.out.println("You need to first read in a valid CSV file!");
    }

    // Is there a way to optimize the search for seating preference?
    // What if I reorder the peopleList to (try to) ensure seating preference is honored by moving records around?
    // The loadParty() generates the tempList based upon a queue/deque structure, so the above idea should still work for most cases.

    private void loadParty(List<Person> listOfAttendees) {
        // Keeps starting place when loading up a new tempList.
        int index = 0; // Starts at 1. Element 0 has header junk-info.
        // A comparator index for comparing with the element at the index.
        int checkIndex = 0; // This will be compared with the index.
        int furthestNonFullIndex = this.nonSponsorStartingIndex; // Will automatically search through sponsored Tables, so this should be the starting point.
        int nearestNonFullIndex = furthestNonFullIndex;
        int tableSubsetIndex = nearestNonFullIndex;
        ArrayDeque<Person> tempList = new ArrayDeque<>(24);
        Table currentTable;
        Group subGroup = null;

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
                    if (nearestNonFullIndex == tables.size()) {
                        furthestNonFullIndex++;
                        addTablePlus(tempList);
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
                while (!tempList.isEmpty() || subGroup != null) {
                    // On the first run, or when the group has been placed, this will return false.
                    if (subGroup == null) {
                        // This will also modify the tempList because of pass-by-reference.
                        // This is what we want.
                        subGroup = generateSubGroup(tempList,currentTable);
                    }
                    // When a table is full (or has too few chairs), this will return false.
                    if (fillChairs(currentTable,subGroup)) {
                        // Set subGroup = null if it's been placed at a table, so the above logic will run
                        // to create a new sub-group.
                        subGroup = null;
                    }
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
                            if (tableSubsetIndex == furthestNonFullIndex){
                                addTablePlus(subGroup);
                            }
                            currentTable = getTableAt(tableSubsetIndex);

                        }
                        // If we end up here, all the tables in the subset are
                        // unable to seat anyone currently in the tempList (but not necessarily full).
                        else {
                            furthestNonFullIndex++;
                            // If furthestNonFullIndex points to a space where no table yet exists,
                            // (and it probably does at this point), one will be created.
                            if (furthestNonFullIndex == tables.size()) {
                                addTablePlus(subGroup);
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
        for (int i = 0; i < numberOfSponsorTables; i++){
            if (tables.get(i).getSponsorName().equalsIgnoreCase(sponsor)){
                return tables.get(i);
            }
        }
        return null;
    }

    private boolean compareOrganizations(Person person1, Person person2){
        return person1.getOrganization().equalsIgnoreCase(person2.getOrganization());
    }
    private void addTablePlus(Collection<Person> list){
        Table table = addTable();
        if (list.size() == 9)
            increaseTableSeatMax(table);
    }
    private void addTablePlus(Group group){
        Table table = addTable();
        if (group.size() == 9)
            increaseTableSeatMax(table);
    }
    private Group generateSubGroup(ArrayDeque<Person> tempList, Table currentTable){
        // Remember: 8 % 8 == 0 % 8
        // The following makes sure comparator returns a value between 1-8, or sometimes 1-9
        // This is okay, since when the tempList is empty (eg. 0 % 8), the loop will exit
        // before reaching this statement.
        Group subGroup;
        int subGroupSize = (tempList.size() % currentTable.getMaxNumberOfChairs() == 0) ?
                currentTable.getMaxNumberOfChairs() : tempList.size() % currentTable.getMaxNumberOfChairs();
        if (tempList.size() >= currentTable.getMaxNumberOfChairs()) {
            // Since it's best to group people together, this will create the largest sub-group possible
            // from the tempList of people (usually 8, but occasionally 9).
            subGroup = new Group(currentTable.getMaxNumberOfChairs(), tempList.getFirst().getOrganization());
        } else {
            // This part will run if there's less than 8 (or 9) people in the tempList.
            subGroup = new Group(subGroupSize, tempList.getFirst().getOrganization());
        }
        // Fill the sub-group with the people on the tempList.
        // Remember, Group.size() returns the full size of the Group (whether there's objects in it or not)
        for (int i = 0; i < subGroup.size(); i++) {
            subGroup.add(tempList.removeFirst());
        }
        return subGroup;
    }

    private boolean fillChairs(Table selectedTable, Group group){
        // When a table is full, this will return false.
        if (selectedTable.chairsLeft() >= group.size()) {
            // If the current table is empty, and the number of people left to place is greater
            // than or equal to the number of chairs left at the table...
            group.setHomeTable(selectedTable);
            return selectedTable.add(group);
        }
        else
            return false;
        // Otherwise, select the nearest, open, regular table and
        // loop back to the placement logic.
    }

    // This could also move people to a table with open chairs.
    // This could be a move like a group of 4 to a table with a group of 2 and 2 empty chairs,
    // or two groups of 2.
    // However, if the initial table doesn't have enough space for the displaced people,
    // it should return an error.
    @Override
    public void swapChairs(Group selectedGroup,Group displacedGroup){
        // If the destination table already has space for the group you want to move, just stick 'em there.
        if (displacedGroup.getHomeTable().chairsLeft() >= selectedGroup.size()){
            fillChairs(displacedGroup.getHomeTable(), selectedGroup);
        }
        else {
            switch (selectedGroup.compareTo(displacedGroup)) {
                // If the selectedGroup is smaller than the displacedGroup...
                case -1:
                    if (selectedGroup.getHomeTable().chairsLeft() + selectedGroup.size() < displacedGroup.size()){
                        // This is an error, since the displacedGroup can't fit at the source table.
                        System.out.println("There is no room at the source table. Please try again.");
                    }
                    else{
                        performSwap(selectedGroup,displacedGroup);
                    }
                    break;

                // If the selectedGroup is the same size as the displacedGroup...
                case 0:
                    performSwap(selectedGroup,displacedGroup);
                    break;

                // If the selectedGroup is larger than the displacedGroup...
                case 1:
                    if (displacedGroup.getHomeTable().chairsLeft() + displacedGroup.size() < selectedGroup.size()){
                        // This is an error, since the source group can't fit at the destination table
                        // by displacing the selected group.
                        // Is there a way to select multiple groups at the destination table?
                        // They'd probably have to load up into a temporary array.
                        System.out.println("There is no room at the destination table." +
                                "\nTry selecting a smaller source group," +
                                "\n a larger group at the destination table," +
                                "\n or a different group at a different destination table.");
                    }
                    else {
                        performSwap(selectedGroup,displacedGroup);
                    }
                    break;
            }
        }
    }

    private void performSwap(Group selectedGroup, Group displacedGroup){
        int destIndex = displacedGroup.getHomeTable().getGroupIndex(displacedGroup);
        Table sourceTable = selectedGroup.getHomeTable();

        displacedGroup.getHomeTable().add(destIndex, selectedGroup);
        selectedGroup.getHomeTable().add(displacedGroup.getHomeTable().remove(destIndex + 1));
        selectedGroup.setHomeTable(displacedGroup.getHomeTable());
        displacedGroup.setHomeTable(sourceTable);
    }

    @Override
    public Table addTable(){
        Table newTable = new Table();
        tables.add(newTable);
        numberOfTables = getNumberOfTables();
        newTable.setTableNumber(numberOfTables);

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

    public void addSponsorTables(int numberOfSponsorTables, ArrayDeque<String> listOfSponsors){
        if(numberOfSponsorTables >= 0 && listOfSponsors.size() == numberOfSponsorTables){
            for (int i = 0; i < numberOfSponsorTables; i++){
                addTable(listOfSponsors.pop());
            }
        }
    }

    @Override
    public Table removeTable(){
        return removeTableAt(numberOfTables - 1);
    }

    public Table removeTable(Table selectedTable){
        return removeTableAt(tables.indexOf(selectedTable));
    }

    public Table removeTableAt(int index){
        index = (index > numberOfTables)? numberOfTables : index;
        Table removed = (getNumberOfTables() != 0)? tables.remove(index) : null;
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
