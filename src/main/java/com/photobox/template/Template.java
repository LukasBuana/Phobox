package com.photobox.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Template {

    private final int id;

    private final String name;
    private final String description;

    private final String backgroundPath;

    private final int canvasWidth;
    private final int canvasHeight;

    private final int photoCount;

    private final boolean active;

    private final List<TemplateSlot> slots;

    public Template(
            int id,
            String name,
            String description,
            String backgroundPath,
            int canvasWidth,
            int canvasHeight,
            int photoCount,
            boolean active,
            List<TemplateSlot> slots
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.backgroundPath = backgroundPath;

        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        this.photoCount = photoCount;

        this.active = active;

        this.slots = new ArrayList<>(slots);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public boolean isActive() {
        return active;
    }

    public List<TemplateSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", canvasWidth=" + canvasWidth +
                ", canvasHeight=" + canvasHeight +
                ", photoCount=" + photoCount +
                ", active=" + active +
                ", slots=" + slots.size() +
                '}';
    }
}
