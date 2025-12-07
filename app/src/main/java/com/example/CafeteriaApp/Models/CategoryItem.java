package com.example.CafeteriaApp.Models;

/**
 * Represents a category in the menu.
 * Contains the category name and its associated icon resource ID.
 */
public class CategoryItem
{
    private String name;
    private int iconRes;

    /**
     * Default constructor for Firebase or empty initialization.
     */
    public CategoryItem()
    {
    }

    /**
     * Constructs a CategoryItem with a name and an icon.
     *
     * @param name    The name of the category.
     * @param iconRes The resource ID of the category icon.
     */
    public CategoryItem(String name, int iconRes)
    {
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getIconRes()
    {
        return iconRes;
    }

    public void setIconRes(int iconRes)
    {
        this.iconRes = iconRes;
    }
}
