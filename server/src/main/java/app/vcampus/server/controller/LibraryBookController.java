package app.vcampus.server.controller;

import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.LibraryBook;
import app.vcampus.server.entity.LibraryTransaction;
import app.vcampus.server.entity.User;
import app.vcampus.server.enums.BookStatus;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Pair;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 图书馆图书控制器。
 * 处理与图书馆图书相关的操作，如图书增删改查、借阅、归还、续借等。
 */
@Slf4j
public class LibraryBookController {

    /**
     * 添加一本新书。
     *
     * @param request  包含图书信息的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/addBook")
    public Response addBook(Request request, org.hibernate.Session database) {
        LibraryBook newBook = IEntity.fromJson(request.getParams().get("book"), LibraryBook.class);
        if (newBook == null) {
            return Response.Common.badRequest();
        }

        newBook.setUuid(UUID.randomUUID());
        Transaction tx = database.beginTransaction();
        Database.updateWhere(LibraryBook.class, "isbn", newBook.getIsbn(), List.of(
                new Pair<>("name", newBook.getName()),
                new Pair<>("description", newBook.getDescription()),
                new Pair<>("author", newBook.getAuthor()),
                new Pair<>("press", newBook.getPress()),
                new Pair<>("cover", newBook.getCover())
        ), database);
        database.persist(newBook);
        tx.commit();

        return Response.Common.ok();
    }

    /**
     * 删除一本书（此功能当前未使用）。
     *
     * @param request  包含图书UUID的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/deleteBook", role = "library_staff")
    public Response deleteBook(Request request, org.hibernate.Session database) {
        String id = request.getParams().get("uuid");

        if (id == null) return Response.Common.error("Book UUID cannot be empty");

        UUID uuid = UUID.fromString(id);
        LibraryBook toDelete = database.get(LibraryBook.class, uuid);
        if (toDelete == null) return Response.Common.error("No such book");

        Transaction tx = database.beginTransaction();
        database.remove(toDelete);
        tx.commit();

        return Response.Common.ok();
    }

    /**
     * 借阅一本书。
     *
     * @param request  包含图书UUID和用户卡号的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/borrowBook")
    public Response borrowBook(Request request, org.hibernate.Session database) {
        try {
            String bookUuid = request.getParams().get("bookUuid");
            int cardNumber = Integer.parseInt(request.getParams().get("cardNumber"));
            if (bookUuid == null || cardNumber == 0)
                return Response.Common.error("Book UUID or user UUID cannot be empty");

            UUID uuid = UUID.fromString(bookUuid);
            LibraryBook toBorrow = database.get(LibraryBook.class, uuid);
            if (toBorrow == null) return Response.Common.error("No such book");
            if (toBorrow.getBookStatus() != BookStatus.available) return Response.Common.error("Book is not available");

            User user = database.get(User.class, cardNumber);
            if (user == null) return Response.Common.error("No such user");

            Transaction tx = database.beginTransaction();
            toBorrow.setBookStatus(BookStatus.lend);
            database.persist(toBorrow);

            LibraryTransaction newRecord = new LibraryTransaction();
            newRecord.setUuid(UUID.randomUUID());
            newRecord.setUserId(cardNumber);
            newRecord.setBookUuid(uuid);
            newRecord.setBorrowTime(new Date());
            newRecord.setDueTime(Date.from(newRecord.getBorrowTime().toInstant().plusSeconds(60 * 60 * 24 * 30)));
            database.persist(newRecord);
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error("Failed to borrow book");
        }
    }

    /**
     * 更新图书信息。
     *
     * @param request  包含更新后图书信息的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/updateBook")
    public Response updateBook(Request request, org.hibernate.Session database) {
        LibraryBook newBook = IEntity.fromJson(request.getParams().get("book"), LibraryBook.class);
        LibraryBook toUpdate = database.get(LibraryBook.class, newBook.getUuid());
        if (toUpdate == null) {
            return Response.Common.badRequest();
        }

        Transaction tx = database.beginTransaction();
        toUpdate.setName(newBook.getName());
        toUpdate.setDescription(newBook.getDescription());
        toUpdate.setPlace(newBook.getPlace());
        toUpdate.setCover(newBook.getCover());
        toUpdate.setPress(newBook.getPress());
        toUpdate.setAuthor(newBook.getAuthor());
        toUpdate.setCallNumber(newBook.getCallNumber());
        toUpdate.setBookStatus(newBook.getBookStatus());
        database.persist(toUpdate);
        tx.commit();

        return Response.Common.ok();
    }

    /**
     * 根据关键词搜索图书。
     *
     * @param request  包含搜索关键词的请求。
     * @param database 数据库会话。
     * @return 按ISBN分组的图书列表的响应。
     */
    @RouteMapping(uri = "library/searchBook")
    public Response searchBook(Request request, org.hibernate.Session database) {
        try {
            String keyword = request.getParams().get("keyword");
            if (keyword == null) return Response.Common.error("Keyword cannot be empty");
            List<LibraryBook> books = Database.likeQuery(LibraryBook.class, new String[]{"name", "isbn", "author", "description", "press", "callNumber"}, keyword, database);

            return Response.Common.ok(books.stream().collect(Collectors.groupingBy(w -> w.isbn)));
        } catch (Exception e) {
            return Response.Common.error("Failed to search books");
        }
    }

