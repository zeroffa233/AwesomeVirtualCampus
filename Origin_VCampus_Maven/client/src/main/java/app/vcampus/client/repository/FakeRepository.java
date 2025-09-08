package app.vcampus.client.repository;

import app.vcampus.client.gateway.AuthClient;
import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.User;


import app.vcampus.server.entity.Student;
import app.vcampus.server.enums.Gender;
import app.vcampus.server.enums.StudentStatus;
import app.vcampus.server.enums.PoliticalStatus;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
public class FakeRepository {
    public static NettyHandler handler;
    public static boolean isConnected = false;
    public static User user;

    public static boolean login(String username, String password) {
        User result = AuthClient.login(handler, username, password);
        if (result != null) {
            user = result;
            return true;
        }
        return false;
    }

    public static void disconnect() {
        if (handler != null) {
            handler.disconnect();
        }
        isConnected = false;
        handler = null;
        user = null;
    }
      // 用 Map 模拟学籍数据库，key = 学号(studentNumber)，value = Student 对象
    private static final Map<String, Student> fakeStudentDb = new HashMap<>();
    static {

        // 初始化一些假学生数据
        Student s1 = new Student();
        s1.setCardNumber(123456);
        s1.setStudentNumber("20250001");
        s1.setFamilyName("王");
        s1.setGivenName("小明");
        s1.setGender(Gender.male);
        s1.setBirthDate(Date.valueOf("2003-05-12"));
        s1.setMajor("计算机科学与技术");
        s1.setSchool("计算机科学与工程学院");
        s1.setStatus(StudentStatus.inSchool);
        s1.setBirthPlace("江苏");
        s1.setPoliticalStatus(PoliticalStatus.Masses);

        Student s2 = new Student();
        s2.setCardNumber(100002);
        s2.setStudentNumber("20250002");
        s2.setFamilyName("李");
        s2.setGivenName("雷");
        s2.setGender(Gender.male);
        s2.setBirthDate(Date.valueOf("2002-11-03"));
        s2.setMajor("软件工程");
        s2.setSchool("计算机科学与工程学院");
        s2.setStatus(StudentStatus.graduated);
        s2.setBirthPlace("上海");
        s2.setPoliticalStatus(PoliticalStatus.Masses);

        fakeStudentDb.put(s1.getStudentNumber(), s1);
        fakeStudentDb.put(s2.getStudentNumber(), s2);
    }
    /** 获取所有学生（返回底层 Map 引用 — 只读/调试用） */
    public static Map<String, Student> getAllStudents() {
        return fakeStudentDb;
    }

    /** 根据学号查询学生 */
    public static Student getStudentByNumber(String studentNumber) {
        return fakeStudentDb.get(studentNumber);
    }

    /** 添加学生（覆盖同学号） */
    public static void addStudent(Student s) {
        if (s == null || s.getStudentNumber() == null) return;
        synchronized (fakeStudentDb) {
            fakeStudentDb.put(s.getStudentNumber(), s);
        }
    }

    /** 删除学生 */
    public static void deleteStudent(String studentNumber) {
        if (studentNumber == null) return;
        synchronized (fakeStudentDb) {
            fakeStudentDb.remove(studentNumber);
        }
    }

    /**
     * 返回当前登录用户对应的 Student 对象（若找不到返回 null）
     *
     * 匹配策略（按优先级）：
     *  1. student.cardNumber == user.cardNum
     *  2. student.studentNumber == String.valueOf(user.cardNum)
     *  3. 若 user 为 null 或没有匹配则返回 null
     */
    public static Student getSelf() {
        if (user == null) return null;

        Integer userCardNum = null;
        try {
            // 尝试通过反射/getter 名称来兼容不同 User 实现，优先使用 getCardNum()
            userCardNum = user.getCardNum();
        } catch (Throwable ignored) {
            // 如果没有 getCardNum()，忽略
        }

        // 遍历查找
        for (Student s : fakeStudentDb.values()) {
            try {
                // 优先按卡号（Integer）匹配
                if (userCardNum != null && s.getCardNumber() != null && Objects.equals(s.getCardNumber(), userCardNum)) {
                    return s;
                }
                // 再尝试按学号字符串匹配（有些场景 user.cardNum 存学号或学号在 username）
                if (userCardNum != null && s.getStudentNumber() != null
                        && s.getStudentNumber().equals(String.valueOf(userCardNum))) {
                    return s;
                }
            } catch (Throwable ignored) {
            }
        }


        return null;
    }

    /**
     * 模糊搜索学生（按 姓 / 名 / 学号 / 专业 / 学院 / 籍贯 做 contains，不区分大小写）。
     * 返回 List<Student>（顺序不可保证）。
     */
    public static List<Student> searchStudent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 返回所有学生的快照
            synchronized (fakeStudentDb) {
                return new ArrayList<>(fakeStudentDb.values());
            }
        }

        final String kw = keyword.trim().toLowerCase(Locale.ROOT);
        List<Student> result = new ArrayList<>();

        synchronized (fakeStudentDb) {
            for (Student s : fakeStudentDb.values()) {
                if (s == null) continue;
                if (containsIgnoreCase(s.getFamilyName(), kw)
                        || containsIgnoreCase(s.getGivenName(), kw)
                        || containsIgnoreCase(s.getStudentNumber(), kw)
                        || containsIgnoreCase(s.getMajor(), kw)
                        || containsIgnoreCase(s.getSchool(), kw)
                        || containsIgnoreCase(s.getBirthPlace(), kw)) {
                    result.add(s);
                }
            }
        }

        return result;
    }

    private static boolean containsIgnoreCase(String field, String kw) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(kw);
    }

    /**
     * 更新学生信息：按 student.studentNumber 作为 key 进行替换 / 覆盖。
     * 如果 student == null 或 studentNumber == null 返回 false。
     * 成功则返回 true。
     */
    public static boolean updateStudent(Student student) {
        if (student == null || student.getStudentNumber() == null) return false;
        synchronized (fakeStudentDb) {
            fakeStudentDb.put(student.getStudentNumber(), student);
        }
        return true;
    }

    // 方便调试：清空数据库（可选）
    public static void clearAll() {
        synchronized (fakeStudentDb) {
            fakeStudentDb.clear();
        }
    }

    // （可选）按卡号查找
    public static Student getStudentByCardNumber(Integer cardNumber) {
        if (cardNumber == null) return null;
        synchronized (fakeStudentDb) {
            for (Student s : fakeStudentDb.values()) {
                if (Objects.equals(s.getCardNumber(), cardNumber)) return s;
            }
        }
        return null;
    }
}


