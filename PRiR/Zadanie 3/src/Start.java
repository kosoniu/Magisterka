import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Start implements RemoteConverterInterface, Serializable {

    private ConverterInterface converter;
    private int counter;
    private final Map<Integer, List<Integer>> usersInput;
    private final Map<Integer, Boolean> usersReadiness;
    private final BlockingQueue<Pair<Integer, List<Integer>>> valuesQueue;


    public Start() {
        usersInput = new ConcurrentHashMap<>();
        usersReadiness = new ConcurrentHashMap<>();
        valuesQueue = new LinkedBlockingQueue();
        Consumer consumer = new Consumer();
        consumer.start();
        try {
            Registry registry = LocateRegistry.getRegistry();
            RemoteConverterInterface stub = (RemoteConverterInterface) UnicastRemoteObject.exportObject((RemoteConverterInterface) this, 0);
            registry.rebind("REMOTE_CONVERTER", stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int registerUser() throws RemoteException {
        counter++;
        usersInput.putIfAbsent(counter, new ArrayList<>());
        usersReadiness.putIfAbsent(counter, false);
        return counter;
    }

    @Override
    public void addDataToList(int userID, int value) throws RemoteException {
        usersInput.get(userID).add(value);
    }

    @Override
    public void setConverterURL(String url) throws RemoteException {
        try {
//            Registry registry = LocateRegistry.getRegistry();
            converter = (ConverterInterface) Naming.lookup(url);
        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endOfData(int userID) throws RemoteException {
//        synchronized (usersInput) {
            System.out.println("User " + userID + " input: " + usersInput.get(userID));
//        }
        valuesQueue.add(new Pair<>(userID, usersInput.get(userID)));
    }

    @Override
    public boolean resultReady(int userID) throws RemoteException {
        return usersReadiness.get(userID);
    }

    @Override
    public List<Integer> getResult(int userID) throws RemoteException {
        return usersInput.get(userID);
    }

    private class Consumer extends Thread {
        @Override
        public void run() {

            while(!isJobFinished()) {
                try {
                    Pair<Integer, List<Integer>> valuesInput = valuesQueue.take();
                    List<Integer> valuesList = valuesInput.getValue();
                    Integer userId = valuesInput.getKey();
                    List<Integer> convertedValuesList = converter.convert(valuesList);
//                    System.out.println("User " + userId + " converted list: " + convertedValuesList);
                    usersInput.replace(userId, convertedValuesList);
                    usersReadiness.replace(userId, true);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        private boolean isJobFinished() {
            boolean finished = false;

            Set<Integer> usersId = usersReadiness.keySet();

            for(Integer userId: usersId) {
                if(usersReadiness.get(userId)) {
                    finished = true;
                } else {
                    finished = false;
                    break;
                }
            }

            return finished;
        }
    }

    private class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
