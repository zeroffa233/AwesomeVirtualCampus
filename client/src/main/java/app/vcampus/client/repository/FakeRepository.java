package app.vcampus.client.repository;

import app.vcampus.client.gateway.*;
import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.*;
import app.vcampus.server.utility.Pair;
import app.vcampus.client.util.ImageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 伪仓库类。
 * <p>
 * 提供静态方法，作为UI/ViewModel层与网络网关客户端之间的桥梁。
 * 为应用程序的其余部分提供一个简化的、类似同步的接口来与服务器交互。
 * </p>
 */
public final class FakeRepository {
    /**
     * Netty 网络处理器。
     */
    public static NettyHandler handler;
    /**
     * 网络连接状态。
     */
    public static boolean isConnected = false;

    /**
     * 当前登录的用户信息。
     */
    public static User user;
    /**
     * 当前会话信息。
     */
    public static app.vcampus.server.utility.Session session;
    /**
     * 日志记录器。
     */
    private static final Logger logger = LoggerFactory.getLogger(FakeRepository.class);

    private FakeRepository() {
    }

    /**
     * 复制一个商店物品对象。
     *
     * @param src 要复制的源对象。
     * @return 复制后的新对象。
     */
    public static StoreItem copyStoreItem(StoreItem src) {
        return copyStoreItem(src, src == null ? 0 : src.getStock());
    }

    /**
     * 复制一个商店物品对象，并指定新的库存。
     *
     * @param src   要复制的源对象。
     * @param stock 新的库存量。
     * @return 复制后的新对象。
     */
    public static StoreItem copyStoreItem(StoreItem src, int stock) {
        if (src == null) return null;
        StoreItem copied = new StoreItem();
        copied.setStock(stock);
        copied.setUuid(src.getUuid());
        copied.setPictureLink(src.getPictureLink());
        copied.setItemName(src.getItemName());
        copied.setSalesVolume(src.getSalesVolume());
        copied.setPrice(src.getPrice());
        copied.setBarcode(src.getBarcode());
        copied.setDescription(src.getDescription());
        return copied;
    }

    /**
     * 用户登录。
     *
     * @param username 用户名（卡号）。
     * @param password 密码。
     * @return 登录成功返回 true，否则返回 false。
     */
    public static boolean login(String username, String password) {
        try {
            User u = AuthClient.login(handler, username, password);
            if (u != null) {
                logger.debug("login user: {}", u);
                user = u;
                isConnected = true;
                ImageCache.getInstance();
                return true;
            }
        } catch (Exception e) {
            logger.error("login error", e);
        }
        return false;
    }

    /**
     * 断开网络连接。
     */
    public static void disconnect() {
        if (handler != null) handler.disconnect();
        isConnected = false;
        handler = null;
        user = null;
    }

    /**
     * 获取当前用户的学籍信息。
     *
     * @return 学生对象，失败则返回 null。
     */
    public static Student getSelf() {
        try {
            return StudentStatusClient.getSelf(handler);
        } catch (Exception e) {
            logger.error("getSelf error", e);
            return null;
        }
    }

    /**
     * 根据关键词搜索学生。
     *
     * @param keyword 搜索关键词。
     * @return 学生列表，失败则返回空列表。
     */
    public static List<Student> searchStudent(String keyword) {
        try {
            List<Student> res = StudentStatusClient.searchInfo(handler, keyword);
            return res != null ? res : Collections.emptyList();
        } catch (Exception e) {
            logger.error("searchStudent error", e);
            return Collections.emptyList();
        }
    }

    /**
     * 更新学生信息。
     *
     * @param student 包含更新信息的学生对象。
     * @return 更新成功返回 true，否则返回 false。
     */
    public static boolean updateStudent(Student student) {
        try {
            return StudentStatusClient.updateInfo(handler, student);
        } catch (Exception e) {
            logger.error("updateStudent error", e);
            return false;
        }
    }

    /**
     * 获取已选课程列表。
     *
     * @return 教学班列表。
     */
    public static List<TeachingClass> getSelectedClasses() {
        try {
            List<TeachingClass> r = TeachingAffairsClient.getSelectedClasses(handler);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            logger.error("getSelectedClasses error", e);
            return Collections.emptyList();
        }
    }

