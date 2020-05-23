package oop.ex6.blocks;

import oop.ex6.main.SJavaFormatException;
import oop.ex6.main.Variable;

import java.util.HashMap;
import java.util.List;

/**
 * Abstract class that represents a block of code in an SJava file.
 */
public abstract class Block {

    // Constants.
    /**
     * Line that represents the end of a block.
     */
    static final String BLOCK_END = "}";
    /**
     * Represents the end of a block or a single-line.
     */
    static final char BLOCK = '{', SINGLE_LINE = ';';

    // Variables.
    private HashMap<String, Variable> variableDictionary;

    /**
     * Creates a new block.
     */
    Block() {
        variableDictionary = new HashMap<>();
    }

    /**
     * Adds the given list of variables to this block.
     *
     * @param variableList the list of variables to add.
     */
    void addVariables(List<Variable> variableList) throws SJavaFormatException {
        String name;
        for (Variable variable : variableList)
            if (!variableDictionary.containsKey(name = variable.getName()))
                variableDictionary.put(name, variable);
            else
                throw new SJavaFormatException(); // Variable name conflict.
    }

    /**
     * If exists in this scope, returns the type of the variable with the given name.
     * Otherwise, returns null.
     * Throws an SJavaException if found but is unassigned.
     *
     * @param name the name of the variable.
     * @return the type of the variable.
     */
    public String getVariableType(String name) throws SJavaFormatException {
        Variable variable = variableDictionary.get(name);
        if (variable == null)
            return null;
        variable.use();
        return variable.getType();
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
    boolean assignVariable(String name, String value) throws SJavaFormatException {
        Variable toAssign = variableDictionary.get(name);
        if (toAssign == null)
            return false;
        toAssign.assign(value);
        return true;
    }

    /**
     * If exists, reverts a global variable with the same name to
     * its original assignment state.
     * If it doesn't exist, throws an SJavaFormatException.
     *
     * @param name the name of the variable.
     */
    void revertVariable(String name) throws SJavaFormatException {
        Variable toRevert = variableDictionary.get(name);
        if (toRevert == null)
            throw new SJavaFormatException();
        toRevert.revert();
    }
}
