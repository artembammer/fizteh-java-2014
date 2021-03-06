package ru.fizteh.fivt.students.artem_gritsay.Storable.Tests;


import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.students.artem_gritsay.Junit.Interpretator.Command;
import ru.fizteh.fivt.students.artem_gritsay.Junit.Interpretator.Interpretator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;

public class InterpreterTest {
    private final String temppathtonewline = System.getProperty("line.separator");
    private final String testCommand = "test";
    private final String testOutput = "TEST";
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;

    @Before
    public void run() {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInterpreterThrowsExceptionConstructedForNullStream() {
        new Interpretator(null, new Command[] {}, null, null);
    }

    @Test
    public void runInInteractive()
            throws Exception {
        Interpretator interpretator = new Interpretator(null, new Command[] {
                new Command("test", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object testConnector, String[] arguments) {
                        printStream.println(testOutput);
                    }
                })}, new ByteArrayInputStream(
                (testCommand + temppathtonewline + "exit" + temppathtonewline).getBytes()), printStream);
        interpretator.run(new String[]{});
        assertEquals(" $" + testOutput + temppathtonewline + " $",
                outputStream.toString());
    }

    @Test
    public void runInBatchMode() throws Exception {
        Interpretator interpreter = new Interpretator(null, new Command[] {
                new Command("test", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object testConnector, String[] arguments) {
                        printStream.println(testOutput);
                    }
                })}, new ByteArrayInputStream(new byte[] {}), printStream);
        interpreter.run(new String[] {testCommand + ";", testCommand});
        assertEquals(testOutput + temppathtonewline + testOutput + temppathtonewline, outputStream.toString());
    }

    @Test
    public void butchModeForUnexpectedCommand() throws Exception {
        Interpretator interpreter = new Interpretator(null, new Command[] {},
                new ByteArrayInputStream(new byte[] {}), printStream);
        interpreter.run(new String[] {testCommand + ";", testCommand});
        assertEquals("No such command declared: "
                + testCommand + temppathtonewline, outputStream.toString());
    }

    @Test
    public void runInterpreterInInteractiveModeForUnexpectedCommand()
            throws Exception {
        String testInput = testCommand + temppathtonewline + testCommand;
        Interpretator interpreter = new Interpretator(null, new Command[] {},
                new ByteArrayInputStream(testInput.getBytes()), printStream);
        interpreter.run(new String[] {});
        String expectedOutput
                = " $" + "No such command declared: " + testCommand + temppathtonewline
                + " $" + "No such command declared: " + testCommand + temppathtonewline
                + " $";
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void commandWithWrongNumberOfArguments()
            throws Exception {
        Interpretator interpreter = new Interpretator(null, new Command[] {
                new Command("test", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object testConnector, String[] arguments) {
                        printStream.println(testOutput);
                    }
                })}, new ByteArrayInputStream(new byte[] {}), printStream);
        interpreter.run(new String[] {testCommand + " some_argument"});
    }
}

