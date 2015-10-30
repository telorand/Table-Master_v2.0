import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Matthew on 10/29/2015.
 */
public class Group implements Iterable<Person>, Comparable<Group>{
    private static final int DEFAULT_SIZE = 9;
    private ArrayList<Person> group;
    private final int groupSize;
    private String organizationName;
    private Table homeTable;

    Group(){
        this(DEFAULT_SIZE, null);
    }
    Group(int maxGroupSize, String organizationName){
        group = new ArrayList<>(maxGroupSize);
        groupSize = maxGroupSize;
        this.organizationName = organizationName;
    }
    Group(Collection<Person> listOfElements){
        group = new ArrayList<>(listOfElements.size());
        groupSize = listOfElements.size();
        group.addAll(listOfElements);
        organizationName = group.get(0).getOrganization();
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public int size() {
        return groupSize;
    }

    public Table getHomeTable() {
        return homeTable;
    }

    public void setHomeTable(Table homeTable) {
        this.homeTable = homeTable;
    }

    public ArrayList<Person> getGroup() {
        return group;
    }
    public void add(Person person){
        group.add(person);
    }

    @Override
    public Iterator<Person> iterator() {
        return group.iterator();
    }

    @Override
    public int compareTo(Group group) {
        if (this.size() > group.size()){
            return 1;
        }
        else if (this.size() < group.size()){
            return -1;
        }
        else {
            return 0;
        }
    }
}
