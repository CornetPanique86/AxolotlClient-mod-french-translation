package io.github.axolotlclient.modules.hud.gui.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 * @license GPL-3.0
 */

@AllArgsConstructor
public enum AnchorPoint {
    TOP_LEFT(-1, 1),
    TOP_MIDDLE(0, 1),
    TOP_RIGHT(1, 1),
    MIDDLE_LEFT(-1, 0),
    MIDDLE_MIDDLE(0, 0),
    MIDDLE_RIGHT(1, 0),
    BOTTOM_LEFT(-1, -1),
    BOTTOM_MIDDLE(0, -1),
    BOTTOM_RIGHT(1, -1),
    ;

    @Getter
    private final int xComponent;

    @Getter
    private final int yComponent;

    public int getX(int anchorX, int width) {
        return switch (xComponent) {
            case 0 -> anchorX - (width / 2);
            case 1 -> anchorX - width;
            default -> anchorX;
        };
    }

    public int getY(int anchorY, int height) {
        return switch (yComponent) {
            case 0 -> anchorY - (height / 2);
            case 1 -> anchorY - height;
            default -> anchorY;
        };
    }

    public int offsetWidth(int width) {
        return switch (xComponent) {
            case 0 -> width / 2;
            case 1 -> width;
            default -> 0;
        };
    }

    public int offsetHeight(int height) {
        return switch (yComponent) {
            case 0 -> (height / 2);
            case 1 -> 0;
            default -> height;
        };
    }
}
