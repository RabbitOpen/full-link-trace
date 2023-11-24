package rabbit.flt.common.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import rabbit.flt.common.exception.FltException;

import java.util.UUID;

public class UUIDUtils {

    private static final UUIDUtils inst = new UUIDUtils();

    private UUIDUtils() {}

    private TimeBasedGenerator generator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

    public static UUID uuid() {
        return inst.generator.generate();
    }

    public static long timestamp(String uuid) {
        UUID id = UUID.fromString(uuid);
        if (1 == id.version()) {
            return (id.timestamp() - 0x01b21dd213814000L) / 10000;
        }
        throw new FltException("timestamp is not supported by this version");
    }
}
