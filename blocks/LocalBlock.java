package oop.ex6.blocks;

import oop.ex6.main.SJavaFormatException;
import oop.ex6.main.Variable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class that represents an inner block in an SJava file.
 */
abstract class LocalBlock extends Block {

    // Constants.
    private static final String VARIABLE_REGEX = "(?:final\\s+)?(?:int|String|boolean|double|char)[^;]+;",
            CALL_REGEX = "([a-zA-Z]\\w*)\\s*\\(([^)]*)\\)\\s*;",
            ASSIGNMENT_REGEX = "([a-zA-Z_]\\w*)\\s*=\\s*([^;]+)\\s*;",
            SPLIT_REGEX = ",\\s*", ASSIGNMENT = "=", RETURN_REGEX = "return\\s*;";
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX),
            CALL_PATTERN = Pattern.compile(CALL_REGEX),
            ASSIGNMENT_PATTERN = Pattern.compile(ASSIGNMENT_REGEX),
            RETURN_PATTERN = Pattern.compile(RETURN_REGEX);
    private static final int NAME_GROUP = 1, VALUE_GROUP = 2;

    // Variables.
    private GlobalBlock global;
    private Block parent;

    /**
     * Creates a new LocalBlock.
     *
     * @param parent the parent Block of this LocalBlock.
     * @param global the GlobalBlock of this file.
     */
    LocalBlock(Block parent, GlobalBlock global) {
        this.parent = parent;
        this.global = global;
    }

    /**
     * Interprets a single line inside a LocalBlock. If the line is a definition of
     * a new Block then a new SubBlock is created and checked for errors.
     *
     * @param line     the line to interpret.
     * @param iterator an iterator that provides any subsequent lines.
     */
    void readLine(String line, Iterator<String> iterator)
            throws SJavaFormatException {
        if (line == null)
            throw new SJavaFormatException();
        char end = line.charAt(line.length() - 1);
        if (end == SINGLE_LINE)
            handleSingleLine(line);
        else if (end == BLOCK)
            new SubBlock(line, iterator, this, global);
        else
            throw new SJavaFormatException();
    }

    /**
     * If exists in this scope or an outer scope, return true if assigning the given
     * value to the variable with the given name is possible. If it doesn't exist or
     * if the value is incompatible with the variable, throws an SJavaException.
     *
     * @param name  the name of the variable.
     * @param value the value to assign to the variable.
     * @return true if the variable exists in this scope and assignment succeeds.
     */
    @Override
    boolean assignVariable(String name, String value) throws SJavaFormatException {
        return super.assignVariable(name, value) || parent.assignVariable(name, value);
    }

    /**
     * If exists in this scope, return true if assigning the given value to the variable
     * with the given name is possible. If it doesn't exist returns false.
     * If the value is incompatible with the variable,
     * throws an SJavaException.
     *
     * @param name  the name of the variable.
     * @param value the value to assign to the variable.
     * @return true if the variable exists in this scope and assignment succeeds. Otherwise, returns false.
     */
    boolean assignVariableToThis(String name, String value) throws SJavaFormatException {
        return super.assignVariable(name, value);
    }

    /**
     * If exists in the global scope, return true if assigning the given value to the variable
     * with the given name is possible. If it doesn't exist or
     * if the value is incompatible with the variable, throws an SJavaException.
     *
     * @param name  the name of the variable.
     * @param value the value to assign to the variable.
     * @return true if the variable exists in this scope and assignment succeeds.
     */
    boolean assignVariableToGlobal(String name, String value) throws SJavaFormatException {
        return global.assignVariable(name, value);
    }

    /**
     * If exists in this scope or an outer scope, returns the type of the variable
     * with the given name.
     * Throws an SJavaException if not found or if found but is unassigned.
     *
     * @param name the name of the variable.
     * @return the type of the variable.
     */
    @Override
    public String getVariableType(String name) throws SJavaFormatException {
        String type = super.getVariableType(name);
        if (type != null)
            return type;
        return parent.getVariableType(name);
    }

    @Override
    void revertVariable(String name) throws SJavaFormatException {
        global.revertVariable(name);
    }

    /**
     * Throws an SJavaException if the given line isn't a return statement.
     *
     * @param line the line to check.
     */
    void parseReturn(String line) throws SJavaFormatException {
        if (!RETURN_PATTERN.matcher(line).matches())
            throw new SJavaFormatException();
    }

    // Handles a single line in a LocalBlock.
    private void handleSingleLine(String line) throws SJavaFormatException {
        if (RETURN_PATTERN.matcher(line).matches())
            return;
        if (VARIABLE_PATTERN.matcher(line).matches()) // Definition of new variable.
            addVariables(Variable.interpretVariables(line, this));
        else if (line.contains(ASSIGNMENT)) { // Assign value to existing variable.
            Matcher matcher = ASSIGNMENT_PATTERN.matcher(line);
            if (!matcher.matches())
                throw new SJavaFormatException();
            assignVariable(matcher.group(NAME_GROUP), matcher.group(VALUE_GROUP).trim());
        } else { // Call method.
            Matcher matcher = CALL_PATTERN.matcher(line);
            if (!matcher.matches())
                throw new SJavaFormatException();
            global.callMethod(matcher.group(NAME_GROUP), getTypeList(matcher.group(VALUE_GROUP).trim()));
        }
    }

    // Returns a list of Variable types according to the given method call values.
    private List<String> getTypeList(String values) throws SJavaFormatException {
        List<String> typeList = new LinkedList<>();
        if (!values.isEmpty())
            for (String value : values.split(SPLIT_REGEX)) {
                String type = Variable.parse(value);
                if (type == null)
                    type = getVariableType(value);
                typeList.add(type);
            }
        return typeList;
    }
}
