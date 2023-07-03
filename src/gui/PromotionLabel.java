package gui;

import model.PieceType;

import javax.swing.*;

public class PromotionLabel extends JLabel {
    private final PieceType pieceType;

    public PromotionLabel(PieceType pieceType) {
        super();
        this.pieceType = pieceType;
    }

    public PieceType getPieceType() {
        return pieceType;
    }
}
