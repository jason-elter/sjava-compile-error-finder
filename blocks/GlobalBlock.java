package oop.ex6.blocks;

import oop.ex6.main.FilteredTrimmedReader;
import oop.ex6.main.SJavaFormatException;
import oop.ex6.main.Variable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the outermost block in an SJava file.
 */
public class GlobalBlock extends Block {

    // Variables.
    private HashMap<String, Method> methodDictionary;

    /**
     * Creates a new GlobalBlock.
     *
     * @param file the file path (including name) of the file to read.
     */
    public GlobalBlock(String file) throws IOException, SJavaFormatException {
        methodDictionary = new HashMap<>();
        compile(file);
        for (Method method : methodDictionary.values())
            method.readMethod();
    }

    /*
     * Creates and returns a list of the lines in a block, from block
     * definition to end of the block (excluding closing line).
     */
    private static List<String> createBlockLinesList(String line, FilteredTrimmedReader reader)
            throws SJavaFormatException, IOException {
        LinkedList<String> lineList = new LinkedList<>();
        int blockCounter = 0;
        do {
            lineList.add(line);
            if ((line = reader.readLine()) == null)
                throw new SJavaFormatException();
            if (line.equals(BLOCK_END))
                blockCounter--;
            else if (line.charAt(line.length() - 1) == BLOCK)
                blockCounter++;
        } while (blockCounter > -1);
        return lineList;
    }

    /**
     * Calls this method.
     * Throws an SJavaException if the list of types is incompatible
     * with this method's parameters.
     *
     * @param name  the name of the variable.
     * @param types the list of variable types we're calling with. (Order is important)
     */
    void callMethod(String name, List<String> types) throws SJavaFormatException {
        Method toCall = methodDictionary.get(name);
        if (toCall == null)
            throw new SJavaFormatException();
        toCall.call(types);
    }

    /**
     * If exists and assigned, returns the type of the variable with the given name.
     * Otherwise, throws an SJavaException.
     *
     * @param name the name of the variable.
     * @return the type of the variable.
     */
    @Override
    public String getVariableType(String name) throws SJavaFormatException {
        String type = super.getVariableType(name);
        if (type == null)
            throw new SJavaFormatException();
        return type;
    }

    // Reads the file and checks for errors.
    private void compile(String file) throws IOException, SJavaFormatException {
        try (FilteredTrimmedReader reader = new FilteredTrimmedReader(
                new BufferedReader(new FileReader(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                char end = line.charAt(line.length() - 1);
                if (end == SINGLE_LINE)
                    addVariables(Variable.interpretVariables(line, this));
                else if (end == BLOCK)
                    createMethod(line, reader);
                else
                    throw new SJavaFormatException();
            }
        }
    }

    // Creates and adds to the method dictionary a new method.
    private void createMethod(String line, FilteredTrimmedReader reader)
            throws SJavaFormatException, IOException {
        Method method = new Method(this, createBlockLinesList(line, reader));
        String name = method.getName();
        if (methodDictionary.containsKey(name))
            throw new SJavaFormatException();
        methodDictionary.put(name, method);
    }
}
