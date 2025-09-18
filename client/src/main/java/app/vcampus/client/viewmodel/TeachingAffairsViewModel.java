package app.vcampus.client.viewmodel;

import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.Pair;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.Base64;

/**
 * 教务视图模型。
 * 负责处理学生、教师和管理员的教务相关逻辑。
 */
public class TeachingAffairsViewModel {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 学生教务视图模型。
     */
    public final MyClasses myClasses = new MyClasses(executor);

    /**
     * 教师教务视图模型。
     */
    public final MyTeachingClasses myTeachingClasses = new MyTeachingClasses(executor);

    /**
     * 管理员教务工具视图模型。
     */
    public final AdminTools adminTools = new AdminTools(executor);

    /**
     * 学生课程视图模型内部类。
     * 管理学生的选课、退课以及课程列表的获取。
     */
    public class MyClasses {
        private final ExecutorService executor;

        /**
         * 已选课程的可观察列表。
         */
        public final ObservableList<TeachingClass> selected = FXCollections.observableArrayList();
        /**
         * 所有可选课程的可观察列表。
         */
        public final ObservableList<Course> allCourses = FXCollections.observableArrayList();

        private boolean inited = false;

        /**
         * 构造函数。
         *
         * @param executor 线程池执行器。
         */
        public MyClasses(ExecutorService executor) {
            this.executor = executor;
        }

        /**
         * 初始化，获取已选课程和可选课程。
         */
        public void init() {
            if (inited) return;
            inited = true;
            getSelectedClasses();
            getSelectableCourses();
        }

        /**
         * 异步获取学生已选课程列表。
         */
        public void getSelectedClasses() {
            executor.submit(() -> {
                try {
                    List<TeachingClass> result = FakeRepository.getSelectedClasses();
                    Platform.runLater(() -> {
                        selected.setAll(result);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        /**
         * 异步获取所有可选课程列表。
         */
        public void getSelectableCourses() {
            executor.submit(() -> {
                try {
                    List<Course> result = FakeRepository.getSelectableCourses();
                    Platform.runLater(() -> allCourses.setAll(result));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        /**
         * 异步选择一门课程。
         *
         * @param teachingClassUuid 教学班的UUID。
         * @return 一个包含操作是否成功的 CompletableFuture。
         */
        public CompletableFuture<Boolean> chooseClass(UUID teachingClassUuid) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    boolean ok = FakeRepository.chooseClass(teachingClassUuid);
                    getSelectableCourses();
                    getSelectedClasses();
                    return ok;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, executor);
        }

        /**
         * 异步退选一门课程。
         *
         * @param teachingClassUuid 教学班的UUID。
         * @return 一个包含操作是否成功的 CompletableFuture。
         */
        public CompletableFuture<Boolean> dropClass(UUID teachingClassUuid) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    boolean ok = FakeRepository.dropClass(teachingClassUuid);
                    getSelectableCourses();
                    getSelectedClasses();
                    return ok;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, executor);
        }
    }

    /**
     * 教师课程视图模型内部类。
     * 管理教师的授课列表以及相关操作，如导出学生名单、成绩模板和导入成绩。
     */
    public class MyTeachingClasses {
        private final ExecutorService executor;
        /**
         * 教师授课列表的可观察列表。
         */
        public final ObservableList<TeachingClass> myClasses = FXCollections.observableArrayList();

        private boolean inited = false;

        /**
         * 构造函数。
         *
         * @param executor 线程池执行器。
         */
        public MyTeachingClasses(ExecutorService executor) {
            this.executor = executor;
        }

        /**
         * 初始化，获取教师的授课列表。
         */
        public void init() {
            if (inited) return;
            inited = true;
            getMyTeachingClasses();
        }

        /**
         * 异步获取教师的授课列表。
         */
        public void getMyTeachingClasses() {
            executor.submit(() -> {
                try {
                    List<TeachingClass> result = FakeRepository.getMyTeachingClasses();
                    Platform.runLater(() -> {
                        myClasses.setAll(result);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        /**
         * 异步导出学生名单到指定文件。
         *
         * @param teachingClass 教学班对象。
         * @param file          要保存的文件。
         */
        public void saveStudentList(TeachingClass teachingClass, File file) {
            executor.submit(() -> {
                try {
                    String encoded = FakeRepository.exportStudentList(teachingClass);
                    Files.write(file.toPath(), Base64.getDecoder().decode(encoded));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        /**
         * 异步导出成绩模板到指定文件。
         *
         * @param teachingClass 教学班对象。
         * @param file          要保存的文件。
         */
        public void gradeTemplate(TeachingClass teachingClass, File file) {
            executor.submit(() -> {
                try {
                    String encoded = FakeRepository.exportGradeTemplate(teachingClass);
                    Files.write(file.toPath(), Base64.getDecoder().decode(encoded));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        /**
         * 异步从文件导入成绩。
         *
         * @param teachingClass 教学班对象。
         * @param file          包含成绩信息的Excel文件。
         */
        public void importGrade(TeachingClass teachingClass, File file) {
            executor.submit(() -> {
                try {
                    String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                    FakeRepository.importGrade(teachingClass, encodedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 管理员工具视图模型内部类。
     * 提供管理员添加课程和教学班的功能。
     */
    public class AdminTools {
        private final ExecutorService executor;

        /**
         * 构造函数。
         *
         * @param executor 线程池执行器。
         */
        public AdminTools(ExecutorService executor) {
            this.executor = executor;
        }

        /**
         * 异步添加新课程。
         *
         * @param courseId   课程代码。
         * @param courseName 课程名称。
         * @param school     开课学院。
         * @param credit     学分。
         * @return 一个包含操作是否成功的 CompletableFuture。
         */
        public CompletableFuture<Boolean> addCourse(String courseId, String courseName, String school, float credit) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return FakeRepository.addCourse(courseId, courseName, school, credit);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, executor);
        }

        /**
         * 异步添加新教学班。
         *
         * @param courseUuid 课程UUID。
         * @param teacherId  教师工号。
         * @param place      上课地点。
         * @param capacity   容量。
         * @param schedule   课程表。
         * @return 一个包含操作是否成功的 CompletableFuture。
         */
        public CompletableFuture<Boolean> addTeachingClass(UUID courseUuid, int teacherId, String place, int capacity, List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> schedule) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return FakeRepository.addTeachingClass(courseUuid, teacherId, place, capacity, schedule);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, executor);
        }
    }

    /**
     * 关闭线程池。
     * 在程序退出时调用，以确保所有后台线程都能正常结束。
     */
    public void shutdown() {
        executor.shutdown();
    }
}