package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.AdminClient;
import app.vcampus.server.utility.User;
import app.vcampus.client.repository.FakeRepository;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

/**
 * 管理员视图模型。
 * 负责处理管理员界面的用户管理逻辑，包括获取、保存、删除和显示用户信息。
 */
public class AdminViewModel {
    /**
     * 用户列表。
     */
    public final ObservableList<User> users = FXCollections.observableArrayList();
    /**
     * 当前选中的用户属性。
     */
    public final ObjectProperty<User> selectedUser = new SimpleObjectProperty<>();

    /**
     * 卡号输入框的属性。
     */
    public final IntegerProperty cardNum = new SimpleIntegerProperty();
    /**
     * 姓名输入框的属性。
     */
    public final StringProperty name = new SimpleStringProperty("");
    /**
     * 角色输入框的属性。
     */
    public final StringProperty roles = new SimpleStringProperty("");
    /**
     * 性别选择框的属性。
     */
    public final StringProperty gender = new SimpleStringProperty("");
    /**
     * 电子邮箱输入框的属性。
     */
    public final StringProperty email = new SimpleStringProperty("");
    /**
     * 电话号码输入框的属性。
     */
    public final StringProperty phone = new SimpleStringProperty("");
    /**
     * 密码输入框的属性。
     */
    public final StringProperty password = new SimpleStringProperty("");

    /**
     * 错误消息文本属性。
     */
    public final StringProperty errorMessage = new SimpleStringProperty("");

    /**
     * 异步从服务器获取所有用户列表。
     */
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

    /**
     * 异步保存用户信息（新建或更新）。
     */
    public void saveUser() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (selectedUser.get() == null) { // 新建用户
                    return AdminClient.addUser(FakeRepository.handler, cardNum.get(), name.get(), password.get(), gender.get(), email.get(), phone.get(), roles.get());
                } else { // 更新已有用户
                    return AdminClient.updateUser(FakeRepository.handler, cardNum.get(), roles.get(), password.get());
                }
            }
        };

        task.setOnSucceeded(event -> {
            if (task.getValue()) {
                fetchUsers(); // 刷新列表
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

    /**
     * 异步删除选中的用户。
     */
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
                fetchUsers(); // 刷新列表
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

    /**
     * 清空表单内容。
     */
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

    /**
     * 使用指定用户信息填充表单。
     *
     * @param user 要显示的用户信息。
     */
    public void setupForm(User user) {
        if (user != null) {
            cardNum.set(user.getCardNum());
            name.set(user.getName());
            roles.set(user.getRoleStr());
            gender.set(user.getGender());
            email.set(user.getEmail());
            phone.set(user.getPhone());
            password.set(""); // 出于安全考虑，清空密码字段
        } else {
            clearForm();
        }
    }
}