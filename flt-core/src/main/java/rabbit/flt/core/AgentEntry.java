package rabbit.flt.core;

import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;

import java.lang.instrument.Instrumentation;

/**
 * 代理人口
 */
public class AgentEntry {

    private static final Logger logger = AgentLoggerFactory.getLogger(AgentEntry.class);

    public static void premain(String agentConfig, Instrumentation inst) throws Exception {

    }
}
