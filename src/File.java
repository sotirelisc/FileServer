
/**
 *
 * @author ChristosPC
 */
public class File {
    private String owner;
    private String name;
    private boolean available;
    
    public File(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }
    
    public String getOwner() { return this.owner; }
    public String getName() { return this.name; }
    public boolean isAvailable() { return this.available; }
}
