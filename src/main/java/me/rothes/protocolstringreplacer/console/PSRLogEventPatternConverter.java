package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;

import java.lang.reflect.Field;
import java.util.List;

@ConverterKeys({"PSRFormatting"})
@Plugin(name = "PSRFormatting", category = PatternConverter.CATEGORY)
@SuppressWarnings("unused")
public class PSRLogEventPatternConverter extends LogEventPatternConverter {

    private final List<PatternFormatter> formatters;
    private final boolean removeAnsi;
    private Field levelField;

    protected PSRLogEventPatternConverter(List<PatternFormatter> formatters, boolean removeAnsi) {
        super("PSRFormatting", null);
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
        }
    }

    public static PSRLogEventPatternConverter newInstance(Configuration config, String[] options) {
        return new PSRLogEventPatternConverter(PatternLayout.createPatternParser(config).parse(options[0]),
                options.length >= 2 && "removeAnsi".equals(options[1]));
    }

}
