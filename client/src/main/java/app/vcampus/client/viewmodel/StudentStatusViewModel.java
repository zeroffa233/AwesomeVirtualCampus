package app.vcampus.client.viewmodel;

import app.vcampus.server.entity.Student;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

/**
 * 学生学籍视图模型。
 * 负责处理学生学籍信息的获取、搜索和更新逻辑。
 */
public class StudentStatusViewModel {
    private final ObjectProperty<Student> currentStudent = new SimpleObjectProperty<>();
    private final ObservableList<Student> searchedStudents = FXCollections.observableArrayList();

    /**
     * 获取当前学生的属性。
     *
     * @return 当前学生的 ObjectProperty。
     */
    public ObjectProperty<Student> currentStudentProperty() {
        return currentStudent;
    }

    /**
     * 获取搜索到的学生列表。
     *
     * @return 搜索结果的 ObservableList。
     */
    public ObservableList<Student> getSearchedStudents() {
        return searchedStudents;
    }

    /**
     * 异步获取当前登录学生的学籍信息。
     */
    public void getStudentStatus() {
        Task<Student> t = new Task<>() {
            @Override
            protected Student call() throws Exception {
                Thread.sleep(200); // 模拟网络延迟
                return app.vcampus.client.repository.FakeRepository.getSelf();
            }
        };

        t.setOnSucceeded(evt -> currentStudent.set(t.getValue()));
        t.setOnFailed(evt -> t.getException().printStackTrace());
        new Thread(t, "getStudentStatus").start();
    }

    /**
     * 根据关键词异步搜索学生。
     *
     * @param keyword 搜索关键词。
     */
    public void searchStudent(String keyword) {
        Task<List<Student>> t = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                Thread.sleep(200); // 模拟网络延迟
                return app.vcampus.client.repository.FakeRepository.searchStudent(keyword);
            }
        };

        t.setOnSucceeded(evt -> {
            searchedStudents.setAll(t.getValue());
        });
        t.setOnFailed(evt -> t.getException().printStackTrace());
        new Thread(t, "searchStudent").start();
    }

    /**
     * 异步更新学生信息。
     *
     * @param s         要更新的学生对象。
     * @param onSuccess 更新成功时执行的回调。
     * @param onError   更新失败时执行的回调。
     */
    public void updateStudent(Student s, Runnable onSuccess, Runnable onError) {
        Task<Boolean> t = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(200); // 模拟网络延迟
                return app.vcampus.client.repository.FakeRepository.updateStudent(s);
            }
        };

        t.setOnSucceeded(evt -> {
            if (Boolean.TRUE.equals(t.getValue())) {
                // 若更新的是当前用户，刷新 currentStudent
                if (currentStudent.get() != null && currentStudent.get().getCardNumber() != null &&
                        currentStudent.get().getCardNumber().equals(s.getCardNumber())) {
                    Platform.runLater(() -> currentStudent.set(s));
                }
                if (onSuccess != null) onSuccess.run();
            } else {
                if (onError != null) onError.run();
            }
        });
        t.setOnFailed(evt -> {
            t.getException().printStackTrace();
            if (onError != null) onError.run();
        });

        new Thread(t, "updateStudent").start();
    }
}