package pl.edu.agh.io.cloudscheduling.utils;

import org.apache.commons.lang3.SerializationUtils;

import java.io.*;

public class MessageUtils {

    public static Serializable deserializeMessage (byte[] body) throws ClassNotFoundException {
        return SerializationUtils.deserialize(body);
//        try {
//            ObjectInput oin = new ObjectInputStream(new ByteArrayInputStream(body));
//            return oin.readObject();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    public static byte[] serializeMessage(Serializable o){
        return SerializationUtils.serialize(o);
//        try{
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(bos);
//            oos.writeObject(o);
//            return bos.toByteArray();
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return null;
//        }
    }
}
