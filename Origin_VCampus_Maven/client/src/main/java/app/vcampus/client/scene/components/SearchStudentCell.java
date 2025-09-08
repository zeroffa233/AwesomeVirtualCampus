package app.vcampus.client.scene.components;

import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * 简化版可编辑单元格：点击展开后可以编辑多个字段并提交到 viewModel.updateStudent(...)
 */
public class SearchStudentCell extends ListCell<Student> {
    private final StudentStatusViewModel viewModel;
    private final boolean editable;

    private VBox root;
    private HBox header;
    private VBox detailsBox;
    private boolean expanded = false;
    private boolean isEditing = false;

    public SearchStudentCell(StudentStatusViewModel viewModel, boolean editable) {
        this.viewModel = viewModel;
        this.editable = editable;
        createUI();
    }

    private void createUI() {
        root = new VBox();
        root.setSpacing(8);
        root.setPadding(new Insets(8));
        header = new HBox();
        header.setSpacing(12);

        detailsBox = new VBox();
        detailsBox.setSpacing(8);
        detailsBox.setVisible(false);

        root.getChildren().addAll(header, detailsBox);
    }

    @Override
    protected void updateItem(Student item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        header.getChildren().clear();
        detailsBox.getChildren().clear();

        Label title = new Label((item.getSchool() == null ? "" : item.getSchool()) + "  " + (item.getMajor()==null?"":item.getMajor()));
        Label names = new Label(item.getFamilyName() + " " + item.getGivenName());
        Label id = new Label("学号：" + (item.getStudentNumber()==null?"":item.getStudentNumber()) +
                "  一卡通：" + (item.getCardNumber()==null? "0": item.getCardNumber().toString()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        JFXButton expandBtn = new JFXButton("展开");
        expandBtn.setOnAction(e -> {
            expanded = !expanded;
            detailsBox.setVisible(expanded);
            expandBtn.setText(expanded ? "收起" : "展开");
        });

        header.getChildren().addAll(title, spacer, names, id, expandBtn);

        // details (editable)
        JFXTextField familyField = new JFXTextField(item.getFamilyName());
        familyField.setPromptText("姓");
        JFXTextField givenField = new JFXTextField(item.getGivenName());
        givenField.setPromptText("名");
        JFXTextField studentNumberField = new JFXTextField(item.getStudentNumber());
        studentNumberField.setPromptText("学号");
        JFXTextField majorField = new JFXTextField(item.getMajor());
        majorField.setPromptText("专业");
        JFXTextField schoolField = new JFXTextField(item.getSchool());
        schoolField.setPromptText("学院");

        HBox fieldsRow = new HBox(8, familyField, givenField, studentNumberField);
        HBox fieldsRow2 = new HBox(8, majorField, schoolField);

        HBox buttonRow = new HBox();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        JFXButton editBtn = new JFXButton("修改");
        JFXButton confirmBtn = new JFXButton("确认");
        JFXButton cancelBtn = new JFXButton("取消");

        editBtn.setOnAction(e -> {
            isEditing = true;
            familyField.setEditable(true);
            givenField.setEditable(true);
            studentNumberField.setEditable(true);
            majorField.setEditable(true);
            schoolField.setEditable(true);
        });

        confirmBtn.setOnAction(e -> {
            // copy values to a new Student and call update
            Student copy = new Student();
            copy.setCardNumber(item.getCardNumber());
            copy.setFamilyName(familyField.getText());
            copy.setGivenName(givenField.getText());
            copy.setStudentNumber(studentNumberField.getText());
            copy.setMajor(majorField.getText());
            copy.setSchool(schoolField.getText());
            viewModel.updateStudent(copy, () -> {
                // success
                Platform.runLater(() -> {
                    // update UI of this cell
                    getListView().getItems().set(getIndex(), copy);
                    isEditing = false;
                });
            }, () -> {
                // error
                // 简单提示，可替换为 Toast/Dialog
                System.err.println("更新失败");
            });
        });

        cancelBtn.setOnAction(e -> {
            // reset fields
            familyField.setText(item.getFamilyName());
            givenField.setText(item.getGivenName());
            studentNumberField.setText(item.getStudentNumber());
            majorField.setText(item.getMajor());
            schoolField.setText(item.getSchool());
            isEditing = false;
        });

        buttonRow.getChildren().addAll(spacer2, editBtn, confirmBtn, cancelBtn);

        detailsBox.getChildren().addAll(fieldsRow, fieldsRow2, buttonRow);

        setGraphic(root);
    }
}
