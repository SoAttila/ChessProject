import gui.ChessFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ChessFrame chessFrame = new ChessFrame();
        SwingUtilities.invokeLater(() -> chessFrame.setVisible(true));
    }
}
