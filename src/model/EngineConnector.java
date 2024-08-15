package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class EngineConnector {
    private Process engineProcess;
    private BufferedReader processReader;
    private OutputStreamWriter processWriter;

    public EngineConnector() {
        try {
            Process engineProcess = Runtime.getRuntime().exec(ModelConstants.ENGINE_PATH);
            processReader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            processWriter = new OutputStreamWriter(engineProcess.getOutputStream());
            System.out.println("Engine connected.");
        } catch (Exception e) {
            System.err.println("Could not connect the engine.");
            e.printStackTrace();
        }
    }

    public void sendCommand(String command) {
        try {
            processWriter.write(command + "\n");
            processWriter.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getBestMove() {
        String bestMove = null;
        try {
            String line;
            while (processReader.ready()) {
                line = processReader.readLine();
                String[] splitLine = line.split(" ");
                if (splitLine[0].equals("bestmove")) {
                    bestMove = splitLine[1];
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return bestMove;
    }
}
