package app.vcampus.server.entity;

import com.google.gson.Gson;

/**
 * 实体接口。
 * 所有实体类都应实现此接口，以提供 JSON 序列化和反序列化的通用方法。
 */
public interface IEntity {
    /**
     * 用于 JSON 操作的 Gson 实例。
     */
    Gson gson = new Gson();

    /**
     * 从 JSON 字符串反序列化为实体对象。
     *
     * @param json  JSON 字符串。
     * @param clazz 目标实体类的 Class 对象。
     * @param <T>   实体类型。
     * @return 反序列化后的实体对象。
     */
    static <T extends IEntity> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 将当前实体对象序列化为 JSON 字符串。
     *
     * @return JSON 字符串。
     */
    default String toJson() {
        return gson.toJson(this);
    }
}