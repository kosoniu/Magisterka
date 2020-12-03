import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class RemoteConverterTest {

    @Before
    public void before() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("CONVERTER", new Converter());
        RemoteConverterInterface remoteConverterInterface = new Start();
        remoteConverterInterface.setConverterURL("//localhost/CONVERTER");
    }


    @Test
    public void testConnection() throws RemoteException, NotBoundException, InterruptedException, MalformedURLException {
        Registry registry = LocateRegistry.getRegistry();
        RemoteConverterInterface remoteConverter = (RemoteConverterInterface) registry.lookup("REMOTE_CONVERTER");

        Integer userNumber = 10;

//        String[] services = registry.list();
//
//        for(String service: services) {
//            System.out.println(service);
//        }

        Thread.sleep(2000);
        ExecutorService executorService = Executors.newCachedThreadPool();

        for(int i = 1; i <= userNumber; i++) {
            User user = new User(remoteConverter);
            user.setName("User " + i);
            executorService.execute(user);
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void testCustomArrays() throws RemoteException, NotBoundException, InterruptedException {
        Registry registry = LocateRegistry.getRegistry();
        RemoteConverterInterface remoteConverter = (RemoteConverterInterface) registry.lookup("REMOTE_CONVERTER");
        Thread.sleep(2000);

        ExecutorService executorService = Executors.newCachedThreadPool();

        ConverterInterface converter = new Converter();

        List<Integer> user1Values = Arrays.asList(0,3,6,5,2,4,7);
        List<Integer> user2Values = Arrays.asList(9,5,8,7,4,1,3,7,4,1,2,3,6);
        List<Integer> user3Values = Arrays.asList(1,8,9,5,4,7,1,2,3,5,6,4);
        List<Integer> user4Values = Arrays.asList(8,9,6,7,4,2,8,6,5,4,1,2,3,6,8,1,4);
        List<Integer> user5Values = Arrays.asList(8,4,7,1,2,5,6,2,5,5,7,5);

        User user1 = new User(remoteConverter, user1Values);
        User user2 = new User(remoteConverter, user2Values);
        User user3 = new User(remoteConverter, user3Values);
        User user4 = new User(remoteConverter, user4Values);
        User user5 = new User(remoteConverter, user5Values);

        user1.setName("User 1");
        user2.setName("User 2");
        user3.setName("User 3");
        user4.setName("User 4");
        user5.setName("User 5");
        executorService.execute(user1);
        executorService.execute(user2);
        executorService.execute(user3);
        executorService.execute(user4);
        executorService.execute(user5);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(user1.getResult(), converter.convert(user1Values));
        assertEquals(user2.getResult(), converter.convert(user2Values));
        assertEquals(user3.getResult(), converter.convert(user3Values));
        assertEquals(user4.getResult(), converter.convert(user4Values));
        assertEquals(user5.getResult(), converter.convert(user5Values));
    }

    @Test
    public void testBigArrays() throws RemoteException, NotBoundException, InterruptedException {
        Registry registry = LocateRegistry.getRegistry();
        RemoteConverterInterface remoteConverter = (RemoteConverterInterface) registry.lookup("REMOTE_CONVERTER");
        Thread.sleep(2000);

        ExecutorService executorService = Executors.newCachedThreadPool();

        ConverterInterface converter = new Converter();

        Random random = ThreadLocalRandom.current();

        Integer userNumber = 20;
        List<List<Integer>> values = new ArrayList<>();
        List<User> users = new ArrayList<>();

        for(int i = 1; i <= userNumber; i++) {
            List<Integer> userValues = new ArrayList<>();
            for( int j = 0; j < 20; j++) {
                userValues.add(random.nextInt(20));
            }
            values.add(userValues);

            User user = new User(remoteConverter, userValues);
            user.setName("User " + i);
            users.add(user);
        }
        users.forEach(executorService::execute);

        executorService.shutdown();

        if(executorService.awaitTermination(20, TimeUnit.SECONDS)) {
            for(int i = 0; i < userNumber; i++) {
                if(!users.get(i).isResultReady()) {
                    assertNull(users.get(i).getResult());
                }

                while(!users.get(i).isResultReady()) {
                    Thread.sleep(100);
                }

                assertEquals("Dla Usera " + i + " wynik okazal sie bledny", converter.convert(values.get(i)), users.get(i).getResult());
            }
        }
    }

}
