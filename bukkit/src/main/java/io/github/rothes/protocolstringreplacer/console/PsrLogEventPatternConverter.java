package io.github.rothes.protocolstringreplacer.console;

import io.github.rothes.protocolstringreplacer.ConfigManager;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager;
import io.github.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;

import java.util.List;

@ConverterKeys({"PsrFormatting"})
@Plugin(name = "PsrFormatting", category = PatternConverter.CATEGORY)
public class PsrLogEventPatternConverter extends LogEventPatternConverter {

    private final List<PatternFormatter> formatters;
    private final boolean removeAnsi;

    protected PsrLogEventPatternConverter(List<PatternFormatter> formatters, boolean removeAnsi) {
        super("PsrFormatting", null);
        this.formatters = formatters;
        this.removeAnsi = removeAnsi;
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        for (PatternFormatter formatter : this.formatters) {
            formatter.format(event, toAppendTo);
        }
        ProtocolStringReplacer plugin = ProtocolStringReplacer.getInstance();
        if (plugin.hasStarted()) {
            SimpleTextContainer container = new SimpleTextContainer(toAppendTo.toString());
            container.createTexts(container);
            ReplacerManager replacerManager = plugin.getReplacerManager();
            List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(
                    plugin.getUserManager().getConsoleUser(), ConsoleReplaceManager.getFilter());
            if (replacerManager.isTextBlocked(container, replacers)) {
                toAppendTo.delete(0, toAppendTo.length());
                return;
            }
            replacerManager.replaceContainerTexts(container, replacers);
            if (plugin.getConfigManager().consolePlaceholder) {
                replacerManager.setPapi(plugin.getUserManager().getConsoleUser(), container.getTexts());
            }
            toAppendTo.delete(0, toAppendTo.length());
            toAppendTo.append(container.getResult());
        }

        if (removeAnsi) {
            int m = - 1;
            for (int i = toAppendTo.length() - 1; i >= 0; i--) {
                char c = toAppendTo.charAt(i);
                if (c == 'm') {
                    m = i;
                } else if (c == '') {
                    toAppendTo.delete(i, m + 1);
                }
            }
        } else {
            ConfigManager configManager = plugin.getConfigManager();
            if (configManager.resetConsoleColor) {
                toAppendTo.append("\u001b[0m");
            }
        }
    }

    @SuppressWarnings("unused")
    public static PsrLogEventPatternConverter newInstance(Configuration config, String[] options) {
        if (options.length < 1 || options.length > 2) {
            LOGGER.error("Incorrect number of options on minecraftFormatting. Expected at least 1, max 2 received " + options.length);
            return null;
        }
        return new PsrLogEventPatternConverter(PatternLayout.createPatternParser(config).parse(ConsoleReplaceManager.getPatterns().get(Short.parseShort(options[0]))),
                options.length >= 2 && "removeAnsi".equals(options[1]));
    }

}
