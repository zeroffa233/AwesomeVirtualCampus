package app.vcampus.server.utility;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    private final IntegerProperty cardNum = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty roleStr = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();

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

    

    public int getCardNum() {
        return cardNum.get();
    }

    public IntegerProperty cardNumProperty() {
        return cardNum;
    }

    public void setCardNum(int cardNum) {
        this.cardNum.set(cardNum);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getRoleStr() {
        return roleStr.get();
    }

    public StringProperty roleStrProperty() {
        return roleStr;
    }

    public void setRoleStr(String roleStr) {
        this.roleStr.set(roleStr);
    }

    public String getGender() {
        return gender.get();
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }
}
