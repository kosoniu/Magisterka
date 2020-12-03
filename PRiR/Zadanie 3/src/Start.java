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
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Start implements RemoteConverterInterface, Serializable {

    private ConverterInterface converter;
    private AtomicInteger counter;
    private final Map<Integer, List<Integer>> usersInput;
    private final Map<Integer, List<Integer>> usersResult;
    private final BlockingQueue<Pair<Integer, List<Integer>>> valuesQueue;

    public Start() {
        usersInput = new ConcurrentSkipListMap<>();
        usersResult = new ConcurrentSkipListMap<>();
        valuesQueue = new LinkedBlockingQueue();
        counter = new AtomicInteger();
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
        int counter = this.counter.incrementAndGet();
        usersInput.putIfAbsent(counter, new ArrayList<>());
        usersResult.putIfAbsent(counter, new ArrayList<>());
        return counter;
    }

    @Override
    public void addDataToList(int userID, int value) throws RemoteException {
        usersInput.get(userID).add(value);
    }

    @Override
    public void setConverterURL(String url) throws RemoteException {
        try {
            converter = (ConverterInterface) Naming.lookup(url);
        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endOfData(int userID) throws RemoteException {
        try {
            valuesQueue.put(new Pair<>(userID, usersInput.get(userID)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean resultReady(int userID) throws RemoteException {
        int size = usersResult.get(userID).size();
        return size > 0;
    }

    @Override
    public List<Integer> getResult(int userID) throws RemoteException {
        List<Integer> result = usersResult.get(userID);
        return result.size() > 0 ? result : null;
    }

    private class Consumer extends Thread {
        @Override
        public void run() {
            while(true) {
                try {
                    Pair<Integer, List<Integer>> valuesInput = valuesQueue.take();
                    List<Integer> valuesList = valuesInput.getValue();
                    Integer userId = valuesInput.getKey();
                    System.out.println("User " + userId + " list: " + valuesList);
                    List<Integer> convertedValuesList = converter.convert(valuesList);
                    System.out.println("User " + userId + " converted list: " + convertedValuesList);
                    usersResult.replace(userId, convertedValuesList);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