    /**
     * 发送教学评估结果。
     *
     * @param result 评估结果。
     * @return 发送成功返回 true，否则返回 false。
     */
    public static boolean sendEvaluationResult(Pair<UUID, Pair<List<Integer>, String>> result) {
        try {
            return TeachingAffairsClient.sendEvaluationResult(handler, result);
        } catch (Exception e) {
            logger.error("sendEvaluationResult error", e);
            return false;
        }
    }

    /**
     * 获取可选课程列表。
     *
     * @return 课程列表。
     */
    public static List<Course> getSelectableCourses() {
        try {
            List<Course> r = TeachingAffairsClient.getSelectableCourses(handler);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            logger.error("getSelectableCourses error", e);
            return Collections.emptyList();
        }
    }

    /**
     * 选择课程。
     *
     * @param uuid 教学班UUID。
     * @return 操作成功返回 true，否则返回 false。
     */
    public static boolean chooseClass(UUID uuid) {
        try {
            return TeachingAffairsClient.chooseClass(handler, uuid);
        } catch (Exception e) {
            logger.error("chooseClass error", e);
            return false;
        }
    }

    /**
     * 退选课程。
     *
     * @param uuid 教学班UUID。
     * @return 操作成功返回 true，否则返回 false。
     */
    public static boolean dropClass(UUID uuid) {
        try {
            return TeachingAffairsClient.dropClass(handler, uuid);
        } catch (Exception e) {
            logger.error("dropClass error", e);
            return false;
        }
    }

    /**
     * 获取教师的授课列表。
     *
     * @return 教学班列表。
     */
    public static List<TeachingClass> getMyTeachingClasses() {
        try {
            List<TeachingClass> r = TeachingAffairsClient.getMyTeachingClasses(handler);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            logger.error("getMyTeachingClasses error", e);
            return Collections.emptyList();
        }
    }

    /**
     * 导出学生名单。
     *
     * @param tc 教学班对象。
     * @return Base64编码的Excel文件内容。
     */
    public static String exportStudentList(TeachingClass tc) {
        try {
            return TeachingAffairsClient.exportStudentList(handler, tc.getUuid());
        } catch (Exception e) {
            logger.error("exportStudentList error", e);
            return "";
        }
    }

    /**
     * 导出成绩模板。
     *
     * @param tc 教学班对象。
     * @return Base64编码的Excel文件内容。
     */
    public static String exportGradeTemplate(TeachingClass tc) {
        try {
            return TeachingAffairsClient.exportGradeTemplate(handler, tc.getUuid());
        } catch (Exception e) {
            logger.error("exportGradeTemplate error", e);
            return "";
        }
    }

    /**
     * 导入成绩。
     *
     * @param tc   教学班对象。
     * @param file Base64编码的Excel文件内容。
     * @return 操作成功返回 true，否则返回 false。
     */
    public static boolean importGrade(TeachingClass tc, String file) {
        try {
            return TeachingAffairsClient.importGrade(handler, tc.getUuid(), file);
        } catch (Exception e) {
            logger.error("importGrade error", e);
            return false;
        }
    }

    /**
     * 添加课程。
     *
     * @param courseId   课程ID。
     * @param courseName 课程名称。
     * @param school     开课学院。
     * @param credit     学分。
     * @return 操作成功返回 true，否则返回 false。
     */
    public static boolean addCourse(String courseId, String courseName, String school, float credit) {
        try {
            return TeachingAffairsClient.addCourse(handler, courseId, courseName, school, credit);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加教学班。
     *
     * @param courseUuid 课程UUID。
     * @param teacherId  教师ID。
     * @param place      上课地点。
     * @param capacity   容量。
     * @param schedule   课程安排。
     * @return 操作成功返回 true，否则返回 false。
     */
    public static boolean addTeachingClass(UUID courseUuid, int teacherId, String place, int capacity, List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> schedule) {
        try {
            return TeachingAffairsClient.addTeachingClass(handler, courseUuid, teacherId, place, capacity, schedule);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}