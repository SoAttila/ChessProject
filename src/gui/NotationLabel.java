package gui;

import javax.swing.*;
import java.awt.*;

public class NotationLabel extends JLabel {

    public NotationLabel(char notation) {
        super(notation + "", SwingConstants.CENTER);
        setFont(GuiConstants.NOTATION_FONT);
        setPreferredSize(new Dimension(GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE));
    }
}
