package app.vcampus.server.entity;

import app.vcampus.server.enums.BookStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * 图书馆图书实体类。
 * 映射到数据库中的 `book` 表。
 */
@Entity
@Data
@Slf4j
@Table(name = "book")
public class LibraryBook implements IEntity {
    /**
     * Gson 实例，用于 JSON 解析。
     */
    private static final Gson gson = new Gson();

    /**
     * 图书的唯一标识符，作为主键。
     */
    @Id
    public UUID uuid;

    /**
     * 图书名称。
     */
    @Column(nullable = false)
    public String name;

    /**
     * 图书的 ISBN 号。
     */
    @Column(nullable = false)
    public String isbn;

    /**
     * 图书作者。
     */
    @Column(nullable = false)
    public String author;

    /**
     * 出版社。
     */
    @Column(nullable = false)
    public String press;

    /**
     * 图书描述。
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    public String description;

    /**
     * 馆藏位置。
     */
    @Column(nullable = false)
    public String place;

    /**
     * 封面图片的链接。
     */
    public String cover;

    /**
     * 索书号。
     */
    @Column(nullable = false, name = "call_number")
    public String callNumber;

    /**
     * 图书状态，默认为可借。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BookStatus bookStatus = BookStatus.available;


    /**
     * 从网络获取的书籍数据中创建 LibraryBook 对象。
     *
     * @param data 从网络API解析得到的包含书籍信息的 Map。
     * @return 解析成功则返回 LibraryBook 对象，否则返回 null。
     */
    public static LibraryBook fromWeb(Map<String, Object> data) {
        try {
            LibraryBook book = new LibraryBook();
            book.setName((String) data.get("bookName"));
            book.setIsbn((String) data.get("isbn"));
            book.setAuthor((String) data.get("author"));
            book.setPress((String) data.get("press"));
            book.setDescription((String) data.get("bookDesc"));
            try {
                List<String> pictures = gson.fromJson((String) data.get("pictures"), new TypeToken<List<String>>() {
                }.getType());
                book.setCover(pictures.get(0));
            } catch (Exception e) {
                log.warn("Failed to parse cover", e);
                e.printStackTrace();
            }
            return book;
        } catch (Exception e) {
            log.warn("Failed to parse book from web", e);
            return null;
        }
    }

}