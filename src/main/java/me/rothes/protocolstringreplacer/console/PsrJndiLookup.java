package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.net.JndiManager;
import org.bukkit.Bukkit;

import javax.naming.NamingException;
import java.util.Objects;

@Plugin(name = "jndi", category = StrLookup.CATEGORY)
public class PsrJndiLookup implements StrLookup {

    static final String CONTAINER_JNDI_RESOURCE_PATH_PREFIX = "java:comp/env/";

    @Override
    public String lookup(final String key) {
        return lookup(null, key);
    }

    @Override
    public String lookup(LogEvent event, String key) {
        if (key == null) {
            return null;
        }
        final String jndiName = convertJndiName(key);
        // TODO: Maybe add a config for those really need jndi..?
        if (true) {
            // runTaskLater to avoid errors.
            Bukkit.getScheduler().runTaskLater(ProtocolStringReplacer.getInstance()
                    , () -> ProtocolStringReplacer.info("Blocked not whitelisted Jndi looking up [" + key + "]")
                    , 0L);
            return null;
        }

        try (final JndiManager jndiManager = JndiManager.getDefaultManager()) {
            return Objects.toString(jndiManager.lookup(jndiName), null);
        } catch (final NamingException e) {
            return null;
        }
    }

    private String convertJndiName(final String jndiName) {
        if (!jndiName.startsWith(CONTAINER_JNDI_RESOURCE_PATH_PREFIX) && jndiName.indexOf(':') == -1) {
            return CONTAINER_JNDI_RESOURCE_PATH_PREFIX + jndiName;
        }
        return jndiName;
    }

}
