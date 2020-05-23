package oop.ex6.main;

import oop.ex6.blocks.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a variable in an SJava file.
 */
public class Variable {

    // Constants.
    private static final String INT = "int", DOUBLE = "double", STRING = "String",
            BOOLEAN = "boolean", CHAR = "char", UNDERSCORE = "_", TRUE = "true", FALSE = "false";
    private static final int FINAL_GROUP = 1, TYPE_GROUP = 2, NAME_GROUP = 3, DECLARATION_GROUP = 4,
            VALUE_GROUP = 5, NEXT_VARIABLE_GROUP = 6, METHOD_NEXT_GROUP = 4, ADJUST = 2, NO_ADJUST = 0;

    // Regular Expressions
    private static final String LINE_START_REGEX = "(final\\s+)?(int|String|boolean|double|char)\\s+",
            VARIABLE_REGEX = "([a-zA-Z_]\\w*)(\\s*=\\s*([^;,]+))?(?:;|,\\s*([^;\\s]+)\\s*;)",
            INITIAL_REGEX = LINE_START_REGEX + VARIABLE_REGEX,
            METHOD_REGEX = LINE_START_REGEX + "([a-zA-Z_]\\w*)\\s*(?:,\\s*(.+))?",
            INT_REGEX = "-?\\d++", DOUBLE_REGEX = INT_REGEX + "(\\.\\d++)?",
            STRING_REGEX = "\"[^\"]*+\"", CHAR_REGEX = "\'[^\']\'",
            BOOLEAN_REGEX = "false|true|" + DOUBLE_REGEX;
    private static final Pattern INITIAL_PATTERN = Pattern.compile(INITIAL_REGEX),
            NEXT_PATTERN = Pattern.compile(VARIABLE_REGEX),
            METHOD_PATTERN = Pattern.compile(METHOD_REGEX),
            INTEGER_PATTERN = Pattern.compile(INT_REGEX),
            DOUBLE_PATTERN = Pattern.compile(DOUBLE_REGEX),
            STRING_PATTERN = Pattern.compile(STRING_REGEX),
            CHAR_PATTERN = Pattern.compile(CHAR_REGEX),
            BOOLEAN_PATTERN = Pattern.compile(BOOLEAN_REGEX);

    // Variables.
    private String name, type;
    private Pattern typeChecker;
    private boolean isFinal, assigned, revert;
    private LinkedList<Variable> variableList;
    private Block container;

    // Creates new independent variable.
    private Variable(String command, Block container) throws SJavaFormatException {
        Matcher matcher = INITIAL_PATTERN.matcher(command);
        if (!matcher.matches())
            throw new SJavaFormatException(); // Variable definition incorrect.
        isFinal = matcher.group(FINAL_GROUP) != null;
        this.container = container;
        type = matcher.group(TYPE_GROUP);
        typeChecker = typeCheckerFactory(type);
        variableList = new LinkedList<>();
        variableList.add(this);
        handleMatcher(matcher, NO_ADJUST);
    }

    /*
     * Creates new variable that shares its type and whether it's final
     * a previous variable. (Corresponds to the 2nd and beyond variables in the
     * format: type variable1, variable2, variable3...)
     */
    private Variable(String command, String type, Pattern typeChecker, boolean isFinal,
                     LinkedList<Variable> variableList, Block container) throws SJavaFormatException {
        this.type = type;
        this.typeChecker = typeChecker;
        this.isFinal = isFinal;
        this.variableList = variableList;
        this.container = container;
        Matcher matcher = NEXT_PATTERN.matcher(command);
        if (!matcher.matches())
            throw new SJavaFormatException(); // Variable definition incorrect.
        handleMatcher(matcher, ADJUST);
    }

    // Creates new independent variable. (Method definition version)
    private Variable(String command, LinkedList<Variable> variableList) throws SJavaFormatException {
        Matcher matcher = METHOD_PATTERN.matcher(command);
        if (!matcher.matches())
            throw new SJavaFormatException(); // Variable definition incorrect.
        this.variableList = variableList;
        assigned = true;
        isFinal = matcher.group(FINAL_GROUP) != null;
        type = matcher.group(TYPE_GROUP);
        typeChecker = typeCheckerFactory(type);
        name = matcher.group(NAME_GROUP);
        variableList.add(this);
        String moreVariables = matcher.group(METHOD_NEXT_GROUP);
        if (moreVariables != null)
            new Variable(moreVariables, variableList);
    }

    /**
     * Interprets a line and returns a list of variables that are defined in that line.
     *
     * @return a list of variables.
     */
    public static List<Variable> interpretVariables(String command, Block container) throws SJavaFormatException {
        return new Variable(command, container).variableList;
    }

