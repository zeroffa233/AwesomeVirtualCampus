package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.Student;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 学生状态客户端，提供与学生信息管理交互的功能，包括搜索、更新学生信息和获取当前学生信息。
 */
@Slf4j
public class StudentStatusClient extends BaseClient {
    /**
     * 用于搜索学生信息。
     *
     * @param handler Netty处理器。
     * @param keyword 搜索关键词。
     * @return 匹配学生的列表，如果失败则返回null。
     */
    public static List<Student> searchInfo(NettyHandler handler,
                                           String keyword) {
        Request request = new Request();
        request.setUri("student/filter");
        request.setParams(Map.of(
                "keyword", keyword
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                List<String> raw_data = ((Map<String, List<String>>) response.getData()).get("students");
                return raw_data.stream().map(json -> IEntity.fromJson(json, Student.class)).toList();
            } else {
                throw new RuntimeException("搜索学生信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("搜索学生信息失败", e);
            return null;
        }
    }

    /**
     * 用于更新现有学生的信息。
     *
     * @param handler Netty处理器。
     * @param student 要更新的学生信息。
     * @return 如果更新成功则返回true，否则返回false。
     */
    public static boolean updateInfo(NettyHandler handler, Student student) {
        Request request = new Request();
        request.setUri("student/updateInfo");
        request.setParams(Map.of(
                "student", student.toJson()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                return true;
            } else {
                throw new RuntimeException("更新学生信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("更新学生信息失败", e);
            return false;
        }
    }

    /**
     * 用于获取当前登录学生的信息。
     *
     * @param handler Netty处理器。
     * @return 包含学生信息的Student对象，如果获取失败则返回null。
     */
    public static Student getSelf(NettyHandler handler) {
        Request request = new Request();
        request.setUri("student/getSelf");

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                String data = ((Map<String, String>) response.getData()).get("student");
                return IEntity.fromJson(data, Student.class);
            } else {
                throw new RuntimeException("获取当前学生信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取当前学生信息失败", e);
            return null;
        }
    }

}

