package gui;

import model.ModelConstants;
import model.PieceType;
import model.PlayerEnum;

import javax.swing.*;
import java.awt.*;

public class PromotionPanel extends JPanel {
    private final PromotionLabel[] promoteTo;
    private PlayerEnum color;

    public PromotionPanel() {
        promoteTo = new PromotionLabel[ModelConstants.POSSIBLE_PROMOTIONS];
        color = PlayerEnum.WHITE;
        FlowLayout layout = new FlowLayout();
        layout.setHgap(0);
        layout.setVgap(0);
        setLayout(layout);
        setPreferredSize(new Dimension(GuiConstants.SQUARE_SIZE * ModelConstants.POSSIBLE_PROMOTIONS + 5, GuiConstants.SQUARE_SIZE));
        setVisible(true);
        promoteTo[0] = new PromotionLabel(PieceType.QUEEN);
        promoteTo[1] = new PromotionLabel(PieceType.KNIGHT);
        promoteTo[2] = new PromotionLabel(PieceType.ROOK);
        promoteTo[3] = new PromotionLabel(PieceType.BISHOP);

        for (int i = 0; i < ModelConstants.POSSIBLE_PROMOTIONS; ++i) {
            add(promoteTo[i]);
            promoteTo[i].setBounds(i * GuiConstants.SQUARE_SIZE, 0, GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE);
        }

        updateIcons();
    }

    public void setColor(PlayerEnum color) {
        this.color = color;
    }

    public void updateIcons() {
        for (int i = 0; i < ModelConstants.POSSIBLE_PROMOTIONS; ++i) {
            if (color == PlayerEnum.WHITE)
                promoteTo[i].setIcon(GuiConstants.WHITE_PROMOTION_ICONS[i]);
            else
                promoteTo[i].setIcon(GuiConstants.BLACK_PROMOTION_ICONS[i]);
        }
    }

    public PromotionLabel getPromotionLabel(int i) {
        return promoteTo[i];
    }
}
