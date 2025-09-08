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
 * 简单的 ViewModel。实际项目中把 FakeRepository 替换为真实 repo/service。
 */
public class StudentStatusViewModel {
    private final ObjectProperty<Student> currentStudent = new SimpleObjectProperty<>();
    private final ObservableList<Student> searchedStudents = FXCollections.observableArrayList();

    public ObjectProperty<Student> currentStudentProperty() {
        return currentStudent;
    }

    public ObservableList<Student> getSearchedStudents() {
        return searchedStudents;
    }

    public void getStudentStatus() {
        Task<Student> t = new Task<>() {
            @Override
            protected Student call() throws Exception {
                // TODO: 替换为真实调用，例如 FakeRepository.getSelf()
                // 模拟阻塞网络调用
                Thread.sleep(200);
                return app.vcampus.client.repository.FakeRepository.getSelf();
            }
        };

        t.setOnSucceeded(evt -> currentStudent.set(t.getValue()));
        t.setOnFailed(evt -> t.getException().printStackTrace());
        new Thread(t, "getStudentStatus").start();
    }

    public void searchStudent(String keyword) {
        Task<List<Student>> t = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                Thread.sleep(200); // 模拟
                return app.vcampus.client.repository.FakeRepository.searchStudent(keyword);
            }
        };

        t.setOnSucceeded(evt -> {
            searchedStudents.setAll(t.getValue());
        });
        t.setOnFailed(evt -> t.getException().printStackTrace());
        new Thread(t, "searchStudent").start();
    }

    public void updateStudent(Student s, Runnable onSuccess, Runnable onError) {
        Task<Boolean> t = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(200);
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
