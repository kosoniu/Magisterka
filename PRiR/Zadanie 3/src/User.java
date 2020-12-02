import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class User extends Thread implements Serializable {

    private RemoteConverterInterface remoteConverter;
    private int[] values;
    private int id;
    private List<Integer> result;

    public User(RemoteConverterInterface remoteConverterInterface) {
        remoteConverter = remoteConverterInterface;
    }

    public User(RemoteConverterInterface remoteConverterInterface, List<Integer> values) {
        remoteConverter = remoteConverterInterface;
        this.values = new int[values.size()];
        for(int i = 0; i < values.size(); i++) this.values[i] = values.get(i);
    }

    public List<Integer> getResult() {
        return result;
    }

    @Override
    public void run() {

        if(values == null) {
            values = ThreadLocalRandom.current().ints(20, 0, 10).toArray();
        }

        try {
            id = remoteConverter.registerUser();

            for(int i = 0; i < values.length; i++) {
                remoteConverter.addDataToList(id, values[i]);
            }

            System.out.println(getName() + " koncze dodawanie");
            remoteConverter.endOfData(id);

            while(!remoteConverter.resultReady(id)) {
                Thread.sleep(100);
            }

            result = remoteConverter.getResult(id);
            System.out.println(getName() + " converted list: " + result);
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
