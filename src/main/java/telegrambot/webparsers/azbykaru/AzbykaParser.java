package telegrambot.webparsers.azbykaru;

import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Класс, предназначенный исключительно для парсинга определенных элементов (html блоков) сайта Azbyka.ru (православный сайт).
 * Принцип единственной ответственности ООП.
 * Парсинг производится с помощью библиотеки Jsoup, посредством определенного в ней html-парсера. Для парсинга используется
 * возможность применять синтаксис в стиле CSS-селекторов.
 * @author borodatyidrug
 */
public class AzbykaParser {
	// Мапа для хранения необходимых элементов распарсенных блоков
    private final Map<String, String> map;
    private final String EMPTY = "Содержание отсутствует. Вероятно, поменялась структура искомого блока HTML-кода, "
    		+ "или блок отсутствует. Бывает. :)";

    public AzbykaParser() {
        this.map = new HashMap<>();
    }
    
    /**
     * Парсит информационный блок "Цитата дня" на странице "Календарь"
     * @param url Ссылка на страницу, блоки которой подлежат парсингу
     * @return Модифицированный объект этого класса
     */
    public AzbykaParser getQuoteOfDay(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements target = document.select("div.box").select("div.quote-of-day");
            try {
                String quote = target.select("div > p").first().text();
                map.put("quote", quote);
            } catch(NullPointerException e) {
                System.out.println("Такого элемента - нет. В вывод добавлен не будет.");
            }
            try {
                String author = target.select("p > em").first().ownText();
                map.put("author", author);
            } catch(NullPointerException e) {
                System.out.println("Такого элемента - нет. В вывод добавлен не будет.");
            }
            try {
                String refToSource = target.select("a[href]").attr("href");
                map.put("refToSource", refToSource);
            } catch(NullPointerException e) {
                System.out.println("Такого элемента - нет. В вывод добавлен не будет.");
            }
            try {
                String refToSourceText = target.select("a[href]").text();
                map.put("refToSourceText", refToSourceText);
            } catch(NullPointerException e) {
                System.out.println("Такого элемента - нет. В вывод добавлен не будет.");
            }
            return this;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return this;
        }
    }
    /**
     * Парсит информационный блок "Притча дня" на странице "Календарь"
     * @param url Ссылка на страницу, блоки которой подлежат парсингу
     * @return Модифицированный объект этого класса
     */
    public AzbykaParser getParableOfDay(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            String text = document
                    .select("div[id=pritcha]")
                    .select("div > p")
                    .html();
            map.put("parableOfDay", text);
            return this;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return this;
        }
    }
    
    /**
     * Парсит информационный блок "Основы православия" на странице "Календарь"
     * @param url Ссылка на страницу, блоки которой подлежат парсингу
     * @return Модифицированный объект этого класса
     */
    public AzbykaParser getFundamentals(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            StringBuilder sb = new StringBuilder();
            document.select("div[id=osnovy]")
                    .eachText()
                    .forEach(s -> sb.append(s));
            map.put("fundamentals", sb.toString());
            return this;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return this;
        }
    }
    /**
     * Строит строку, содержащую весь необходимый текст с распарсенных информационных блоков для вставки его в качестве текста сообщения,
     * которое бот отправляет в чат
     * @return Строка с текстом сформированного сообщения
     */
    public String getText() {
        if (map.isEmpty()) {
            return "Ничего нет. Пусто.";
        } else {
            return "ЦИТАТА ДНЯ (Azbyka.ru):\n\n"
                    + (map.get("quote") == null ? EMPTY : (map.get("quote") + "\n\n"))
                    + (map.get("author") == null ? "" : (map.get("author") + "\n"))
                    + map.get("refToSourceText") + "\n"
                    + map.get("refToSource") + "\n\n"
                    + "ПРИТЧА ДНЯ:\n\n"
                    + (map.get("parableOfDay") == null ? EMPTY : (map.get("parableOfDay") + "\n\n"))
                    + "ОСНОВЫ ПРАВОСЛАВИЯ:\n\n"
                    + (map.get("fundamentals") == null ? EMPTY : (map.get("fundamentals") + "\n\n"));
        }
    }
}
