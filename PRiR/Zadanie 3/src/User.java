import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class User extends Thread implements Serializable {

    private RemoteConverterInterface remoteConverter;
    private int[] values;
    private int id;
    private List<Integer> result;
//    private ReentrantLock lock = new ReentrantLock();
//    private Condition resultReady = lock.newCondition();
//    private Condition resultNotReady = lock.newCondition();

    public User(RemoteConverterInterface remoteConverterInterface) {
        remoteConverter = remoteConverterInterface;
    }

    public User(RemoteConverterInterface remoteConverterInterface, List<Integer> values) {
        remoteConverter = remoteConverterInterface;
        this.values = new int[values.size()];
        for(int i = 0; i < values.size(); i++) this.values[i] = values.get(i);
    }

    public boolean isResultReady() throws RemoteException {
        return remoteConverter.resultReady(id);
    }

    public List<Integer> getResult() throws RemoteException {
        if(result != null)
            return result;

        result = remoteConverter.getResult(id);
        return result;
    }

    @Override
    public void run() {

        if(values == null) {
            values = ThreadLocalRandom.current().ints(20, 0, 10).toArray();
        }

        try {
            id = remoteConverter.registerUser();

            for (int value : values) {
                remoteConverter.addDataToList(id, value);
            }

//            System.out.println(getName() + " koncze dodawanie");
            remoteConverter.endOfData(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
