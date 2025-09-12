package app.vcampus.client.repository;

import app.vcampus.client.gateway.AuthClient;
import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.User;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.Pair;
import app.vcampus.server.entity.Student;

import app.vcampus.server.entity.Student;
import app.vcampus.server.enums.Gender;
import app.vcampus.server.enums.StudentStatus;
import app.vcampus.server.enums.PoliticalStatus;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
public class FakeRepository {
    public static NettyHandler handler;
    public static boolean isConnected = false;
    public static User user;
    public static app.vcampus.server.utility.Session session;

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
    // 所有课程与教学班的内存存储
    private static final Map<UUID, Course> courseMap = new LinkedHashMap<>();
    private static final Map<UUID, TeachingClass> classMap = new LinkedHashMap<>();

    // 每个班级的已选人数（仅在内存中维护）
    private static final Map<UUID, Integer> classSelectedCount = new ConcurrentHashMap<>();

    // 每个登录用户(cardNumber) 的选课集合（set of class UUID）
    private static final Map<Integer, Set<UUID>> userSelections = new ConcurrentHashMap<>();

    // 存储评教结果（classUuid -> list of (scores, comment)）
    private static final Map<UUID, List<Pair<List<Integer>, String>>> evaluations = new ConcurrentHashMap<>();

    static {
        initSampleData();
    }

    private static void initSampleData() {
        // 创建示例课程与教学班
        // Course 1: 数据结构
        Course c1 = new Course();
        c1.setUuid(UUID.randomUUID());
        c1.setCourseId("CS201");
        c1.setCourseName("数据结构");
        c1.setSchool("计算机学院");
        c1.setCredit(3.0f);

        TeachingClass tc1 = new TeachingClass();
        tc1.setUuid(UUID.randomUUID());
        tc1.setCourseUuid(c1.getUuid());
        tc1.setTeacherId(2001);
        tc1.setTeacherName("张老师");
        // schedule: [(1, (2, (1,2)))] — 仅示例结构，请对照实体结构使用
        // schedule 的结构： Pair<Pair<startWeek,endWeek>, Pair<weekday, Pair<startTime,endTime>>>
        List<Pair<Pair<Integer,Integer>, Pair<Integer, Pair<Integer,Integer>>>> schedule1 = new ArrayList<>();
        schedule1.add(new Pair<>(new Pair<>(1,16), new Pair<>(2, new Pair<>(1,2)))); // 周二 第1-2节
        tc1.setSchedule(schedule1);
        tc1.setPlace("教学楼A101");
        tc1.setCapacity(60);
        tc1.setSelectedCount(0);

        TeachingClass tc2 = new TeachingClass();
        tc2.setUuid(UUID.randomUUID());
        tc2.setCourseUuid(c1.getUuid());
        tc2.setTeacherId(2002);
        tc2.setTeacherName("李老师");
        List<Pair<Pair<Integer,Integer>, Pair<Integer, Pair<Integer,Integer>>>> schedule2 = new ArrayList<>();
        schedule2.add(new Pair<>(new Pair<>(1,16), new Pair<>(2, new Pair<>(3,4)))); // 周三 第3-4节
        tc2.setSchedule(schedule2);
        tc2.setPlace("教学楼A102");
        tc2.setCapacity(50);
        tc2.setSelectedCount(0);

        c1.setTeachingClasses(Arrays.asList(tc1, tc2));

        // Course 2: 操作系统
        Course c2 = new Course();
        c2.setUuid(UUID.randomUUID());
        c2.setCourseId("CS301");
        c2.setCourseName("操作系统");
        c2.setSchool("计算机学院");
        c2.setCredit(4.0f);

        TeachingClass tc3 = new TeachingClass();
        tc3.setUuid(UUID.randomUUID());
        tc3.setCourseUuid(c2.getUuid());
        tc3.setTeacherId(2003);
        tc3.setTeacherName("王老师");
        List<Pair<Pair<Integer,Integer>, Pair<Integer, Pair<Integer,Integer>>>> schedule3 = new ArrayList<>();
        schedule3.add(new Pair<>(new Pair<>(1,16), new Pair<>(2, new Pair<>(3,4)))); // 周二 第3-4节
        tc3.setSchedule(schedule3);
        tc3.setPlace("教学楼B201");
        tc3.setCapacity(40);
        tc3.setSelectedCount(0);

        TeachingClass tc4 = new TeachingClass();
        tc4.setUuid(UUID.randomUUID());
        tc4.setCourseUuid(c2.getUuid());
        tc4.setTeacherId(2004);
        tc4.setTeacherName("赵老师");
        List<Pair<Pair<Integer,Integer>, Pair<Integer, Pair<Integer,Integer>>>> schedule4 = new ArrayList<>();
        schedule4.add(new Pair<>(new Pair<>(1,16), new Pair<>(5, new Pair<>(1,2)))); // 周五 第1-2节
        tc4.setSchedule(schedule4);
        tc4.setPlace("教学楼B202");
        tc4.setCapacity(45);
        tc4.setSelectedCount(0);

        c2.setTeachingClasses(Arrays.asList(tc3, tc4));

        // put into maps
        courseMap.put(c1.getUuid(), c1);
        courseMap.put(c2.getUuid(), c2);

        for (TeachingClass tc : Arrays.asList(tc1, tc2, tc3, tc4)) {
            classMap.put(tc.getUuid(), tc);
            classSelectedCount.put(tc.getUuid(), 0);
        }

        // optional: 给某个用户预置一个已选（便于调试）
        // if FakeRepository.getSelf() exists at startup it may be null; so skip preselect here
    }

