package rabbit.flt.common.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import java.util.UUID;

public class UUIDUtil {

    private static final TimeBasedGenerator generator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

    public static UUID uuid() {
        return generator.generate();
    }

    public static long timestamp(String uuid) {
        UUID id = UUID.fromString(uuid);
        if (1 == id.version()) {
            return (id.timestamp() - 0x01b21dd213814000L) / 10000;
        }
        throw new RuntimeException("timestamp is not supported by this version");
    }
}
