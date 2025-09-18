package app.vcampus.client.repository;

import app.vcampus.client.gateway.*;
import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.*;
import app.vcampus.server.utility.Pair;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.vcampus.client.util.ImageCache; // 【重要】导入 ImageCache

import java.awt.Window;
import java.util.*;

/**
 * Java 版 FakeRepository
 * 提供静态方法以兼容现有前端调用。
 */
public final class FakeRepository
{
    // Netty handler（需要外部注入/设置）
    public static NettyHandler handler;
    public static boolean isConnected = false;

    public static User user;
    public static app.vcampus.server.utility.Session session;
    private static final Logger logger = LoggerFactory.getLogger(FakeRepository.class);

    // ComposeWindow 在 Java 中不是标准类型，这里用 java.awt.Window 占位（按需替换）
    public static Window window;

    // 用于在非 JavaFX 线程中创建 WebView 的桥接
    public static final JFXPanel gptJfxPanel = new JFXPanel();

    private FakeRepository() {
        // 不可实例化
    }

    public static StoreItem copyStoreItem(StoreItem src) {
        return copyStoreItem(src, src == null ? 0 : src.getStock());
    }

    public static StoreItem copyStoreItem(StoreItem src, int stock) {
        if (src == null) return null;
        StoreItem copied = new StoreItem();
        // 假设这些 setter/getter 存在
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


    public static boolean login(String username, String password) {
        try {
            User u = AuthClient.login(handler, username, password);
            if (u != null) {
                logger.debug("login user: {}", u);
                System.out.println("[FakeRepository.login(): userCardNum]" + u.cardNum);
                user = u;
                isConnected = true;
                System.out.println("[login] : ImageCache.getInstance() called");
                ImageCache.getInstance();

                return true;
            }
        } catch (Exception e) {
            logger.error("login error", e);
        }
        return false;
    }
    public static void disconnect() {
        if (handler != null) handler.disconnect();
        isConnected = false;
        handler = null;
        user = null;
    }

    public static Student getSelf() {
        try {
            return StudentStatusClient.getSelf(handler);
        } catch (Exception e) {
            logger.error("getSelf error", e);
            return null;
        }
    }

    public static List<Student> searchStudent(String keyword) {
        try {
            List<Student> res = StudentStatusClient.searchInfo(handler, keyword);
            return res != null ? res : Collections.emptyList();
        } catch (Exception e) {
            logger.error("searchStudent error", e);
            return Collections.emptyList();
        }
    }

    public static boolean updateStudent(Student student) {
        try {
            return StudentStatusClient.updateInfo(handler, student);
        } catch (Exception e) {
            logger.error("updateStudent error", e);
            return false;
        }
    }



    // ------------------ Teaching affairs ------------------
    public static List<TeachingClass> getSelectedClasses() {
        try {
            List<TeachingClass> r = TeachingAffairsClient.getSelectedClasses(handler);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            logger.error("getSelectedClasses error", e);
            return Collections.emptyList();
        }
    }

    public static boolean sendEvaluationResult(Pair<UUID, Pair<List<Integer>, String>> result) {
        try {
            return TeachingAffairsClient.sendEvaluationResult(handler, result);
        } catch (Exception e) {
            logger.error("sendEvaluationResult error", e);
            return false;
        }
    }

    public static List<Course> getSelectableCourses() {
        try {
            List<Course> r = TeachingAffairsClient.getSelectableCourses(handler);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            logger.error("getSelectableCourses error", e);
            return Collections.emptyList();
        }
    }

    public static boolean chooseClass(UUID uuid) {
        try {
            return TeachingAffairsClient.chooseClass(handler, uuid);
        } catch (Exception e) {
            logger.error("chooseClass error", e);
            return false;
        }
    }

    public static boolean dropClass(UUID uuid) {
        try {
            return TeachingAffairsClient.dropClass(handler, uuid);
        } catch (Exception e) {
            logger.error("dropClass error", e);
            return false;
        }
    }

    public static List<TeachingClass> getMyTeachingClasses() {
        try {
            List<TeachingClass> r = TeachingAffairsClient.getMyTeachingClasses(handler);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            logger.error("getMyTeachingClasses error", e);
            return Collections.emptyList();
        }
    }

    public static String exportStudentList(TeachingClass tc) {
        try {
            return TeachingAffairsClient.exportStudentList(handler, tc.getUuid());
        } catch (Exception e) {
            logger.error("exportStudentList error", e);
            return "";
        }
    }

    public static String exportGradeTemplate(TeachingClass tc) {
        try {
            return TeachingAffairsClient.exportGradeTemplate(handler, tc.getUuid());
        } catch (Exception e) {
            logger.error("exportGradeTemplate error", e);
            return "";
        }
    }

    public static boolean importGrade(TeachingClass tc, String file) {
        try {
            return TeachingAffairsClient.importGrade(handler, tc.getUuid(), file);
        } catch (Exception e) {
            logger.error("importGrade error", e);
            return false;
        }
    }
    /**
     * 添加课程
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
     * 添加教学班
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