    /**
     * 获取所有图书信息。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含所有图书列表的响应。
     */
    @RouteMapping(uri = "library/all")
    public Response all(Request request, org.hibernate.Session database) {
        try {
            List<LibraryBook> books = database.createQuery("from LibraryBook", LibraryBook.class).list();
            return Response.Common.ok(books.stream().map(IEntity::toJson).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Failed to get all books", e);
            return Response.Common.error("Failed to get all books");
        }
    }

    /**
     * 为删除操作搜索图书。
     *
     * @param request  包含书名的请求。
     * @param database 数据库会话。
     * @return 包含匹配书名图书列表的响应。
     */
    @RouteMapping(uri = "library/searchForDeletion")
    public Response searchForDeletion(Request request, org.hibernate.Session database) {
        try {
            String bookName = request.getParams().get("bookName");
            if (bookName == null) return Response.Common.error("Book name cannot be empty");
            List<LibraryBook> books = Database.getWhereString(LibraryBook.class, "name", bookName, database);
            return Response.Common.ok(books.stream().map(IEntity::toJson).collect(Collectors.toList()));
        } catch (Exception e) {
            return Response.Common.error("Failed to search books for deletion");
        }
    }

    /**
     * 根据ISBN获取图书信息，如果本地不存在则从外部API获取。
     *
     * @param request  包含ISBN的请求。
     * @param database 数据库会话。
     * @return 包含图书信息的响应。
     */
    @RouteMapping(uri = "library/isbn", role = "library_staff")
    public Response isbn(Request request, org.hibernate.Session database) {
        String isbn = request.getParams().get("isbn");

        if (isbn == null) return Response.Common.error("ISBN cannot be empty");

        List<LibraryBook> searchedBook = Database.getWhereString(LibraryBook.class, "isbn", isbn, database);
        if (!searchedBook.isEmpty()) return Response.Common.ok(Map.of("book", searchedBook.get(0).toJson()));

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://47.99.80.202:6066/openApi/getInfoByIsbn?appKey=ae1718d4587744b0b79f940fbef69e77&isbn=" + isbn))
                    .GET()
                    .build();

            HttpResponse result = HttpClient.newHttpClient().send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            log.info(result.toString());
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> data = (new Gson()).fromJson(result.body().toString(), type);
            data = (Map<String, Object>) data.get("data");
            LibraryBook newBook = LibraryBook.fromWeb(data);
            return Response.Common.ok(Map.of("book", newBook.toJson()));
        } catch (Exception e) {
            log.warn("Fail to get book info", e);
            return Response.Common.error("Failed to get book info");
        }
    }

    /**
     * 获取用户的借阅记录。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含用户借阅记录列表的响应。
     */
    @RouteMapping(uri = "library/user/records", role = "student")
    public Response userRecords(Request request, org.hibernate.Session database) {
        try {
            int cardNumber = request.getSession().getCardNum();

            List<LibraryTransaction> records = Database.getWhereString(LibraryTransaction.class, "userId", Integer.toString(cardNumber), database);
            records = records.stream().peek(w -> w.setBook(database.get(LibraryBook.class, w.getBookUuid()))).collect(Collectors.toList());
            records.sort((a, b) -> b.getBorrowTime().compareTo(a.getBorrowTime()));
            return Response.Common.ok(records.stream().map(LibraryTransaction::toJson).collect(Collectors.toList()));
        } catch (Exception e) {
            return Response.Common.error("Failed to get user records");
        }
    }