    // --- API 方法，供前端 ViewModel 调用 ---

    /**
     * 获取当前用户已选的教学班（返回 TeachingClass 列表）
     */
    public static List<TeachingClass> getSelectedClasses() {
        Student self = FakeRepository.getSelf();
        if (self == null) return Collections.emptyList();
        Integer card = self.getCardNumber();
        Set<UUID> set = userSelections.getOrDefault(card, Collections.emptySet());

        List<TeachingClass> result = new ArrayList<>();
        for (UUID uuid : set) {
            TeachingClass tc = classMap.get(uuid);
            if (tc != null) {
                // 复制一个实例以避免副作用（简单浅拷贝）
                TeachingClass copy = shallowCopyTeachingClass(tc);
                copy.setSelectedCount(classSelectedCount.getOrDefault(uuid, 0));
                // note: selectRecord 留空（视前端逻辑而定）
                result.add(copy);
            }
        }
        return result;
    }

    /**
     * 获取所有课程并附上教学班信息（Course.teachingClasses 已填）
     */
    public static List<Course> getSelectableCourses() {
        // 返回 courses 的深拷贝集合（避免外部修改原始数据）
        List<Course> list = new ArrayList<>();
        for (Course c : courseMap.values()) {
            Course copyCourse = new Course();
            copyCourse.setUuid(c.getUuid());
            copyCourse.setCourseId(c.getCourseId());
            copyCourse.setCourseName(c.getCourseName());
            copyCourse.setSchool(c.getSchool());
            copyCourse.setCredit(c.getCredit());

            List<TeachingClass> tcCopies = new ArrayList<>();
            for (TeachingClass tc : c.getTeachingClasses()) {
                TeachingClass copy = shallowCopyTeachingClass(tc);
                copy.setSelectedCount(classSelectedCount.getOrDefault(tc.getUuid(), 0));
                // 如果当前用户已选，则可以在 copy 上做标记（前端会通过 myClasses.selected 判断已选）
                tcCopies.add(copy);
            }
            copyCourse.setTeachingClasses(tcCopies);
            list.add(copyCourse);
        }
        return list;
    }

    /**
     * 学生选择某教学班（返回 true 表示成功）
     */
    public static boolean chooseClass(UUID teachingClassUuid) {
        Student self = FakeRepository.getSelf();
        if (self == null) return false;
        Integer card = self.getCardNumber();
        TeachingClass tc = classMap.get(teachingClassUuid);
        if (tc == null) return false;

        synchronized (userSelections) {
            Set<UUID> set = userSelections.computeIfAbsent(card, k -> Collections.synchronizedSet(new LinkedHashSet<>()));
            if (set.contains(teachingClassUuid)) return true; // 已选则视为成功（幂等）
            // 超额检查
            int cur = classSelectedCount.getOrDefault(teachingClassUuid, 0);
            if (cur >= tc.getCapacity()) return false; // 满员
            set.add(teachingClassUuid);
            classSelectedCount.put(teachingClassUuid, cur + 1);
        }
        return true;
    }

    /**
     * 学生退选某教学班（返回 true 表示成功）
     */
    public static boolean dropClass(UUID teachingClassUuid) {
        Student self = FakeRepository.getSelf();
        if (self == null) return false;
        Integer card = self.getCardNumber();
        if (!userSelections.containsKey(card)) return false;

        synchronized (userSelections) {
            Set<UUID> set = userSelections.get(card);
            if (set == null || !set.remove(teachingClassUuid)) return false;
            classSelectedCount.put(teachingClassUuid, Math.max(0, classSelectedCount.getOrDefault(teachingClassUuid, 1) - 1));
        }
        return true;
    }

    /**
     * 接收评教结果（简单保存到内存）
     * evaluationResult: Pair<classUuid, Pair<List<Integer>, String>>
     */
    public static boolean sendEvaluationResult(Pair<UUID, Pair<List<Integer>, String>> evaluationResult) {
        if (evaluationResult == null) return false;
        UUID classUuid = evaluationResult.getFirst();
        Pair<List<Integer>, String> pair = evaluationResult.getSecond();
        if (classUuid == null || pair == null) return false;

        evaluations.computeIfAbsent(classUuid, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new Pair<>(pair.getFirst(), pair.getSecond()));
        return true;
    }

    // --- 辅助方法 ---
    private static TeachingClass shallowCopyTeachingClass(TeachingClass src) {
        TeachingClass copy = new TeachingClass();
        copy.setUuid(src.getUuid());
        copy.setCourseUuid(src.getCourseUuid());
        copy.setCourse(src.getCourse());
        copy.setSelectRecord(src.getSelectRecord());
        copy.setTeacherId(src.getTeacherId());
        copy.setTeacherName(src.getTeacherName());
        copy.setSchedule(src.getSchedule());
        copy.setPlace(src.getPlace());
        copy.setCapacity(src.getCapacity());
        copy.setSelectedCount(src.getSelectedCount());
        copy.setIsEvaluated(src.getIsEvaluated());
        copy.setEvaluationResult(src.getEvaluationResult());
        return copy;
    }

    // 暴露内部状态（仅用于调试）
    public static Map<UUID, Integer> getClassSelectedCountSnapshot() {
        return new HashMap<>(classSelectedCount);
    }

    public static Map<Integer, Set<UUID>> getUserSelectionsSnapshot() {
        Map<Integer, Set<UUID>> snap = new HashMap<>();
        userSelections.forEach((k, v) -> snap.put(k, new LinkedHashSet<>(v)));
        return snap;
    }
}
