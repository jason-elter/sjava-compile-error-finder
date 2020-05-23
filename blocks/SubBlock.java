package oop.ex6.blocks;

import oop.ex6.main.SJavaFormatException;
import oop.ex6.main.Variable;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an if or while block in an SJava file.
 */
class SubBlock extends LocalBlock {

    // Constants.
    private static final int VALUE_GROUP = 1;
    private static final String SUB_BLOCK_REGEX = "(?:if|while)\\s*\\(([^)]*)\\)\\s*\\{",
            SPLIT_REGEX = "\\s*(&&|\\|\\|)\\s*";
    private static final Pattern SUB_BLOCK_PATTERN = Pattern.compile(SUB_BLOCK_REGEX);

    /**
     * Creates a new SubBlock.
     *
     * @param line     the definition line of this SubBlock.
     * @param iterator an iterator that contains the lines of this SubBlock.
     * @param parent   the parent Block of this SubBlock.
     * @param global   the GlobalBlock of this file.
     */
    SubBlock(String line, Iterator<String> iterator, Block parent,
             GlobalBlock global) throws SJavaFormatException {
        super(parent, global);
        Matcher matcher = SUB_BLOCK_PATTERN.matcher(line);
        if (!matcher.matches())
            throw new SJavaFormatException();
        parseCondition(matcher.group(VALUE_GROUP).trim());
        readSubBlock(iterator);
    }

    // Reads the lines of this SubBlock.
    private void readSubBlock(Iterator<String> iterator) throws SJavaFormatException {
        String line;
        while (!(line = iterator.next()).equals(BLOCK_END))
            readLine(line, iterator);
    }

    // Interprets the SubBlock's condition.
    private void parseCondition(String conditionLine) throws SJavaFormatException {
        if (!conditionLine.isEmpty())
            for (String condition : conditionLine.split(SPLIT_REGEX))
                if (Variable.notBoolean(condition))
                    Variable.equalsBoolean(getVariableType(condition));
    }
}
