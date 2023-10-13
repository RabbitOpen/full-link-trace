package rabbit.flt.rpc.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.concurrent.locks.ReentrantLock;

public class Serializer {

    private static Logger logger = AgentLoggerFactory.getLogger(Serializer.class);

    /**
     * 池对象
     */
    private static Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 32) {

        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo() {
                private ReentrantLock lock = new ReentrantLock();

                @Override
                public void writeObject(Output output, Object object) {
                    try {
                        lock.lock();
                        super.writeObject(output, object);
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public <T> T readObject(Input input, Class<T> type) {
                    try {
                        lock.lock();
                        return super.readObject(input, type);
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public Registration getRegistration(Class type) {
                    Registration registration = getClassResolver().getRegistration(type);
                    if (null == registration) {
                        return getClassResolver().registerImplicit(type);
                    }
                    return registration;
                }
            };
            kryo.setDefaultSerializer(new SerializerFactory.CompatibleFieldSerializerFactory());
            kryo.setRegistrationRequired(false);
            // 不支持循环引用
            kryo.setReferences(false);
            return kryo;
        }
    };

    /**
     * 序列化
     * @param data
     * @return
     */
    public static byte[] serialize(Object data) {
        Kryo kryo = kryoPool.obtain();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Output output = new Output(bos);
            kryo.writeClassAndObject(output, data);
            output.flush();
            return bos.toByteArray();
        } finally {
            kryoPool.free(kryo);
            close(bos);
        }
    }

    /**
     * 反序列化
     * @param data
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data) {
        Kryo kryo = kryoPool.obtain();
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        Input input = new Input(is);
        try {
            return (T) kryo.readClassAndObject(input);
        } finally {
            kryoPool.free(kryo);
            close(is);
        }
    }

    public static void close(Closeable resource) {
        try {
            if (null == resource) {
                return;
            }
            resource.close();
        } catch (Exception e) {

        }
    }
}
