/**
 * Created by Matthew on 10/12/2015.
 */
class Person {
    private final String firstName;
    private final String lastName;
    private final String organization;

    public Person(String firstName){
        this(firstName,null);
    }
    public Person(String firstName, String lastName){
        this(firstName,lastName,null);
    }
    public Person(String firstName, String lastName, String organization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
    }

    public String getPerson(){
        return (firstName + " " + lastName + ", " + organization);
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
    boolean isFull(){
        return (firstName != null || lastName != null || organization != null);
    }

    @Override
    public String toString(){
        return getPerson();
    }
}
