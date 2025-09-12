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
 * TeachingAffairsViewModel（包含 MyClasses 内部类）
 */
public class TeachingAffairsViewModel {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    //public final List<String> identity = new ArrayList<>(FakeRepository.user.roles);
    public final MyClasses myClasses = new MyClasses(executor);

    // 你可以按需启用/添加教师部分实例
    // public final MyTeachingClasses myTeachingClasses = new MyTeachingClasses(executor);

    // ---------------- 学生部分 ----------------
    public class MyClasses {
        private final ExecutorService executor;

        // ObservableList，UI 可以直接监听
        public final ObservableList<TeachingClass> selected = FXCollections.observableArrayList();
        public final ObservableList<Course> allCourses = FXCollections.observableArrayList();

        private boolean inited = false;

        public MyClasses(ExecutorService executor) {
            this.executor = executor;
        }

        public void init() {
            if (inited) return;
            inited = true;
            getSelectedClasses();
            getSelectableCourses();
        }

        public void getSelectedClasses() {
            executor.submit(() -> {
                try {
                    List<TeachingClass> result = FakeRepository.getSelectedClasses();
                    Platform.runLater(() -> {
                        selected.setAll(result); // 更新 ObservableList
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

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

        public CompletableFuture<Boolean> chooseClass(UUID teachingClassUuid) {
//            return CompletableFuture.supplyAsync(() -> {
//                try {
//                    boolean ok = FakeRepository.chooseClass(teachingClassUuid);
//                    // 刷新可选与已选（在后台线程触发，结果会在 Platform.runLater 中应用）
//                    getSelectableCourses();
//                    getSelectedClasses();
//                    return ok;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }, executor);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    System.out.println("[DEBUG] chooseClass called for: " + teachingClassUuid);
                    System.out.println("[DEBUG] FakeRepository.user = " + FakeRepository.user);
                    System.out.println("[DEBUG] FakeRepository.getSelf() = " + FakeRepository.getSelf());
                    boolean ok = FakeRepository.chooseClass(teachingClassUuid);
                    System.out.println("[DEBUG] FakeRepository.chooseClass returned: " + ok);

                    // 刷新可选与已选
                    getSelectableCourses();
                    getSelectedClasses();
                    return ok;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, executor);
        }

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

    // ---------------- 教师部分（注释，按需启用） ----------------
//    public class MyTeachingClasses {
//        private final ExecutorService executor;
//        public final List<TeachingClass> myClasses = new CopyOnWriteArrayList<>();
//
//        private boolean inited = false;
//
//        public MyTeachingClasses(ExecutorService executor) {
//            this.executor = executor;
//        }
//
//        public void init() {
//            if (inited) return;
//            inited = true;
//            getMyTeachingClasses();
//        }
//
//        public void getMyTeachingClasses() {
//            executor.submit(() -> {
//                try {
//                    List<TeachingClass> result = FakeRepository.getMyTeachingClasses();
//                    myClasses.clear();
//                    myClasses.addAll(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        public void saveStudentList(TeachingClass teachingClass, File file) {
//            executor.submit(() -> {
//                try {
//                    String encoded = FakeRepository.exportStudentList(teachingClass);
//                    Files.write(file.toPath(), Base64.getDecoder().decode(encoded));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        public void gradeTemplate(TeachingClass teachingClass, File file) {
//            executor.submit(() -> {
//                try {
//                    String encoded = FakeRepository.exportGradeTemplate(teachingClass);
//                    Files.write(file.toPath(), Base64.getDecoder().decode(encoded));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        public void importGrade(TeachingClass teachingClass, File file) {
//            executor.submit(() -> {
//                try {
//                    String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
//                    FakeRepository.importGrade(teachingClass, encodedFile);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }

    /**
     * 可选：在程序退出时调用，停止线程池，防止进程无法结束
     */
    public void shutdown() {
        executor.shutdown();
    }
}
