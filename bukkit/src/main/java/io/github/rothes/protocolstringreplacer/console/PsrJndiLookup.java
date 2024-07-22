package io.github.rothes.protocolstringreplacer.console;

import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.util.scheduler.PsrScheduler;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

@Plugin(name = "jndi", category = StrLookup.CATEGORY)
public class PsrJndiLookup implements StrLookup {

//    static final String CONTAINER_JNDI_RESOURCE_PATH_PREFIX = "java:comp/env/";

    @Override
    public String lookup(final String key) {
        return lookup(null, key);
    }

    @Override
    public String lookup(LogEvent event, String key) {
        if (key == null) {
            return null;
        }
        // runTaskLater to avoid errors.
        PsrScheduler.runTaskLater(() -> ProtocolStringReplacer.info("Blocked not whitelisted Jndi looking up [" + key + "]")
                , 0L);
        return null;

        // TODO: Maybe add a config for those really need jndi..?
//        try (final JndiManager jndiManager = JndiManager.getDefaultManager()) {
//            final String jndiName = convertJndiName(key);
//            return Objects.toString(jndiManager.lookup(jndiName), null);
//        } catch (final NamingException e) {
//            return null;
//        }
    }

//    private String convertJndiName(final String jndiName) {
//        if (!jndiName.startsWith(CONTAINER_JNDI_RESOURCE_PATH_PREFIX) && jndiName.indexOf(':') == -1) {
//            return CONTAINER_JNDI_RESOURCE_PATH_PREFIX + jndiName;
//        }
//        return jndiName;
//    }

}
