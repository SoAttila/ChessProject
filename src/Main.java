import gui.ChessFrame;
import gui.GuiConstants;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Main {
    public static void main(String[] args) {
        ChessFrame chessFrame = new ChessFrame();
        SwingUtilities.invokeLater(() -> chessFrame.setVisible(true));
    }
}
