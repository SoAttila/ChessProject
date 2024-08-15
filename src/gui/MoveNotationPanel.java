package gui;

import javax.swing.*;
import java.awt.*;

public class MoveNotationPanel extends JPanel {
    DefaultListModel<String> listModel;
    JList<String> moveNotationsList;
    JScrollPane scrollPane;

    public MoveNotationPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setPreferredSize(new Dimension(GuiConstants.MOVE_NOTATION_PANEL_WIDTH, GuiConstants.FRAME_LENGTH));
        listModel = new DefaultListModel<>();
        moveNotationsList = new JList<>(listModel);
        moveNotationsList.setVisibleRowCount(GuiConstants.VISIBLE_ROWS);
        moveNotationsList.setFont(GuiConstants.MOVE_NOTATION_LIST_FONT);
        scrollPane = new JScrollPane(moveNotationsList);
        scrollPane.setViewportView(moveNotationsList);
        add(scrollPane);
    }

    public DefaultListModel<String> getListModel() {
        return listModel;
    }
}
