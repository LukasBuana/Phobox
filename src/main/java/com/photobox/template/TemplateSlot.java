package com.photobox.template;

public class TemplateSlot {

    private final int id;
    private final int slotIndex;

    private final int x;
    private final int y;

    private final int width;
    private final int height;

    private final double rotation;

    public TemplateSlot(
            int id,
            int slotIndex,
            int x,
            int y,
            int width,
            int height,
            double rotation
    ) {
        this.id = id;
        this.slotIndex = slotIndex;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public int getId() {
        return id;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "TemplateSlot{" +
                "slotIndex=" + slotIndex +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                '}';
    }
}
