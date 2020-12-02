import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class Server {

    public static void main(String[] args) throws AccessException, AlreadyBoundException, RemoteException {
        int PORT = 1099;
        Registry registry = java.rmi.registry.LocateRegistry.createRegistry(PORT);

        for ( String service : registry.list() ) {
            System.out.println( "Service : " + service );
        }

    }

}
