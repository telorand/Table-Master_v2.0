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

    Table() {
        this(DEFAULT_TABLE_SEATING);
    }
    Table(int numberOfChairs){
        this.chairs = new ArrayList<>(numberOfChairs);
        this.maxNumberOfChairs = numberOfChairs;
        isSponsor = false;
        sponsorName = "Not Sponsored";
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
        int count = 0;

        for (Group group : chairs){
            count += group.size();
        }
        return count >= maxNumberOfChairs;
    }
    protected boolean add(Group group){
        return chairs.add(group);
    }
    protected void add(int position, Group group){
        chairs.add(position, group);
    }
    protected Group remove(int position) {
        return chairs.remove(position);
    }
    protected boolean isEmpty() {
        return chairs.isEmpty();
    }
    protected int getMaxNumberOfChairs(){
        return maxNumberOfChairs;
    }
    protected void setSponsorName(String sponsorName){
        this.sponsorName = sponsorName;
    }
    protected void ensureCapacity(int minCapacity){
        chairs.ensureCapacity(minCapacity);
    }
    protected void setMaxNumberOfChairs(int number){
        maxNumberOfChairs = number;
    }
    protected int getChairIndex(Person person){
        int found = -1;
        for (Group group : chairs){
            if (group.getGroup().contains(person)){
                found = group.getGroup().indexOf(person);
            }
        }
        return found;
    }
    protected int getGroupIndex(Group group){
        return chairs.indexOf(group);
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
