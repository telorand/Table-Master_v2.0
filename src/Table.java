import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Matthew on 10/28/2015.
 */
public class Table implements Iterable<Group>{
    private static final int DEFAULT_TABLE_SEATING = 8;
    private ArrayList<Group> chairs; //An ArrayList that will contain Person objects. Represents the chairs (or Person's) around the table.
    private int maxNumberOfChairs; //Defined by the size of the ArrayList. Represents all chairs, open or filled.
    private String sponsorName; //The name of the sponsor.
    boolean isSponsor;
    private int tableNumber = -1;

    Table() {
        this(DEFAULT_TABLE_SEATING);
    }
    Table(int numberOfChairs){
        this.chairs = new ArrayList<>(numberOfChairs);
        this.maxNumberOfChairs = numberOfChairs;
        isSponsor = false;
        sponsorName = "Not Sponsored";
    }

    // FIXME: 12/6/2015 Fix chairsLeft algorithm. It looks to be based upon number of Groups instead of number of Persons in the group.
    int getNumberOfFilledChairs(){
        int number = 0;
        for (Group group : this){
            number += group.size();
        }
        return number;
    }
    int chairsLeft() {
        return maxNumberOfChairs - getNumberOfFilledChairs();
    }
    String getSponsorName() {
        return sponsorName;
    }
    boolean isFull(){
        int count = 0;

        for (Group group : chairs){
            count += group.size();
        }
        return count >= maxNumberOfChairs;
    }
    boolean add(Group group){
        return chairs.add(group);
    }
    void add(int position, Group group){
        chairs.add(position, group);
    }
    Group remove(int position) {
        return chairs.remove(position);
    }
    boolean isEmpty() {
        return chairs.isEmpty();
    }
    int getMaxNumberOfChairs(){
        return maxNumberOfChairs;
    }
    void setSponsorName(String sponsorName){
        this.sponsorName = sponsorName;
    }
    void ensureCapacity(int minCapacity){
        chairs.ensureCapacity(minCapacity);
    }
    void setMaxNumberOfChairs(int number){
        maxNumberOfChairs = number;
    }
    int getChairIndex(Person person){
        int found = -1;
        for (Group group : chairs){
            if (group.getGroup().contains(person)){
                found = group.getGroup().indexOf(person);
            }
        }
        return found;
    }
    int getGroupIndex(Group group){
        return chairs.indexOf(group);
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    @Override
    public String toString(){
        return sponsorName;
    }

    @Override
    public Iterator<Group> iterator() {
        return chairs.iterator();
    }
}
