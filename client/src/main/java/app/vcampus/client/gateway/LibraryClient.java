package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.LibraryBook;
import app.vcampus.server.entity.LibraryTransaction;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 图书馆客户端，提供与图书馆系统交互的功能，包括书籍的添加、搜索、更新、删除、借阅、续借和归还，以及查询借阅记录。
 */
@Slf4j
public class LibraryClient extends BaseClient {
    /**
     * 根据ISBN预添加书籍，获取书籍信息。
     *
     * @param handler Netty处理器。
     * @param isbn 书籍的ISBN号。
     * @return LibraryBook对象，如果获取失败则返回null。
     */
    public static LibraryBook preAddBook(NettyHandler handler, String isbn) {
        Request request = new Request();
        request.setUri("library/isbn");
        request.setParams(Map.of(
                "isbn", isbn
        ));
        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                Map<String, String> data = (Map<String, String>) response.getData();
                return IEntity.fromJson(data.get("book"), LibraryBook.class);
            } else {
                throw new RuntimeException("获取书籍信息失败");
            }
        } catch (Exception e) {
            log.warn("获取书籍信息失败", e);
            return null;
        }
    }

    /**
     * 添加新书籍到图书馆。
     *
     * @param handler Netty处理器。
     * @param newBook 要添加的LibraryBook对象。
     * @return 如果添加成功则返回true，否则返回false。
     */
    public static boolean addBook(NettyHandler handler, LibraryBook newBook) {
        Request request = new Request();
        request.setUri("library/addBook");
        request.setParams(Map.of(
                "book", newBook.toJson()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("添加书籍失败", e);
            return false;
        }
    }

    /**
     * 搜索书籍。
     *
     * @param handler Netty处理器。
     * @param keyword 搜索关键词。
     * @return 包含搜索结果的Map，如果搜索失败则返回null。
     */
    public static Map<String, List<LibraryBook>> searchBook(NettyHandler handler, String keyword) {
        Request request = new Request();
        request.setUri("library/searchBook");
        request.setParams(Map.of(
                "keyword", keyword
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                // 用 TypeToken 泛型反序列化，避免 LinkedTreeMap 问题
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, List<LibraryBook>>>(){}.getType();
                String json = gson.toJson(response.getData());
                return gson.fromJson(json, type);
            } else {
                throw new RuntimeException("获取书籍信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取书籍信息失败", e);
            return null;
        }
    }

    /**
     * 获取所有书籍列表。
     *
     * @param handler Netty处理器。
     * @return 所有书籍的列表，如果获取失败则返回null。
     */
    public static List<LibraryBook> getAllBooks(NettyHandler handler) {
        Request request = new Request();
        request.setUri("library/all");

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> raw_data = new Gson().fromJson(new Gson().toJson(response.getData()), type);
                return raw_data.stream().map(json -> IEntity.fromJson(json, LibraryBook.class)).toList();
            } else {
                throw new RuntimeException("获取所有书籍失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取所有书籍失败", e);
            return null;
        }
    }

    /**
     * 搜索可删除的书籍。
     *
     * @param handler Netty处理器。
     * @param bookName 书籍名称。
     * @return 可删除书籍的列表，如果搜索失败则返回null。
     */
    public static List<LibraryBook> searchBookForDeletion(NettyHandler handler, String bookName) {
        Request request = new Request();
        request.setUri("library/searchForDeletion");
        request.setParams(Map.of(
                "bookName", bookName
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> raw_data = new Gson().fromJson(new Gson().toJson(response.getData()), type);
                return raw_data.stream().map(json -> IEntity.fromJson(json, LibraryBook.class)).toList();
            } else {
                throw new RuntimeException("搜索待删除书籍失败");
            }
        } catch (InterruptedException e) {
            log.warn("搜索待删除书籍失败", e);
            return null;
        }
    }

    /**
     * 更新书籍信息。
     *
     * @param handler Netty处理器。
     * @param book 要更新的LibraryBook对象。
     * @return 如果更新成功则返回true，否则返回false。
     */
    public static boolean updateBook(NettyHandler handler, LibraryBook book) {
        Request request = new Request();
        request.setUri("library/updateBook");
        request.setParams(Map.of(
                "book", book.toJson()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("更新书籍失败", e);
            return false;
        }
    }

    /**
     * 删除书籍。
     *
     * @param handler Netty处理器。
     * @param uuid 要删除书籍的UUID。
     * @return 如果删除成功则返回true，否则返回false。
     */
    public static boolean deleteBook(NettyHandler handler, UUID uuid) {
        Request request = new Request();
        request.setUri("library/deleteBook");
        request.setParams(Map.of(
                "uuid", uuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("删除书籍失败", e);
            return false;
        }
    }

    /**
     * 借阅书籍。
     *
     * @param handler Netty处理器。
     * @param bookUuid 要借阅书籍的UUID。
     * @param cardNumber 借阅者的卡号。
     * @return 如果借阅成功则返回true，否则返回false。
     */
    public static boolean borrowBook(NettyHandler handler, String bookUuid, String cardNumber) {
        Request request = new Request();
        request.setUri("library/borrowBook");
        request.setParams(Map.of(
                "bookUuid", bookUuid,
                "cardNumber", cardNumber
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("借阅书籍失败", e);
            return false;
        }
    }

    /**
     * 获取当前用户的借阅记录。
     *
     * @param handler Netty处理器。
     * @return 当前用户的借阅记录列表，如果获取失败则返回null。
     */
    public static List<LibraryTransaction> getMyRecords(NettyHandler handler) {
        Request request = new Request();
        request.setUri("library/user/records");

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                List<String> raw_data = (List<String>) response.getData();
                return raw_data.stream().map(json -> IEntity.fromJson(json, LibraryTransaction.class)).toList();
            } else {
                throw new RuntimeException("获取书籍信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取书籍信息失败", e);
            return null;
        }
    }

    /**
     * 管理员获取指定用户的借阅记录。
     *
     * @param handler Netty处理器。
     * @param cardNumber 用户的卡号。
     * @return 指定用户的借阅记录列表，如果获取失败则返回null。
     */
    public static List<LibraryTransaction> staffGetRecords(NettyHandler handler, String cardNumber) {
        Request request = new Request();
        request.setUri("library/staff/records");
        request.setParams(Map.of(
                "cardNumber", cardNumber
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                List<String> raw_data = (List<String>) response.getData();
                return raw_data.stream().map(json -> IEntity.fromJson(json, LibraryTransaction.class)).toList();
            } else {
                throw new RuntimeException("获取书籍信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取书籍信息失败", e);
            return null;
        }
    }

    /**
     * 用户续借书籍。
     *
     * @param handler Netty处理器。
     * @param uuid 要续借书籍的UUID。
     * @return 如果续借成功则返回true，否则返回false。
     */
    public static Boolean userRenewBook(NettyHandler handler, UUID uuid) {
        Request request = new Request();
        request.setUri("library/user/renew");
        request.setParams(Map.of(
                "uuid", uuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("续借书籍失败", e);
            return false;
        }
    }

    /**
     * 管理员续借书籍。
     *
     * @param handler Netty处理器。
     * @param uuid 要续借书籍的UUID。
     * @return 如果续借成功则返回true，否则返回false。
     */
    public static Boolean staffRenewBook(NettyHandler handler, UUID uuid) {
        Request request = new Request();
        request.setUri("library/staff/renew");
        request.setParams(Map.of(
                "uuid", uuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("续借书籍失败", e);
            return false;
        }
    }

    /**
     * 归还书籍。
     *
     * @param handler Netty处理器。
     * @param uuid 要归还书籍的UUID。
     * @return 如果归还成功则返回true，否则返回false。
     */
    public static Boolean returnBook(NettyHandler handler, UUID uuid) {
        Request request = new Request();
        request.setUri("library/staff/return");
        request.setParams(Map.of(
                "uuid", uuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("归还书籍失败", e);
            return false;
        }
    }
}
