package oop.ex6.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A Reader decorator that gets a buffered reader that filters single-line comments
 * and whitespace-only lines and returns trimmed lines.
 */
public class FilteredTrimmedReader extends Reader {

    // Constants.
    private static final String COMMENT = "//";

    // Variables.
    private BufferedReader in;

    /**
     * Creates a new reader decorator.
     *
     * @param in the BufferedReader to read from.
     */
    public FilteredTrimmedReader(BufferedReader in) {
        super(in);
        this.in = in;
    }

    /**
     * Reads a line of text. Operates the same way as BufferedReader but filters
     * single-line comments and empty lines (including lines that only have whitespaces).
     * Also trims all lines.
     *
     * @return A String containing the contents of the line, not including
     * any line-termination characters, or null if the end of the
     * stream has been reached without reading any characters
     */
    public String readLine() throws IOException, SJavaFormatException {
        String line;
        do {
            line = in.readLine();
        } while (line != null && (line.startsWith(COMMENT) || (line = line.trim()).isEmpty()));
        if (line != null && line.startsWith(COMMENT))
            throw new SJavaFormatException();// Detects any comments that had spaces preceding it.
        return line;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return in.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
