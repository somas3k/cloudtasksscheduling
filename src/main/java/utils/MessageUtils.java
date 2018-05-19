package utils;

import java.io.*;

public class MessageUtils {

    public static Object deserializeMessage (byte[] body) throws ClassNotFoundException {
        try {
            ObjectInput oin = new ObjectInputStream(new ByteArrayInputStream(body));
            return oin.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] serializeMessage(Object o){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(o);
            return bos.toByteArray();
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
