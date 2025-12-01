package com.example.CafeteriaApp;

import com.example.CafeteriaApp.Adapters.AddonItem;
import com.example.CafeteriaApp.Adapters.ProductItem;

/**
 * מחלקה אחת שמרכזת קטגוריות ותפריט מלא בפורמט שלך (public final + arrays).
 * אין פונקציות “חכמות” — רק דאטה נקי לשימוש באפליקציה.
 */
public final class MenuCategories {

    /** זוג שם־קטגוריה + מערך מוצרים. */
    public static final class Category {
        public final String name;
        public final ProductItem[] products;

        public Category(String name, ProductItem[] products) {
            this.name = name;
            this.products = products;
        }
    }

    // --- Placeholders לאייקונים (תוכל להחליף ל־drawable אמיתיים) ---
    private static final int PH = R.drawable.ic_launcher_foreground;
    private static final int TOAST = R.drawable.toast;

    private static final int IC_AVOCADO = R.drawable.ic_launcher_foreground;
    private static final int IC_EGG     = R.drawable.ic_launcher_foreground;
    private static final int IC_CABBAGE = R.drawable.ic_launcher_foreground;
    private static final int IC_SPICE   = R.drawable.ic_launcher_foreground; // מלח/פלפל
    private static final int IC_SAUCE   = R.drawable.ic_launcher_foreground; // רוטב ביתי

    // --- Addons משותפים לכריכים (מחירים 1–2 ₪, ותבלינים חינם) ---
    private static final AddonItem ADD_AVOCADO   = new AddonItem(IC_AVOCADO, "אבוקדו", 2.00);
    private static final AddonItem ADD_EGG       = new AddonItem(IC_EGG,     "ביצה",   1.50);
    private static final AddonItem ADD_CABBAGE   = new AddonItem(IC_CABBAGE, "כרוב",   1.00);
    private static final AddonItem ADD_SALTPEP   = new AddonItem(IC_SPICE,   "מלח/פלפל", 0.00);
    private static final AddonItem ADD_HOUSE_SAU = new AddonItem(IC_SAUCE,   "רוטב ביתי", 0.00); // "רוטב של זיפים"

    // ======================
    // כריכים (טוסט/טוסט גדול/טונה/שקשוקה)
    // ======================
    public static final Category SANDWICHES = new Category(
            "כריכים",
            new ProductItem[]{
                    new ProductItem(
                            PH,
                            "טוסט",
                            "לחם טרי, גבינה צהובה",
                            8.00,
                            false,
                            null
                    ),
                    new ProductItem(
                            TOAST,
                            "טוסט גדול",
                            "לחם כפול, גבינה נדיבה",
                            10.00,
                            false,
                            null
                    ),
                    new ProductItem(
                            TOAST,
                            "כריך טונה",
                            "לבחירה: לבן/מלא",
                            10.00,
                            true,
                            new AddonItem[]{ ADD_AVOCADO, ADD_EGG, ADD_CABBAGE, ADD_SALTPEP, ADD_HOUSE_SAU }
                    ),
                    new ProductItem(
                            PH,
                            "כריך שקשוקה",
                            "שקשוקה עדינה בלחם טרי",
                            10.00,
                            true,
                            new AddonItem[]{ ADD_AVOCADO, ADD_EGG, ADD_CABBAGE, ADD_SALTPEP, ADD_HOUSE_SAU }
                    ),
            }
    );

    // ======================
    // פסטה
    // ======================
    public static final Category PASTA = new Category(
            "פסטה",
            new ProductItem[]{
                    new ProductItem(
                            PH,
                            "פסטה פנה ברוטב עגבניות",
                            "פנה, רוטב עגבניות עדין",
                            12.00,
                            false,
                            null
                    ),
            }
    );

    // ======================
    // משקאות קרים (פחיות + ברד)
    // ======================
    public static final Category COLD_DRINKS = new Category(
            "משקאות קרים",
            new ProductItem[]{
                    new ProductItem(PH, "קולה זירו (פחית)",   "330 מ״ל", 6.00, false, null),
                    new ProductItem(PH, "קוקה-קולה (פחית)",   "330 מ״ל", 6.00, false, null),
                    new ProductItem(PH, "ספרייט (פחית)",      "330 מ״ל", 6.00, false, null),
                    new ProductItem(PH, "פאנטה תפוז (פחית)",  "330 מ״ל", 6.00, false, null),
                    new ProductItem(PH, "סודה (פחית)",        "330 מ״ל", 6.00, false, null),
                    new ProductItem(PH, "ברד",                "טעמים לבחירה", 9.00, false, null),
            }
    );

    // ======================
    // קינוחים (שוקולדים)
    // ======================
    public static final Category DESSERTS = new Category(
            "קינוחים (שוקולדים)",
            new ProductItem[]{
                    new ProductItem(PH, "קינדר", "שוקולד חלב", 5.00, false, null),
                    new ProductItem(PH, "בואנו", "שוקולד/וופלים", 5.00, false, null),
            }
    );

    // ======================
    // מאפים
    // ======================
    public static final Category BAKERY = new Category(
            "מאפים",
            new ProductItem[]{
                    new ProductItem(
                            PH,
                            "בורקס תפוחי אדמה",
                            "פריך וחם",
                            8.00,
                            false,
                            null
                    ),
            }
    );

    /** כל הקטגוריות בסדר התצוגה. */
    public static final Category[] ALL = new Category[]{
            SANDWICHES,
            PASTA,
            COLD_DRINKS,
            DESSERTS,
            BAKERY
    };


    private MenuCategories() { /* no instances */ }
}
