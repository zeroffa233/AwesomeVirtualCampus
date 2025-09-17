package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.AdminClient;
import app.vcampus.server.utility.User;
import app.vcampus.client.repository.FakeRepository;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

public class AdminViewModel {
    public final ObservableList<User> users = FXCollections.observableArrayList();
    public final ObjectProperty<User> selectedUser = new SimpleObjectProperty<>();

    public final IntegerProperty cardNum = new SimpleIntegerProperty();
    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty roles = new SimpleStringProperty("");
    public final StringProperty gender = new SimpleStringProperty("");
    public final StringProperty email = new SimpleStringProperty("");
    public final StringProperty phone = new SimpleStringProperty("");
    public final StringProperty password = new SimpleStringProperty("");

    public final StringProperty errorMessage = new SimpleStringProperty("");

    public void fetchUsers() {
        Task<List<app.vcampus.server.entity.User>> task = new Task<>() {
            @Override
            protected List<app.vcampus.server.entity.User> call() throws Exception {
                return AdminClient.getAllUsers(FakeRepository.handler);
            }
        };

        task.setOnSucceeded(event -> {
            List<app.vcampus.server.entity.User> result = task.getValue();
            if (result != null) {
                users.clear();
                result.forEach(serverUser -> users.add(User.fromServerEntity(serverUser)));
            }
        });

        task.setOnFailed(event -> {
            errorMessage.set("无法加载用户列表: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public void saveUser() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (selectedUser.get() == null) { // Create new user
                    return AdminClient.addUser(FakeRepository.handler, cardNum.get(), name.get(), password.get(), gender.get(), email.get(), phone.get(), roles.get());
                } else { // Update existing user
                    return AdminClient.updateUser(FakeRepository.handler, cardNum.get(), roles.get(), password.get());
                }
            }
        };

        task.setOnSucceeded(event -> {
            if (task.getValue()) {
                fetchUsers(); // Refresh the list
                clearForm();
            } else {
                errorMessage.set("保存用户失败");
            }
        });

        task.setOnFailed(event -> {
            errorMessage.set("保存用户失败: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public void deleteUser() {
        if (selectedUser.get() == null) return;

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return AdminClient.deleteUser(FakeRepository.handler, selectedUser.get().getCardNum());
            }
        };

        task.setOnSucceeded(event -> {
            if (task.getValue()) {
                fetchUsers(); // Refresh the list
                clearForm();
            } else {
                errorMessage.set("删除用户失败");
            }
        });

        task.setOnFailed(event -> {
            errorMessage.set("删除用户失败: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public void clearForm() {
        selectedUser.set(null);
        cardNum.set(0);
        name.set("");
        roles.set("");
        gender.set("");
        email.set("");
        phone.set("");
        password.set("");
        errorMessage.set("");
    }

    public void setupForm(User user) {
        if (user != null) {
            cardNum.set(user.getCardNum());
            name.set(user.getName());
            roles.set(user.getRoleStr());
            gender.set(user.getGender());
            email.set(user.getEmail());
            phone.set(user.getPhone());
            password.set(""); // Clear password field for security
        } else {
            clearForm();
        }
    }
}
