import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ConverterInterface extends Remote {
    /**
     * Metoda konwertuje ciÄg liczb dostarczonych jako "input".
     *
     * @param input ciÄg liczb do skonwertowania
     * @return ciÄg po wykonaniu konwersji
     * @throws RemoteException wyjÄtek wymagany przez RMI
     */
    public List<Integer> convert(List<Integer> input) throws RemoteException;
}