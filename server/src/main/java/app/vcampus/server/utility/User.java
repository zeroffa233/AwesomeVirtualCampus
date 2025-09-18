package app.vcampus.server.utility;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 用户类，用于客户端显示。
 * 这个类使用了 JavaFX 的属性，以便于数据绑定。
 */
public class User {
    /**
     * 卡号属性。
     */
    private final IntegerProperty cardNum = new SimpleIntegerProperty();
    /**
     * 姓名属性。
     */
    private final StringProperty name = new SimpleStringProperty();
    /**
     * 角色字符串属性。
     */
    private final StringProperty roleStr = new SimpleStringProperty();
    /**
     * 性别属性。
     */
    private final StringProperty gender = new SimpleStringProperty();
    /**
     * 电子邮箱属性。
     */
    private final StringProperty email = new SimpleStringProperty();
    /**
     * 电话号码属性。
     */
    private final StringProperty phone = new SimpleStringProperty();

    /**
     * 从服务端的 User 实体类创建客户端的 User 对象。
     *
     * @param serverUser 服务端的 User 实体。
     * @return 转换后的客户端 User 对象。
     */
    public static User fromServerEntity(app.vcampus.server.entity.User serverUser) {
        User clientUser = new User();
        clientUser.setCardNum(serverUser.getCardNum());
        clientUser.setName(serverUser.getName());
        clientUser.setRoleStr(serverUser.getRoleStr());
        clientUser.setGender(serverUser.getGender().toString());
        clientUser.setEmail(serverUser.getEmail());
        clientUser.setPhone(serverUser.getPhone());
        return clientUser;
    }

    /**
     * 获取卡号。
     *
     * @return 卡号。
     */
    public int getCardNum() {
        return cardNum.get();
    }

    /**
     * 获取卡号的 IntegerProperty。
     *
     * @return 卡号属性。
     */
    public IntegerProperty cardNumProperty() {
        return cardNum;
    }

    /**
     * 设置卡号。
     *
     * @param cardNum 新的卡号。
     */
    public void setCardNum(int cardNum) {
        this.cardNum.set(cardNum);
    }

    /**
     * 获取姓名。
     *
     * @return 姓名。
     */
    public String getName() {
        return name.get();
    }

    /**
     * 获取姓名的 StringProperty。
     *
     * @return 姓名属性。
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * 设置姓名。
     *
     * @param name 新的姓名。
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * 获取角色字符串。
     *
     * @return 角色字符串。
     */
    public String getRoleStr() {
        return roleStr.get();
    }

    /**
     * 获取角色字符串的 StringProperty。
     *
     * @return 角色字符串属性。
     */
    public StringProperty roleStrProperty() {
        return roleStr;
    }

    /**
     * 设置角色字符串。
     *
     * @param roleStr 新的角色字符串。
     */
    public void setRoleStr(String roleStr) {
        this.roleStr.set(roleStr);
    }

    /**
     * 获取性别。
     *
     * @return 性别。
     */
    public String getGender() {
        return gender.get();
    }

    /**
     * 获取性别的 StringProperty。
     *
     * @return 性别属性。
     */
    public StringProperty genderProperty() {
        return gender;
    }

    /**
     * 设置性别。
     *
     * @param gender 新的性别。
     */
    public void setGender(String gender) {
        this.gender.set(gender);
    }

    /**
     * 获取电子邮箱。
     *
     * @return 电子邮箱。
     */
    public String getEmail() {
        return email.get();
    }

    /**
     * 获取电子邮箱的 StringProperty。
     *
     * @return 电子邮箱属性。
     */
    public StringProperty emailProperty() {
        return email;
    }

    /**
     * 设置电子邮箱。
     *
     * @param email 新的电子邮箱。
     */
    public void setEmail(String email) {
        this.email.set(email);
    }

    /**
     * 获取电话号码。
     *
     * @return 电话号码。
     */
    public String getPhone() {
        return phone.get();
    }

    /**
     * 获取电话号码的 StringProperty。
     *
     * @return 电话号码属性。
     */
    public StringProperty phoneProperty() {
        return phone;
    }

    /**
     * 设置电话号码。
     *
     * @param phone 新的电话号码。
     */
    public void setPhone(String phone) {
        this.phone.set(phone);
    }
}