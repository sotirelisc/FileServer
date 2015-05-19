
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

    // H lista twn arxeiwn tou server
    private static ArrayList<File> files;
    
    public FileServer() throws RemoteException {
        super();
    }
    
    public static void main(String[] args) {
        try {
            // Enarksh server
            FileServer server = new FileServer();
            Naming.rebind("//localhost/FileServer", server);
            // Comment or uncomment the line below accordingly
            System.setProperty("java.rmi.server.hostname", "192.168.126.1");
            files = new ArrayList();
        } catch (RemoteException | MalformedURLException ex) {
            System.out.println("Server error: " + ex);
            System.exit(1);
        }
    }

    /**
     * Dhmiourgia neou arxeiou kai prosthikh tou sth lista arxeiwn.
     * Xrhsimopoiei to IP tou client gia tautopoihsh.
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
            files.add(new File(getClientHost(), filename));
            System.out.println("Added new file with host " + getClientHost() + " and name " + filename + ".");
        } catch (IOException ex) {
            System.out.println("Error while creating file: " + ex);
        } catch (ServerNotActiveException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Anagnwsh tou periexomenou enos arxeiou ean uparxei.
     * @param filename
     * @return To periexomeno tou arxeio ws String.
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
     * Epeksergasia enos arxeiou (den epanaprostithetai sth lista).
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
     * Elegxos gia thn uparksh enos arxeiou.
     * @param filename
     * @return False, opote mporei na dhmiourgithei neo 'h true, uparxei hdh.
     */
    @Override
    public boolean exists(String filename) {
        for (File file : files) {
            if (file.getName().equals(filename)) {
                System.out.println("File " + filename + " already exists!");
                return true;
            }
        }
        // Den vrethike, mporei na dhmiourgithei
        return false;
    }
    
    /**
     * Elegxos an enas xrhsths (IP) einai idiokthths enos sugkekrimenou arxeiou.
     * @param filename
     * @return True ara mporei na tropopoiithei, 'h false.
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
     * Epistrofh listas me ola ta onomata arxeiwn pou einai ston server.
     * @return Lista me ta filenames.
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
