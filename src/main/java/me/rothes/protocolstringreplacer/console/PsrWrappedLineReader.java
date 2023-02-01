package me.rothes.protocolstringreplacer.console;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Buffer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.Expander;
import org.jline.reader.Highlighter;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.Widget;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public class PsrWrappedLineReader implements LineReader{

    private final LineReader reader;

    public PsrWrappedLineReader(LineReader reader) {
        this.reader = reader;
    }

    public LineReader getOriReader() {
        return reader;
    }

    @Override
    public Map<String, KeyMap<Binding>> defaultKeyMaps() {
        return reader.defaultKeyMaps();
    }

    @Override
    public String readLine() throws UserInterruptException, EndOfFileException {
        return reader.readLine();
    }

    @Override
    public String readLine(Character mask) throws UserInterruptException, EndOfFileException {
        return reader.readLine(mask);
    }

    @Override
    public String readLine(String prompt) throws UserInterruptException, EndOfFileException {
        return reader.readLine(prompt);
    }

    @Override
    public String readLine(String prompt, Character mask) throws UserInterruptException, EndOfFileException {
        return reader.readLine(prompt, mask);
    }

    @Override
    public String readLine(String prompt, Character mask, String buffer) throws UserInterruptException, EndOfFileException {
        return reader.readLine(prompt, mask, buffer);
    }

    @Override
    public String readLine(String prompt, String rightPrompt, Character mask, String buffer) throws UserInterruptException, EndOfFileException {
        return reader.readLine(prompt, rightPrompt, mask, buffer);
    }

    @Override
    public String readLine(String prompt, String rightPrompt, MaskingCallback maskingCallback, String buffer) throws UserInterruptException, EndOfFileException {
        return reader.readLine(prompt, rightPrompt, maskingCallback, buffer);
    }

    @Override
    public void printAbove(String str) {
        if (!str.isEmpty()) {
            reader.printAbove(str);
        }
    }

    @Override
    public void printAbove(AttributedString str) {
        printAbove(str.toAnsi(reader.getTerminal()));
    }

    @Override
    public boolean isReading() {
        return reader.isReading();
    }

    @Override
    public LineReader variable(String name, Object value) {
        return reader.variable(name, value);
    }

    @Override
    public LineReader option(Option option, boolean value) {
        return reader.option(option, value);
    }

    @Override
    public void callWidget(String name) {
        reader.callWidget(name);
    }

    @Override
    public Map<String, Object> getVariables() {
        return reader.getVariables();
    }

    @Override
    public Object getVariable(String name) {
        return reader.getVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        reader.setVariable(name, value);
    }

    @Override
    public boolean isSet(Option option) {
        return reader.isSet(option);
    }

    @Override
    public void setOpt(Option option) {
        reader.setOpt(option);
    }

    @Override
    public void unsetOpt(Option option) {
        reader.unsetOpt(option);
    }

    @Override
    public Terminal getTerminal() {
        return reader.getTerminal();
    }

    @Override
    public Map<String, Widget> getWidgets() {
        return reader.getWidgets();
    }

    @Override
    public Map<String, Widget> getBuiltinWidgets() {
        return reader.getBuiltinWidgets();
    }

    @Override
    public Buffer getBuffer() {
        return reader.getBuffer();
    }

    @Override
    public String getAppName() {
        return reader.getAppName();
    }

    @Override
    public void runMacro(String macro) {
        reader.runMacro(macro);
    }

    @Override
    public MouseEvent readMouseEvent() {
        return reader.readMouseEvent();
    }

    @Override
    public History getHistory() {
        return reader.getHistory();
    }

    @Override
    public Parser getParser() {
        return reader.getParser();
    }

    @Override
    public Highlighter getHighlighter() {
        return reader.getHighlighter();
    }

    @Override
    public Expander getExpander() {
        return reader.getExpander();
    }

    @Override
    public Map<String, KeyMap<Binding>> getKeyMaps() {
        return reader.getKeyMaps();
    }

    @Override
    public String getKeyMap() {
        return reader.getKeyMap();
    }

    @Override
    public boolean setKeyMap(String name) {
        return reader.setKeyMap(name);
    }

    @Override
    public KeyMap<Binding> getKeys() {
        return reader.getKeys();
    }

    @Override
    public ParsedLine getParsedLine() {
        return reader.getParsedLine();
    }

    @Override
    public String getSearchTerm() {
        return reader.getSearchTerm();
    }

    @Override
    public RegionType getRegionActive() {
        return reader.getRegionActive();
    }

    @Override
    public int getRegionMark() {
        return reader.getRegionMark();
    }

    @Override
    public void addCommandsInBuffer(Collection<String> commands) {
        reader.addCommandsInBuffer(commands);
    }

    @Override
    public void editAndAddInBuffer(File file) throws Exception {
        reader.editAndAddInBuffer(file);
    }

    @Override
    public String getLastBinding() {
        return reader.getLastBinding();
    }

    @Override
    public String getTailTip() {
        return reader.getTailTip();
    }

    @Override
    public void setTailTip(String tailTip) {
        reader.setTailTip(tailTip);
    }

    @Override
    public void setAutosuggestion(SuggestionType type) {
        reader.setAutosuggestion(type);
    }

    @Override
    public SuggestionType getAutosuggestion() {
        return reader.getAutosuggestion();
    }

}