    /**
     * 用户续借图书。
     *
     * @param request  包含借阅记录UUID的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/user/renew", role = "library_user")
    public Response renew(Request request, org.hibernate.Session database) {
        try {
            String uuid = request.getParams().get("uuid");
            if (uuid == null) return Response.Common.error("UUID cannot be empty");

            UUID bookUuid = UUID.fromString(uuid);
            LibraryTransaction toRenew = database.get(LibraryTransaction.class, bookUuid);
            if (toRenew == null) return Response.Common.error("No such record");

            if (toRenew.getUserId() != request.getSession().getCardNum())
                return Response.Common.error("You cannot renew this book");

            LibraryBook book = database.get(LibraryBook.class, toRenew.getBookUuid());
            if (book == null) return Response.Common.error("No such book");

            if (toRenew.getReturnTime() != null) return Response.Common.error("Book has been returned");
            if (toRenew.getDueTime().before(new Date())) return Response.Common.error("Book is overdue");

            Transaction tx = database.beginTransaction();
            toRenew.setDueTime(Date.from(toRenew.getDueTime().toInstant().plusSeconds(60 * 60 * 24 * 30)));
            database.persist(toRenew);
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error("Failed to renew book");
        }
    }

    /**
     * 图书馆工作人员获取指定用户的借阅记录。
     *
     * @param request  包含用户卡号的请求。
     * @param database 数据库会话。
     * @return 包含用户借阅记录列表的响应。
     */
    @RouteMapping(uri = "library/staff/records")
    public Response staffRecords(Request request, org.hibernate.Session database) {
        try {
            int cardNumber = Integer.parseInt(request.getParams().get("cardNumber"));

            List<LibraryTransaction> records = Database.getWhereString(LibraryTransaction.class, "userId", Integer.toString(cardNumber), database);
            records = records.stream().peek(w -> w.setBook(database.get(LibraryBook.class, w.getBookUuid()))).collect(Collectors.toList());
            records.sort((a, b) -> b.getBorrowTime().compareTo(a.getBorrowTime()));
            return Response.Common.ok(records.stream().map(LibraryTransaction::toJson).collect(Collectors.toList()));
        } catch (Exception e) {
            return Response.Common.error("Failed to get staff records");
        }
    }

    /**
     * 图书馆工作人员为用户续借图书。
     *
     * @param request  包含借阅记录UUID的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/staff/renew")
    public Response staffRenew(Request request, org.hibernate.Session database) {
        try {
            String uuid = request.getParams().get("uuid");
            if (uuid == null) return Response.Common.error("UUID cannot be empty");

            UUID bookUuid = UUID.fromString(uuid);
            LibraryTransaction toRenew = database.get(LibraryTransaction.class, bookUuid);
            if (toRenew == null) return Response.Common.error("No such record");

            LibraryBook book = database.get(LibraryBook.class, toRenew.getBookUuid());
            if (book == null) return Response.Common.error("No such book");

            if (toRenew.getReturnTime() != null) return Response.Common.error("Book has been returned");

            Transaction tx = database.beginTransaction();
            toRenew.setDueTime(Date.from(toRenew.getDueTime().toInstant().plusSeconds(60 * 60 * 24 * 30)));
            database.persist(toRenew);
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error("Failed to renew book");
        }
    }

    /**
     * 归还图书。
     *
     * @param request  包含借阅记录UUID的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "library/staff/return")
    public Response returnBook(Request request, org.hibernate.Session database) {
        try {
            String uuid = request.getParams().get("uuid");
            if (uuid == null) return Response.Common.error("UUID cannot be empty");

            UUID bookUuid = UUID.fromString(uuid);
            LibraryTransaction toReturn = database.get(LibraryTransaction.class, bookUuid);
            if (toReturn == null) return Response.Common.error("No such record");

            if (toReturn.getReturnTime() != null) return Response.Common.error("Book has been returned");

            LibraryBook book = database.get(LibraryBook.class, toReturn.getBookUuid());
            if (book == null) return Response.Common.error("No such book");

            Transaction tx = database.beginTransaction();
            toReturn.setReturnTime(new Date());
            book.setBookStatus(BookStatus.available);
            database.persist(toReturn);
            database.persist(book);
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error("Failed to return book");
        }
    }
}