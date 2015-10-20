/**
 * Created by Matthew on 10/12/2015.
 */
public class Person {
    private final String firstName;
    private final String lastName;
    private final String organization;
    private final String seatingPreference;
    private final String seatingAvoid;

    public Person(String firstName){
        this(firstName,null);
    }
    public Person(String firstName, String lastName){
        this(firstName,lastName,null);
    }
    public Person(String firstName, String lastName, String organization){
        this(firstName,lastName,organization,null,null);
    }
    public Person(String firstName, String lastName, String organization, String seatingPreference, String seatingAvoid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.seatingPreference = seatingPreference;
        this.seatingAvoid = seatingAvoid;
    }

    public String getPerson(){
        return (firstName + " " + lastName + ", " + organization + "; \nPrefers to sit with: " + seatingPreference + "; \nWould like to avoid: " + seatingAvoid);
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getOrganization(){
        return organization;
    }
    public String getPreference() {
        return seatingPreference;
    }
    public String getAvoidance() {
        return seatingAvoid;
    }
    boolean isFull(){
        return (firstName != null || lastName != null || organization != null);
    }
}
