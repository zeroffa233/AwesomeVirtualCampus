package app.vcampus.server.utility;

import app.vcampus.server.entity.*;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据库工具类。
 * 提供数据库初始化和常用的数据查询、更新操作。
 */
public class Database {
    /**
     * 初始化数据库 SessionFactory。
     *
     * @param DB_USERNAME 数据库用户名。
     * @param DB_PASSWORD 数据库密码。
     * @return 配置好的 SessionFactory 实例。
     */
    public static SessionFactory init(String DB_USERNAME , String DB_PASSWORD) {
        Configuration configuration = new Configuration().configure();
        configuration.setProperty("hibernate.connection.username", DB_USERNAME);
        configuration.setProperty("hibernate.connection.password", DB_PASSWORD);
        return configuration
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Course.class)
                .addAnnotatedClass(LibraryBook.class)
                .addAnnotatedClass(TeachingClass.class)
                .addAnnotatedClass(StoreItem.class)
                .addAnnotatedClass(StoreTransaction.class)
                .addAnnotatedClass(SelectRecord.class)
                .addAnnotatedClass(FinanceCard.class)
                .addAnnotatedClass(CardTransaction.class)
                .addAnnotatedClass(TeachingEvaluation.class)
                .addAnnotatedClass(LibraryTransaction.class)
                .addAnnotatedClass(CachedImage.class)
                .addAnnotatedClass(UserTransactionHistory.class)
                .buildSessionFactory();
    }

    /**
     * 从数据库加载指定类型的所有数据。
     *
     * @param type    要加载的数据的实体类。
     * @param session 数据库会话。
     * @param <T>     数据的类型。
     * @return 包含所有数据的列表。
     */
    public static <T> List<T> loadAllData(Class<T> type, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        criteria.from(type);
        return session.createQuery(criteria).getResultList();
    }

    /**
     * 根据模糊查询条件从数据库加载数据。
     *
     * @param type    要查询的数据的实体类。
     * @param field   要查询的字段数组。
     * @param value   要匹配的值。
     * @param session 数据库会话。
     * @param <T>     数据的类型。
     * @return 满足模糊查询条件的数据列表。
     */
    public static <T> List<T> likeQuery(Class<T> type, String[] field, String value, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        Root<T> itemRoot = criteria.from(type);
        ArrayList<Predicate> conditions = new ArrayList<>();
        for (String s : field) {
            conditions.add(builder.like(itemRoot.get(s).as(String.class), "%" + value + "%"));
        }
        criteria.where(builder.or(conditions.toArray(new Predicate[0])));
        return session.createQuery(criteria).getResultList();
    }

    /**
     * 根据字符串类型的 where 条件从数据库加载数据。
     *
     * @param type    要查询的数据的实体类。
     * @param field   要查询的字段。
     * @param value   要匹配的值。
     * @param session 数据库会话。
     * @param <T>     数据的类型。
     * @return 满足条件的数据列表。
     */
    public static <T> List<T> getWhereString(Class<T> type, String field, String value, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        Root<T> itemRoot = criteria.from(type);
        criteria.where(builder.equal(itemRoot.get(field).as(String.class), value));
        return session.createQuery(criteria).getResultList();
    }

    /**
     * 根据 UUID 类型的 where 条件从数据库加载数据。
     *
     * @param type    要查询的数据的实体类。
     * @param field   要查询的字段。
     * @param value   要匹配的 UUID 值。
     * @param session 数据库会话。
     * @param <T>     数据的类型。
     * @return 满足条件的数据列表。
     */
    public static <T> List<T> getWhereUuid(Class<T> type, String field, UUID value, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        Root<T> itemRoot = criteria.from(type);
        criteria.where(builder.equal(itemRoot.get(field), value));
        return session.createQuery(criteria).getResultList();
    }

    /**
     * 根据字符串类型的 where 条件更新数据库中的数据。
     *
     * @param type    要更新的数据的实体类。
     * @param field   用于 where 条件的字段。
     * @param value   用于 where 条件的值。
     * @param updates 要执行的更新操作列表，每个 Pair 包含字段名和新值。
     * @param session 数据库会话。
     * @param <T>     数据的类型。
     */
    public static <T> void updateWhere(Class<T> type, String field, String value, List<Pair<String, String>> updates, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaUpdate<T> criteria = builder.createCriteriaUpdate(type);
        Root<T> itemRoot = criteria.from(type);

        criteria.where(builder.equal(itemRoot.get(field).as(String.class), value));
        updates.forEach(pair -> criteria.set(pair.getFirst(), pair.getSecond()));

        session.createMutationQuery(criteria).executeUpdate();
    }
}