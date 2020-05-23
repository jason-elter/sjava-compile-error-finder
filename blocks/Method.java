package oop.ex6.blocks;

import oop.ex6.main.SJavaFormatException;
import oop.ex6.main.Variable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a method in an SJava file.
 */
class Method extends LocalBlock {

    // Constants.
    private static final String METHOD_REGEX = "void\\s+([a-zA-Z]\\w*)\\s*\\(([^)]*)\\)\\s*\\{",
            INT = "int", DOUBLE = "double", BOOLEAN = "boolean";
    private static final Pattern METHOD_PATTERN = Pattern.compile(METHOD_REGEX);
    private static final int DEFINITION = 0, STARTING_INDEX = 1, NAME_GROUP = 1, VARIABLES_GROUP = 2;

    // Variables.
    private List<String> lines, parameterTypes, toRevert;
    private String name;

    /**
     * Creates a new Method and checks the definition for errors.
     *
     * @param global the Block that contains this method.
     * @param lines  the lines of method (excludes closing line).
     */
    Method(GlobalBlock global, List<String> lines) throws SJavaFormatException {
        super(global, global);
        this.lines = lines;
        Matcher matcher = METHOD_PATTERN.matcher(lines.get(DEFINITION));
        if (!matcher.matches())
            throw new SJavaFormatException(); // Method definition incorrect.
        name = matcher.group(NAME_GROUP);
        toRevert = new LinkedList<>();
        parameterTypes = new LinkedList<>();
        String variablesString = matcher.group(VARIABLES_GROUP);
        if (variablesString != null && !(variablesString = variablesString.trim()).isEmpty()) {
            List<Variable> variables = Variable.interpretVariables(variablesString);
            for (Variable variable : variables)
                parameterTypes.add(variable.getType());
            addVariables(variables);
        }
    }

    /**
     * Checks the method lines for errors.
     */
    void readMethod() throws SJavaFormatException {
        ListIterator<String> iterator = lines.listIterator(STARTING_INDEX);
        for (int i = STARTING_INDEX; i < (lines.size() - 1); i = iterator.nextIndex())
            readLine(iterator.next(), iterator);
        parseReturn(iterator.next()); // Method must end with 'return;'.
        revertGlobalVariables(); // Returns global variables to their previous state.
    }

    /**
     * Calls this method.
     * Throws an SJavaFormatException if the type list isn't compatible
     * with this method's parameter requirements.
     *
     * @param typeList A list of types to compare with. (Order is important)
     */
    void call(List<String> typeList) throws SJavaFormatException {
        if (typeList.size() != parameterTypes.size())
            throw new SJavaFormatException();
        Iterator<String> ParameterIterator = parameterTypes.iterator(), typeIterator = typeList.iterator();
        while (ParameterIterator.hasNext()) {
            String parameter = ParameterIterator.next(), type = typeIterator.next();
            if (!parameter.equals(type))
                switch (parameter) {
                    case BOOLEAN:
                        if (type.equals(DOUBLE))
                            break;
                    case DOUBLE:
                        if (type.equals(INT))
                            break;
                    default:
                        throw new SJavaFormatException();
                }
        }
    }

    @Override
    boolean assignVariable(String name, String value) throws SJavaFormatException {
        if (assignVariableToThis(name, value))
            return true;
        toRevert.add(name);
        return assignVariableToGlobal(name, value);
    }

    /**
     * Returns the name of this method.
     *
     * @return the name of this method.
     */
    String getName() {
        return name;
    }

    /*
     * Reverts all global variables whose assignment was changed by this
     * method to their previous state.
     */
    private void revertGlobalVariables() throws SJavaFormatException {
        for (String name : toRevert)
            revertVariable(name);
    }
}
