package oop.ex6.main;

import oop.ex6.blocks.GlobalBlock;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Checks if an SJava file has any errors.
 */
public class Sjavac {

    private static final int LEGAL_CODE = 0, ILLEGAL_CODE = 1, IO_ERROR = 2,
            FILE_LOCATION = 0;
    private static final String FILE_NOT_FOUND = "IO ERROR: File was not found.",
            IO_ERROR_STRING = "IO ERROR: A problem has occurred when reading the file.";

    public static void main(String[] args) {
        try {
            new GlobalBlock(args[FILE_LOCATION]);
            System.out.println(LEGAL_CODE);
        } catch (SJavaFormatException e) {
            e.printStackTrace();
            System.out.println(ILLEGAL_CODE);
        } catch (FileNotFoundException e) {
            System.err.println(FILE_NOT_FOUND);
            System.out.println(IO_ERROR);
        } catch (IOException e) {
            System.err.println(IO_ERROR_STRING);
            System.out.println(IO_ERROR);
        }
    }
}
