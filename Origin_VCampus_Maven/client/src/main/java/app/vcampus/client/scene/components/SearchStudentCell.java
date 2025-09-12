package app.vcampus.client.scene.components;

import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;

import java.util.Objects;

/**
 * 编辑型 ListCell：展开显示与 StudentStatusView 相同的字段。
 * editable 控制是否允许编辑/保存操作。
 */
public class SearchStudentCell extends ListCell<Student> {
    private final StudentStatusViewModel viewModel;
    private final boolean editable;

    // UI
    private final VBox root = new VBox(8);
    private final HBox header = new HBox(8);
    private final VBox detailsBox = new VBox(8);

    // fields (will be recreated per update to keep state simple)
    private boolean expanded = false;
    private boolean isEditing = false;

    public SearchStudentCell(StudentStatusViewModel viewModel, boolean editable) {
        this.viewModel = viewModel;
        this.editable = editable;
        initUI();
    }

    private void initUI() {
        root.setPadding(new Insets(8));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e8edf2; -fx-border-radius: 8;");
        header.setPadding(new Insets(4, 0, 0, 0));
        detailsBox.setPadding(new Insets(6, 0, 0, 0));
        detailsBox.setVisible(false);

        root.getChildren().addAll(header, detailsBox);
    }

    @Override
    protected void updateItem(Student item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        // reset
        header.getChildren().clear();
        detailsBox.getChildren().clear();
        expanded = false;
        isEditing = false;
        detailsBox.setVisible(false);

        // Header: school/major | spacer | name | id/card | expandBtn
        Label title = new Label((item.getSchool() == null ? "" : item.getSchool()) + "  " + (item.getMajor() == null ? "" : item.getMajor()));
        title.setStyle("-fx-font-size:13px; -fx-font-weight:600;");

        Label name = new Label(nullSafe(item.getFamilyName()) + " " + nullSafe(item.getGivenName()));
        name.setStyle("-fx-font-size:12px;");

        Label idCard = new Label("学号: " + nullSafe(item.getStudentNumber()) + "  一卡通: " + (item.getCardNumber() == null ? "" : item.getCardNumber().toString()));
        idCard.setStyle("-fx-font-size:11px; -fx-text-fill:#666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        JFXButton expandBtn = new JFXButton("展开");
        expandBtn.setOnAction(e -> {
            expanded = !expanded;
            detailsBox.setVisible(expanded);
            expandBtn.setText(expanded ? "收起" : "展开");
        });

        header.getChildren().addAll(title, spacer, name, idCard, expandBtn);

        // details (same fields as StudentStatusView) - initially readonly
        // create controls for each field
        JFXTextField familyField = new JFXTextField(nullSafe(item.getFamilyName()));
        familyField.setPromptText("姓");
        familyField.setEditable(false);

        JFXTextField givenField = new JFXTextField(nullSafe(item.getGivenName()));
        givenField.setPromptText("名");
        givenField.setEditable(false);

        JFXTextField studentNumberField = new JFXTextField(nullSafe(item.getStudentNumber()));
        studentNumberField.setPromptText("学号");
        studentNumberField.setEditable(false);

        JFXTextField cardNumberField = new JFXTextField(item.getCardNumber() == null ? "" : item.getCardNumber().toString());
        cardNumberField.setPromptText("一卡通");
        cardNumberField.setEditable(false);

        JFXTextField genderField = new JFXTextField(item.getGender() == null ? "" : item.getGender().getLabel());
        genderField.setPromptText("性别");
        genderField.setEditable(false);

        JFXTextField birthDateField = new JFXTextField(item.getBirthDate() == null ? "" : app.vcampus.server.utility.DateUtility.fromDate(item.getBirthDate()));
        birthDateField.setPromptText("出生日期");
        birthDateField.setEditable(false);

        JFXTextField birthPlaceField = new JFXTextField(nullSafe(item.getBirthPlace()));
        birthPlaceField.setPromptText("籍贯");
        birthPlaceField.setEditable(false);

        JFXTextField politicalStatusField = new JFXTextField(item.getPoliticalStatus() == null ? "" : item.getPoliticalStatus().getLabel());
        politicalStatusField.setPromptText("政治面貌");
        politicalStatusField.setEditable(false);

        JFXTextField statusField = new JFXTextField(item.getStatus() == null ? "" : item.getStatus().getLabel());
        statusField.setPromptText("学籍状态");
        statusField.setEditable(false);

        JFXTextField majorField = new JFXTextField(nullSafe(item.getMajor()));
        majorField.setPromptText("专业");
        majorField.setEditable(false);

        JFXTextField schoolField = new JFXTextField(nullSafe(item.getSchool()));
        schoolField.setPromptText("学院");
        schoolField.setEditable(false);

        // layout rows
        HBox row1 = new HBox(8, familyField, givenField, studentNumberField);
        HBox row2 = new HBox(8, cardNumberField, genderField, birthDateField);
        HBox row3 = new HBox(8, birthPlaceField, politicalStatusField, statusField);
        HBox row4 = new HBox(8, majorField, schoolField);

        // buttons row
        HBox buttonRow = new HBox(8);
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        JFXButton editBtn = new JFXButton("修改");
        JFXButton confirmBtn = new JFXButton("确认");
        JFXButton cancelBtn = new JFXButton("取消");

        // initial visibility/state
        editBtn.setVisible(editable);
        confirmBtn.setVisible(false);
        cancelBtn.setVisible(false);

        // edit action: enable editing
        editBtn.setOnAction(e -> {
            if (!editable) return;
            isEditing = true;
            familyField.setEditable(true);
            givenField.setEditable(true);
            studentNumberField.setEditable(true);
            // card number usually shouldn't be editable, keep it read-only
            majorField.setEditable(true);
            schoolField.setEditable(true);
            genderField.setEditable(true);
            birthDateField.setEditable(true);
            birthPlaceField.setEditable(true);
            politicalStatusField.setEditable(true);
            statusField.setEditable(true);

            editBtn.setVisible(false);
            confirmBtn.setVisible(true);
            cancelBtn.setVisible(true);
        });

        // cancel: restore original values
        cancelBtn.setOnAction(e -> {
            isEditing = false;
            familyField.setText(nullSafe(item.getFamilyName()));
            givenField.setText(nullSafe(item.getGivenName()));
            studentNumberField.setText(nullSafe(item.getStudentNumber()));
            cardNumberField.setText(item.getCardNumber() == null ? "" : item.getCardNumber().toString());
            majorField.setText(nullSafe(item.getMajor()));
            schoolField.setText(nullSafe(item.getSchool()));
            genderField.setText(item.getGender() == null ? "" : item.getGender().getLabel());
            birthDateField.setText(item.getBirthDate() == null ? "" : app.vcampus.server.utility.DateUtility.fromDate(item.getBirthDate()));
            birthPlaceField.setText(nullSafe(item.getBirthPlace()));
            politicalStatusField.setText(item.getPoliticalStatus() == null ? "" : item.getPoliticalStatus().getLabel());
            statusField.setText(item.getStatus() == null ? "" : item.getStatus().getLabel());

            familyField.setEditable(false);
            givenField.setEditable(false);
            studentNumberField.setEditable(false);
            majorField.setEditable(false);
            schoolField.setEditable(false);
            genderField.setEditable(false);
            birthDateField.setEditable(false);
            birthPlaceField.setEditable(false);
            politicalStatusField.setEditable(false);
            statusField.setEditable(false);

            editBtn.setVisible(editable);
            confirmBtn.setVisible(false);
            cancelBtn.setVisible(false);
        });

        // confirm: build updated Student, call viewModel.updateStudent(...)
        confirmBtn.setOnAction(e -> {
            // build updated copy: preserve cardNumber and other identity fields
            Student updated = new Student();
            // preserve identification fields if present
            try {
                updated.setCardNumber(item.getCardNumber());
            } catch (Throwable ignored) {}
            // set editable fields from UI
            updated.setFamilyName(familyField.getText());
            updated.setGivenName(givenField.getText());
            updated.setStudentNumber(studentNumberField.getText());
            updated.setMajor(majorField.getText());
            updated.setSchool(schoolField.getText());
            // NOTE: for enums like gender/politicalStatus/status you might need mapping from text to enum
            // Here we leave them unchanged if mapping not available
            // If you have setter overloads, call them accordingly.

            // Call update (asynchronous). Provide success and error callbacks.
            viewModel.updateStudent(updated,
                    () -> Platform.runLater(() -> {
                        // success: update item in the ListView if possible
                        try {
                            int idx = getIndex();
                            if (getListView() != null && idx >= 0 && idx < getListView().getItems().size()) {
                                // replace element (we replace with 'updated' but preserve non-edited fields from original)
                                Student replaced = mergeStudents(item, updated);
                                getListView().getItems().set(idx, replaced);
                                getListView().refresh();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            // restore UI state
                            cancelBtn.fire();
                        }
                    }),
                    () -> Platform.runLater(() -> {
                        // error handling: simple stderr print; you can pop up dialog if needed
                        System.err.println("Student update failed for: " + item.getStudentNumber());
                    })
            );
        });

        buttonRow.getChildren().addAll(btnSpacer, editBtn, confirmBtn, cancelBtn);

        detailsBox.getChildren().addAll(row1, row2, row3, row4, buttonRow);

        setGraphic(root);
    }

    /**
     * 在将更新结果放回列表时，尽量保留原对象中除已编辑字段外的其它重要字段。
     * This merges 'base' (original) and 'updated' (partial) into a new Student to show in UI.
     */
    private Student mergeStudents(Student base, Student updated) {
        Student merged = new Student();
        // identity
        try { merged.setCardNumber(base.getCardNumber()); } catch (Throwable ignored) {}

        // prefer updated if provided, else base
        merged.setFamilyName(isEmpty(updated.getFamilyName()) ? base.getFamilyName() : updated.getFamilyName());
        merged.setGivenName(isEmpty(updated.getGivenName()) ? base.getGivenName() : updated.getGivenName());
        merged.setStudentNumber(isEmpty(updated.getStudentNumber()) ? base.getStudentNumber() : updated.getStudentNumber());
        merged.setMajor(isEmpty(updated.getMajor()) ? base.getMajor() : updated.getMajor());
        merged.setSchool(isEmpty(updated.getSchool()) ? base.getSchool() : updated.getSchool());

        // copy other fields from base (gender, birthDate, status...) if setters/getters exist
        try { merged.setGender(base.getGender()); } catch (Throwable ignored) {}
        try { merged.setBirthDate(base.getBirthDate()); } catch (Throwable ignored) {}
        try { merged.setBirthPlace(base.getBirthPlace()); } catch (Throwable ignored) {}
        try { merged.setPoliticalStatus(base.getPoliticalStatus()); } catch (Throwable ignored) {}
        try { merged.setStatus(base.getStatus()); } catch (Throwable ignored) {}

        return merged;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
