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

@Slf4j
public class LibraryClient extends BaseClient {
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
                throw new RuntimeException("Failed to get book info");
            }
        } catch (Exception e) {
            log.warn("Fail to get book info", e);
            return null;
        }
    }

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
            log.warn("Fail to add book", e);
            return false;
        }
    }

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
                throw new RuntimeException("Failed to get book info");
            }
        } catch (InterruptedException e) {
            log.warn("Fail to get book info", e);
            return null;
        }
    }

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
                throw new RuntimeException("Failed to search books for deletion");
            }
        } catch (InterruptedException e) {
            log.warn("Fail to search books for deletion", e);
            return null;
        }
    }

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
            log.warn("Fail to update book", e);
            return false;
        }
    }

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
            log.warn("Fail to delete book", e);
            return false;
        }
    }

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
            log.warn("Fail to borrow book", e);
            return false;
        }
    }

    public static List<LibraryTransaction> getMyRecords(NettyHandler handler) {
        Request request = new Request();
        request.setUri("library/user/records");

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                List<String> raw_data = (List<String>) response.getData();
                return raw_data.stream().map(json -> IEntity.fromJson(json, LibraryTransaction.class)).toList();
            } else {
                throw new RuntimeException("Failed to get book info");
            }
        } catch (InterruptedException e) {
            log.warn("Fail to get book info", e);
            return null;
        }
    }

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
                throw new RuntimeException("Failed to get book info");
            }
        } catch (InterruptedException e) {
            log.warn("Fail to get book info", e);
            return null;
        }
    }

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
            log.warn("Fail to renew book", e);
            return false;
        }
    }

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
            log.warn("Fail to renew book", e);
            return false;
        }
    }

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
            log.warn("Fail to return book", e);
            return false;
        }
    }
}
