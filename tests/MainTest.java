import junit.framework.TestCase;
import org.apache.commons.exec.*;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class MainTest extends TestCase {
    private static String testsPath = "data/tests/";

    private void runTest(String testName) {
        int myResult = new Main().run(new String[]{testName});
        assertNotEquals(myResult, -1);
        String pirnCommand = "soft/pirn-linux-v201-64 " + testName;
        CommandLine cmdLine = CommandLine.parse(pirnCommand);

        DefaultExecutor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        executor.setExitValue(0);
        long timeLimit = 1000000;
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeLimit);
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errStream);
        executor.setStreamHandler(streamHandler);

        long curTime = System.currentTimeMillis();
        try {
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            assertTrue("pirn failed!", false);
        }

        long executionTime = System.currentTimeMillis() - curTime;
        assertTrue(executionTime < timeLimit);

        Scanner input = new Scanner(outputStream.toString());
        String line;
        while (input.hasNextLine()) {
            line = input.nextLine();
            System.out.println(line);
            if (line.contains("The lowest number of hybridization events found so far is")) {
                String[] tokens = line.split(" ");
                int pirnResult = Integer.parseInt(tokens[tokens.length - 1]);
                line = input.nextLine();
                if (line.contains("This may not be the optimal solution")) {
//                    assertTrue(myResult <= pirnResult);
                } else {
//                    assertEquals(myResult, pirnResult);
                }
                break;
            }
        }
    }

    public void runDirectory(String dirName){
        File dir = new File(dirName);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                runTest(dirName + "/" + child.getName());
            }
        } else {
            assertTrue(false);
        }
    }

    public void testSmall() {
        runDirectory(testsPath + "small");
    }

//    public void testLarge() throws IOException {
//        runDirectory(new File(testsPath + "large"));
//    }
}