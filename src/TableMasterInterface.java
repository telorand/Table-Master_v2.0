/**
 * Created by Matthew on 10/2/2015.
 */
public interface TableMasterInterface<Person,Table> {
    /**
     * Places a person in a chair.
     * This method can read from a CSV file, or create someone from user input.
     * @return Returns a Person object that can be placed at a Table.
     */
    boolean fillChair(Table table, Person person);

    /**
     * Method to add a table.
     * @return Returns a Table object, so you can immediately operate upon it.
     */
    Table addTable();

    /**
     * Removes a table from the room.
     * Removes a table from the end of the list, regardless of what data the Table contains.
     * @return Returns the removed Table object, so something can be done with it before full deletion.
     */
    Table removeTable();

    /**
     * Designate a table as a sponsor table.
     * @param table The Table object being sponsored.
     * @param organizationName The name of the organization (or person) sponsoring the table.
     */
    void makeSponsorTable(Table table, String organizationName);

    /**
     * Gets the number of tables in the room.
     * @return Returns an int representation of the number of tables.
     */
    int getNumberOfTables();

    /**
     * Sets the number of tables in the room.
     * @param numberOfTables The number of tables you'd like to have in the room.
     */
    void setNumberOfTables(int numberOfTables);

    /**
     * Gets the maximum number of chairs your current setup has.
     * Multiplies the number of tables by the number of chairs at each table.
     * @return Returns an integer.
     */
    int getMaxSeating();

    /**
     * Increases the number of Chairs at all Tables by the given number.
     * @param numberOfAddedSeats Number of Chairs you'd like to add to each of the Tables.
     */
    void increaseGlobalTableSeatMax(int numberOfAddedSeats);

    /**
     * Increases the number of Chairs at a given Table by the given number.
     * This is an overload of the above method, but is used to add Chairs to a specific table.
     * @param numberOfAddedSeats Number of Chairs you'd like to add to the given Table.
     * @param table The Table object you'd like to add Chairs to.
     */
    void increaseTableSeatMax(int numberOfAddedSeats, Table table);

    /**
     * Decreases the number of Chairs at all Tables by the given number.
     * @param numberOfRemovedSeats The number of Chairs you'd like to remove from each Table.
     * @return Returns true or false, depending on if the process completed successfully.
     */
    boolean decreaseGlobalTableSeatMax(int numberOfRemovedSeats);

    /**
     * Decreases the number of Chairs at the given Table by the given number.
     * @param numberOfRemovedSeats The number of Chairs you'd like to remove from the given Table.
     * @param table The Table object from which you'd like to remove Chairs.
     * @return Returns true or false, depending on if the process completed successfully.
     */
    boolean decreaseTableSeatMax(int numberOfRemovedSeats, Table table);
}