    /**
     * Interprets a line that contains a method's parameters and returns
     * a list of variables that are defined in that line.
     *
     * @return a list of variables.
     */
    public static List<Variable> interpretVariables(String methodParameters) throws SJavaFormatException {
        return new Variable(methodParameters.trim(), new LinkedList<>()).variableList;
    }

    /**
     * Returns true if value isn't of type boolean. Otherwise, return false.
     *
     * @param value the value to check.
     * @return true if value isn't of type boolean. Otherwise, return false.
     */
    public static boolean notBoolean(String value) throws SJavaFormatException {
        return !BOOLEAN_PATTERN.matcher(value).matches();
    }

    /**
     * Throws an SJavaException if value doesn't equal boolean.
     *
     * @param value the value to check.
     */
    public static void equalsBoolean(String value) throws SJavaFormatException {
        if (!value.equals(BOOLEAN))
            throw new SJavaFormatException();
    }

    /**
     * Returns the type of the value. (Returns null if the type isn't valid)
     *
     * @param value the value to check.
     * @return the type of the value. (Returns null if the type isn't valid)
     */
    public static String parse(String value) {
        if (CHAR_PATTERN.matcher(value).matches())
            return CHAR;
        if (STRING_PATTERN.matcher(value).matches())
            return STRING;
        if (INTEGER_PATTERN.matcher(value).matches())
            return INT;
        if (DOUBLE_PATTERN.matcher(value).matches())
            return DOUBLE;
        if (value.equals(TRUE) || value.equals(FALSE))
            return BOOLEAN;
        return null;
    }

    // Factory method that returns the correct pattern according to the type.
    private static Pattern typeCheckerFactory(String typeString) throws SJavaFormatException {
        switch (typeString) {
            case INT:
                return INTEGER_PATTERN;
            case DOUBLE:
                return DOUBLE_PATTERN;
            case STRING:
                return STRING_PATTERN;
            case BOOLEAN:
                return BOOLEAN_PATTERN;
            case CHAR:
                return CHAR_PATTERN;
            default:
                throw new SJavaFormatException(); // Invalid type.
        }
    }

    /*
     * Handles the information that is given by the matcher's groups.
     * (Adjusts the group numbers according to whether this is an independent
     * variable or another variable on the same line.)
     */
    private void handleMatcher(Matcher matcher, int adjust) throws SJavaFormatException {
        name = matcher.group(NAME_GROUP - adjust);
        if (name.equals(UNDERSCORE))
            throw new SJavaFormatException(); // Name can't be '_'.
        assigned = revert = matcher.group(DECLARATION_GROUP - adjust) != null;
        String value = matcher.group(VALUE_GROUP - adjust);
        if (assigned && !typeChecker.matcher(value).matches()) {
            // Value type not recognized so checks if it's an existing variable.
            String type = container.getVariableType(value);
            if (type == null || !this.type.equals(type))
                throw new SJavaFormatException();
        }
        String moreVariables = matcher.group(NEXT_VARIABLE_GROUP - adjust);
        if (moreVariables != null)
            variableList.add(new Variable(moreVariables, type, typeChecker, isFinal,
                    variableList, container));
    }

    /**
     * If possible, assigns the given value to this variable.
     * Otherwise, throws SJavaFormatException.
     *
     * @param value the value or the name of the variable whose value to assign to this variable.
     */
    public void assign(String value) throws SJavaFormatException {
        if (isFinal)
            throw new SJavaFormatException(); // Can't assign to final variable.
        if (!typeChecker.matcher(value).matches()) {
            String type = container.getVariableType(value);
            if (type == null)
                throw new SJavaFormatException();
            if (!this.type.equals(type))
                switch (this.type) {
                    case BOOLEAN:
                        if (type.equals(DOUBLE))
                            break;
                    case DOUBLE:
                        if (type.equals(INT))
                            break;
                        throw new SJavaFormatException();
                }
        }
        assigned = true;
    }

    /**
     * Reverts the variable to it's original assignment state.
     */
    public void revert() {
        assigned = revert;
    }

    /**
     * Attempts to use this variable.
     * If it's unassigned then an SJavaFormatException is thrown.
     */
    public void use() throws SJavaFormatException {
        if (!assigned)
            throw new SJavaFormatException(); // Can't use uninitialized variable.
    }

    /**
     * Returns the type of this variable.
     *
     * @return the type of this variable.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name of this variable.
     *
     * @return the name of this variable.
     */
    public String getName() {
        return name;
    }
}
