import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Converter implements ConverterInterface, Serializable {
    @Override
    public List<Integer> convert(List<Integer> input) throws RemoteException {
        List<Integer> result = new ArrayList<>();
        input.forEach(value -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result.add(value*value);
        });
        return result;
    }
}
