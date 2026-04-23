package com.example.CafeteriaApp.Helpers;

import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuDataUploader {

    public static Task<Void> uploadFullMenu() {
        int PH = R.drawable.ic_launcher_foreground;
        List<Product> products = new ArrayList<>();

        // --- קטגוריה: כריכים וטוסטים ---
        List<Addon> breadOptions = Arrays.asList(
                new Addon("bread_grains", "לחם דגנים", 0, PH),
                new Addon("bread_brown", "לחם חום", 0, PH),
                new Addon("bread_white", "לחם לבן", 0, PH)
        );

        products.add(new Product("1001", "כריך קטן", "טונה / שקשוקה / אבוקדו", 10.0, "כריכים וטוסטים", breadOptions, PH, 1));
        products.add(new Product("1002", "כריך גדול", "טונה / אבוקדו / בולגרית / ביצה / צהובה", 12.0, "כריכים וטוסטים", breadOptions, PH, 1));
        products.add(new Product("1003", "באגט שקשוקה", "ביצה, כרוב לבן ומלפפון חמוץ", 15.0, "כריכים וטוסטים", null, PH, 1));
        products.add(new Product("1004", "טוסט קטן", "מוגש בלחמניה", 9.0, "כריכים וטוסטים", null, PH, 1));
        
        List<Addon> toastAddons = Arrays.asList(
                new Addon("add_tuna", "תוספת טונה", 3.0, PH),
                new Addon("add_egg", "תוספת ביצה", 2.0, PH),
                new Addon("add_bulgarian", "תוספת בולגרית", 3.0, PH)
        );
        products.add(new Product("1005", "טוסט גדול", "מוגש בבאגט", 15.0, "כריכים וטוסטים", toastAddons, PH, 1));

        // --- קטגוריה: סלטים ---
        List<Addon> saladAddons = Arrays.asList(
                new Addon("add_egg_s", "ביצה קשה", 2.0, PH),
                new Addon("add_bulgarian_s", "גבינה בולגרית", 5.0, PH),
                new Addon("add_tuna_s", "טונה", 5.0, PH),
                new Addon("add_avocado_s", "אבוקדו", 2.0, PH)
        );
        products.add(new Product("2001", "סלט רגיל", "הרכבה אישית עם רוטב ישראלי", 12.0, "סלטים", saladAddons, PH, 1));
        products.add(new Product("2002", "סלט גדול", "הרכבה אישית עם רוטב ישראלי", 18.0, "סלטים", saladAddons, PH, 1));

        // --- קטגוריה: מוקפץ ---
        products.add(new Product("3001", "מוקפץ רגיל", "500 מ\"ל - ירקות טריים ברוטב טריאקי וסויה", 12.0, "מוקפץ", null, PH, 1));
        products.add(new Product("3002", "מוקפץ גדול", "750 מ\"ל - ירקות טריים ברוטב טריאקי וסויה", 16.0, "מוקפץ", null, PH, 1));

        // --- קטגוריה: תוספות ---
        products.add(new Product("4001", "אורז בקערה", "מנה רגילה", 10.0, "תוספות", null, PH, 1));
        products.add(new Product("4002", "קוסקוס צמחוני קטן", "מנה קטנה", 13.0, "תוספות", null, PH, 1));
        products.add(new Product("4003", "קוסקוס צמחוני גדול", "מנה גדולה", 19.0, "תוספות", null, PH, 1));

        // --- קטגוריה: חטיפים ---
        products.add(new Product("5001", "תפוצ'יפס", "חטיף תפוחי אדמה קלאסי", 5.0, "חטיפים", null, PH, 1));
        products.add(new Product("5002", "ביסלי", "גריל / ברביקיו / בצל", 5.0, "חטיפים", null, PH, 1));
        products.add(new Product("5003", "אפרופו", "חטיף תירס פריך", 5.0, "חטיפים", null, PH, 1));
        products.add(new Product("5004", "קינדר בואנו", "אצבעות שוקולד במילוי קרם אגוזים", 6.0, "חטיפים", null, PH, 1));
        products.add(new Product("5005", "במבה נוגט", "חטיף בוטנים במילוי נוגט", 6.0, "חטיפים", null, PH, 1));

        // --- קטגוריה: שתייה חמה ---
        products.add(new Product("6001", "נס קפה", "על בסיס חלב / מים", 7.0, "שתייה חמה", null, PH, 1));
        products.add(new Product("6002", "הפוך", "קפה הפוך קלאסי", 7.0, "שתייה חמה", null, PH, 1));
        products.add(new Product("6003", "אספרסו", "מנה רגילה", 6.0, "שתייה חמה", null, PH, 1));
        products.add(new Product("6004", "אספרסו עם חלב", "מקיאטו", 6.0, "שתייה חמה", null, PH, 1));
        products.add(new Product("6005", "תה בטעמים", "חליטות תה שונות", 6.0, "שתייה חמה", null, PH, 1));
        products.add(new Product("6006", "קפה שחור", "קפה בוץ קלאסי", 5.0, "שתייה חמה", null, PH, 1));

        // --- קטגוריה: שתייה קרה ---
        products.add(new Product("7001", "מים מינרליים", "500 מ\"ל", 5.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7002", "פחית קוקה-קולה", "330 מ\"ל", 7.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7003", "פחית קולה זירו", "330 מ\"ל", 7.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7004", "פחית ספרייט", "330 מ\"ל", 7.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7005", "פחית פאנטה", "330 מ\"ל", 7.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7006", "פחית סודה", "330 מ\"ל", 3.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7007", "בקבוק קוקה-קולה", "500 מ\"ל", 9.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7008", "בקבוק קולה זירו", "500 מ\"ל", 9.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7009", "בקבוק ספרייט", "500 מ\"ל", 9.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7010", "בקבוק פאנטה", "500 מ\"ל", 9.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7011", "בקבוק ענבים", "500 מ\"ל", 9.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7012", "ברד קטן", "בטעמים משתנים", 5.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7013", "ברד גדול", "בטעמים משתנים", 8.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7014", "אייס קפה קטן", "חלבי וקפוא", 5.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7015", "אייס קפה גדול", "חלבי וקפוא", 8.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7016", "אייס קפה דיאט קטן", "מופחת קלוריות", 5.0, "שתייה קרה", null, PH, 1));
        products.add(new Product("7017", "אייס קפה דיאט גדול", "מופחת קלוריות", 8.0, "שתייה קרה", null, PH, 1));

        List<Task<Void>> tasks = new ArrayList<>();
        for (Product p : products) {
            tasks.add(FBRef.refProducts.child(p.getCategory()).child(p.getId()).setValue(p));
        }

        return Tasks.whenAll(tasks);
    }
}
