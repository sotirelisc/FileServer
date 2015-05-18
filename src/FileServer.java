
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Christos Sotirelis
 */
public class FileServer extends UnicastRemoteObject implements FileInterface {

    private static ArrayList<File> files;
    
    public FileServer() throws RemoteException {
        super();
    }
    
    public static void main(String[] args) {
        try {
            // Starting server
            FileServer server = new FileServer();
            Naming.rebind("//localhost/FileServer", server);
            // Change accordingly
            System.setProperty("java.rmi.server.hostname", "192.168.126.1");
            files = new ArrayList();
        } catch (RemoteException | MalformedURLException ex) {
            System.out.println("Server error: " + ex);
            System.exit(1);
        }
    }

    /**
     * Create a new text file and add it to existing file list.
     * Using client's IP as identification.
     * @param filename
     * @param content
     * @throws java.rmi.RemoteException
     */
    @Override
    public synchronized void createFile(String filename, String content) throws RemoteException {
        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            out.println(content);
            out.close();
            // Add to file list
            files.add(new File(getClientHost(), filename));
            System.out.println("Added new file with host " + getClientHost() + " and name " + filename + ".");
        } catch (IOException ex) {
            System.out.println("Error while creating file: " + ex);
        } catch (ServerNotActiveException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * View content of a text file if it exists.
     * @param filename
     * @return Content of file as String.
     * @throws java.rmi.RemoteException
     */
    @Override
    public String viewFile(String filename) throws RemoteException {
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(filename));
            String line, input = "";
            while ((line = in.readLine()) != null) {
                input = input.concat(line + "\n");
            }
            in.close();
            return input;
        } catch (FileNotFoundException ex) {
            System.out.println("File not found!");
        } catch (IOException ex) {
            System.out.println("Error while reading file: " + ex);
        }
        return null;
    }

    /**
     * Edit a text file (only the file owner).
     * @param filename
     * @param content
     * @throws java.rmi.RemoteException
     */
    @Override
    public void editFile(String filename, String content) throws RemoteException {
        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            out.println(content);
            out.close();
        } catch (IOException ex) {
            System.out.println("Error while writing in file: " + ex);
        }
    }

    /**
     * Check if a file exists.
     * @param filename
     * @return True therefore cannot create a new, or false.
     */
    @Override
    public boolean exists(String filename) {
        for (File file : files) {
            if (file.getName().equals(filename)) {
                System.out.println("File " + filename + " already exists!");
                return true;
            }
        }
        // File not found, can be created
        return false;
    }
    
    /**
     * Check if a user (IP) is the owner of a certain file.
     * @param filename
     * @return True therefore editable, or false.
     */
    @Override
    public boolean canEdit(String filename) {
        for (File file : files) {
            if (file.getName().equals(filename)) {
                try {
                    if (file.getOwner().equals(getClientHost())) {
                        return true;
                    }
                } catch (ServerNotActiveException ex) {
                    Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    /**
     * Get a list with all the filenames saved.
     * @return List with filenames.
     * @throws RemoteException 
     */
    @Override
    public ArrayList<String> getList() throws RemoteException {
        ArrayList<String> filenames = new ArrayList();
        for (File file : files) {
            filenames.add(file.getName());
        }
        return filenames;
    }

}
