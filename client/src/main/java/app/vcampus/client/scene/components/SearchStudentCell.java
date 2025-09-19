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
import javafx.util.Duration;
import lombok.Getter;

/**
 * 可编辑的学生信息列表单元格。
 * <p>
 * 用于在 ListView 中显示学生信息，并提供展开、编辑和保存功能。
 * </p>
 */
public class SearchStudentCell extends ListCell<Student> {
    private final StudentStatusViewModel viewModel;
    private final boolean editable;

    @Getter
    private final VBox root = new VBox(12);
    private final HBox header = new HBox(12);
    private final VBox detailsBox = new VBox(12);

    private boolean expanded = false;
    private boolean isEditing = false;

    /**
     * 构造函数。
     *
     * @param viewModel 学生学籍视图模型。
     * @param editable  是否允许编辑。
     */
    public SearchStudentCell(StudentStatusViewModel viewModel, boolean editable) {
        this.viewModel = viewModel;
        this.editable = editable;
        initUI();
    }

    private void initUI() {
        root.setPadding(new Insets(16));
        root.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 12px;"
                        + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                        + "-fx-border-color: #f0f2f5;"
                        + "-fx-border-radius: 12px;"
                        + "-fx-border-width: 1px;"
        );

        header.setPadding(new Insets(0));

        detailsBox.setPadding(new Insets(8, 0, 0, 0));
        detailsBox.setVisible(false);
        detailsBox.setManaged(false);
        detailsBox.setMaxHeight(0);

        root.getChildren().addAll(header, detailsBox);

        root.setOnMouseEntered(e -> {
            root.setStyle(
                    "-fx-background-color: white;"
                            + "-fx-background-radius: 12px;"
                            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 10, 0, 0, 4);"
                            + "-fx-border-color: #e6e9ed;"
                            + "-fx-border-radius: 12px;"
                            + "-fx-border-width: 1px;"
            );
        });

        root.setOnMouseExited(e -> {
            root.setStyle(
                    "-fx-background-color: white;"
                            + "-fx-background-radius: 12px;"
                            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                            + "-fx-border-color: #f0f2f5;"
                            + "-fx-border-radius: 12px;"
                            + "-fx-border-width: 1px;"
            );
        });
    }

    /**
     * 更新单元格内容。
     *
     * @param item  要显示的学生对象。
     * @param empty 是否为空单元格。
     */
    @Override
    public void updateItem(Student item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        header.getChildren().clear();
        detailsBox.getChildren().clear();
        expanded = false;
        isEditing = false;
        detailsBox.setVisible(false);
        detailsBox.setManaged(false);
        detailsBox.setMaxHeight(0);

        Label title = new Label((item.getSchool() == null ? "" : item.getSchool()) + "  " +
                (item.getMajor() == null ? "" : item.getMajor()));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #333;");

        Label name = new Label(nullSafe(item.getFamilyName()) + " " + nullSafe(item.getGivenName()));
        name.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");

        Label idCard = new Label("学号: " + nullSafe(item.getStudentNumber()) + "  一卡通: " +
                (item.getCardNumber() == null ? "" : item.getCardNumber().toString()));
        idCard.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        JFXButton expandBtn = new JFXButton("展开");
        expandBtn.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #444; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-padding: 6 12 6 12;");

        expandBtn.setOnMouseEntered(e -> expandBtn.setStyle("-fx-background-color: #e6e9ed; -fx-text-fill: #333; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-padding: 6 12 6 12;"));
        expandBtn.setOnMouseExited(e -> expandBtn.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #444; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-padding: 6 12 6 12;"));

        expandBtn.setOnAction(e -> {
            expanded = !expanded;
            expandBtn.setText(expanded ? "收起" : "展开");
            ExpandCollapseUtil.animate(detailsBox, expanded, Duration.millis(300), () -> {
                if (getListView() != null) getListView().requestLayout();
            });
        });

        header.getChildren().addAll(title, spacer, name, idCard, expandBtn);

        JFXTextField familyField = createTextField(nullSafe(item.getFamilyName()), "姓");
        JFXTextField givenField = createTextField(nullSafe(item.getGivenName()), "名");
        JFXTextField studentNumberField = createTextField(nullSafe(item.getStudentNumber()), "学号");
        JFXTextField cardNumberField = createTextField(item.getCardNumber() == null ? "" : item.getCardNumber().toString(), "一卡通");
        JFXTextField genderField = createTextField(item.getGender() == null ? "" : item.getGender().getLabel(), "性别");
        JFXTextField birthDateField = createTextField(item.getBirthDate() == null ? "" :
                app.vcampus.server.utility.DateUtility.fromDate(item.getBirthDate()), "出生日期");
        JFXTextField birthPlaceField = createTextField(nullSafe(item.getBirthPlace()), "籍贯");
        JFXTextField politicalStatusField = createTextField(item.getPoliticalStatus() == null ? "" :
                item.getPoliticalStatus().getLabel(), "政治面貌");
        JFXTextField statusField = createTextField(item.getStatus() == null ? "" :
                item.getStatus().getLabel(), "学籍状态");
        JFXTextField majorField = createTextField(nullSafe(item.getMajor()), "专业");
        JFXTextField schoolField = createTextField(nullSafe(item.getSchool()), "学院");

        HBox row1 = new HBox(16);
        row1.getChildren().addAll(
                createFieldVBox("姓", familyField),
                createFieldVBox("名", givenField),
                createFieldVBox("学号", studentNumberField),
                createFieldVBox("一卡通", cardNumberField),
                createFieldVBox("性别", genderField),
                createFieldVBox("出生日期", birthDateField)
        );

        HBox row2 = new HBox(16);
        row2.getChildren().addAll(
                createFieldVBox("籍贯", birthPlaceField),
                createFieldVBox("政治面貌", politicalStatusField),
                createFieldVBox("学籍状态", statusField),
                createFieldVBox("专业", majorField),
                createFieldVBox("学院", schoolField)
        );

        HBox buttonRow = new HBox(12);
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        JFXButton editBtn = new JFXButton("修改");
        editBtn.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #444; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-padding: 8 16 8 16;");
        JFXButton confirmBtn = new JFXButton("确认");
        confirmBtn.setStyle("-fx-background-color: #607830; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-padding: 8 16 8 16;");
        JFXButton cancelBtn = new JFXButton("取消");
        cancelBtn.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #444; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-padding: 8 16 8 16;");

        addButtonHoverEffects(editBtn, "#f0f2f5", "#e6e9ed");
        addButtonHoverEffects(confirmBtn, "#607830", "#4f6228");
        addButtonHoverEffects(cancelBtn, "#f0f2f5", "#e6e9ed");

        editBtn.setVisible(editable);
        confirmBtn.setVisible(false);
        cancelBtn.setVisible(false);

        editBtn.setOnAction(e -> {
            if (!editable) return;
            isEditing = true;
            familyField.setEditable(true);
            givenField.setEditable(true);
            studentNumberField.setEditable(true);
            majorField.setEditable(true);
            schoolField.setEditable(true);
            genderField.setEditable(true);
            birthDateField.setEditable(true);
            birthPlaceField.setEditable(true);
            politicalStatusField.setEditable(true);
            statusField.setEditable(true);

            setEditableFieldStyle(familyField);
            setEditableFieldStyle(givenField);
            setEditableFieldStyle(studentNumberField);
            setEditableFieldStyle(majorField);
            setEditableFieldStyle(schoolField);
            setEditableFieldStyle(genderField);
            setEditableFieldStyle(birthDateField);
            setEditableFieldStyle(birthPlaceField);
            setEditableFieldStyle(politicalStatusField);
            setEditableFieldStyle(statusField);

            editBtn.setVisible(false);
            confirmBtn.setVisible(true);
            cancelBtn.setVisible(true);
        });

        cancelBtn.setOnAction(e -> {
            isEditing = false;
            familyField.setText(nullSafe(item.getFamilyName()));
            givenField.setText(nullSafe(item.getGivenName()));
            studentNumberField.setText(nullSafe(item.getStudentNumber()));
            cardNumberField.setText(item.getCardNumber() == null ? "" : item.getCardNumber().toString());
            majorField.setText(nullSafe(item.getMajor()));
            schoolField.setText(nullSafe(item.getSchool()));
            genderField.setText(item.getGender() == null ? "" : item.getGender().getLabel());
            birthDateField.setText(item.getBirthDate() == null ? "" :
                    app.vcampus.server.utility.DateUtility.fromDate(item.getBirthDate()));
            birthPlaceField.setText(nullSafe(item.getBirthPlace()));
            politicalStatusField.setText(item.getPoliticalStatus() == null ? "" :
                    item.getPoliticalStatus().getLabel());
            statusField.setText(item.getStatus() == null ? "" : item.getStatus().getLabel());

            resetTextFieldStyle(familyField);
            resetTextFieldStyle(givenField);
            resetTextFieldStyle(studentNumberField);
            resetTextFieldStyle(majorField);
            resetTextFieldStyle(schoolField);
            resetTextFieldStyle(genderField);
            resetTextFieldStyle(birthDateField);
            resetTextFieldStyle(birthPlaceField);
            resetTextFieldStyle(politicalStatusField);
            resetTextFieldStyle(statusField);

            editBtn.setVisible(editable);
            confirmBtn.setVisible(false);
            cancelBtn.setVisible(false);
        });

        confirmBtn.setOnAction(e -> {
            Student updated = new Student();
            try { updated.setCardNumber(item.getCardNumber()); } catch (Throwable ignored) {}
            updated.setFamilyName(familyField.getText());
            updated.setGivenName(givenField.getText());
            updated.setStudentNumber(studentNumberField.getText());
            updated.setMajor(majorField.getText());
            updated.setSchool(schoolField.getText());

            // 添加必填字段
            try { updated.setGender(item.getGender()); } catch (Throwable ignored) {}
            try { updated.setBirthDate(item.getBirthDate()); } catch (Throwable ignored) {}
            try { updated.setBirthPlace(item.getBirthPlace()); } catch (Throwable ignored) {}
            try { updated.setPoliticalStatus(item.getPoliticalStatus()); } catch (Throwable ignored) {}
            try { updated.setStatus(item.getStatus()); } catch (Throwable ignored) {}

            viewModel.updateStudent(updated,
                    () -> Platform.runLater(() -> {
                        try {
                            // 修改1：直接使用item引用更新数据
                            // 合并更新后的学生信息
                            Student merged = mergeStudents(item, updated);

                            // 修改2：更新UI显示的文本
                            familyField.setText(merged.getFamilyName());
                            givenField.setText(merged.getGivenName());
                            studentNumberField.setText(merged.getStudentNumber());
                            majorField.setText(merged.getMajor());
                            schoolField.setText(merged.getSchool());

                            // 修改3：刷新ListView而不是尝试替换单个项目
                            if (getListView() != null) {
                                getListView().refresh();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            // 修改4：不直接调用cancelBtn.fire()，而是手动恢复非编辑状态
                            isEditing = false;
                            resetTextFieldStyle(familyField);
                            resetTextFieldStyle(givenField);
                            resetTextFieldStyle(studentNumberField);
                            resetTextFieldStyle(majorField);
                            resetTextFieldStyle(schoolField);
                            resetTextFieldStyle(genderField);
                            resetTextFieldStyle(birthDateField);
                            resetTextFieldStyle(birthPlaceField);
                            resetTextFieldStyle(politicalStatusField);
                            resetTextFieldStyle(statusField);

                            editBtn.setVisible(editable);
                            confirmBtn.setVisible(false);
                            cancelBtn.setVisible(false);
                        }
                    }),
                    () -> Platform.runLater(() -> {
                        System.err.println("Student update failed for: " + item.getStudentNumber());
                    })
            );
        });

        buttonRow.getChildren().addAll(btnSpacer, editBtn, confirmBtn, cancelBtn);

        detailsBox.getChildren().addAll(row1, row2, buttonRow);

        root.setMaxWidth(Double.MAX_VALUE);

        setGraphic(root);
    }

    private VBox createFieldVBox(String labelText, JFXTextField textField) {
        VBox fieldBox = new VBox(6);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #555; -fx-padding: 0 0 2 0;");

        fieldBox.getChildren().addAll(label, textField);
        return fieldBox;
    }

    private JFXTextField createTextField(String text, String promptText) {
        JFXTextField field = new JFXTextField(text);
        field.setPromptText(promptText);
        field.setEditable(false);
        resetTextFieldStyle(field);
        return field;
    }

    private void resetTextFieldStyle(JFXTextField field) {
        field.setEditable(false);
        field.setStyle(
                "-fx-background-color: #f5f7fa;"
                        + "-fx-text-fill: #333;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-border-color: #e6e9ed;"
                        + "-fx-border-radius: 6px;"
                        + "-fx-border-width: 1px;"
                        + "-fx-font-size: 12px;"
                        + "-fx-padding: 8;"
        );
    }

    private void setEditableFieldStyle(JFXTextField field) {
        field.setEditable(true);
        field.setStyle(
                "-fx-background-color: white;"
                        + "-fx-text-fill: #333;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-border-color: #4f6228;"
                        + "-fx-border-radius: 6px;"
                        + "-fx-border-width: 1px;"
                        + "-fx-font-size: 12px;"
                        + "-fx-padding: 8;"
        );
    }

    private void addButtonHoverEffects(JFXButton button, String normalColor, String hoverColor) {
        String originalStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle.replace(normalColor, hoverColor));
        });

        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
        });
    }

    private Student mergeStudents(Student base, Student updated) {
        Student merged = new Student();
        try { merged.setCardNumber(base.getCardNumber()); } catch (Throwable ignored) {}

        merged.setFamilyName(isEmpty(updated.getFamilyName()) ? base.getFamilyName() : updated.getFamilyName());
        merged.setGivenName(isEmpty(updated.getGivenName()) ? base.getGivenName() : updated.getGivenName());
        merged.setStudentNumber(isEmpty(updated.getStudentNumber()) ? base.getStudentNumber() : updated.getStudentNumber());
        merged.setMajor(isEmpty(updated.getMajor()) ? base.getMajor() : updated.getMajor());
        merged.setSchool(isEmpty(updated.getSchool()) ? base.getSchool() : updated.getSchool());

        try { merged.setGender(base.getGender()); } catch (Throwable ignored) {}
        try { merged.setBirthDate(base.getBirthDate()); } catch (Throwable ignored) {}
        try { merged.setBirthPlace(base.getBirthPlace()); } catch (Throwable ignored) {}
        try { merged.setPoliticalStatus(base.getPoliticalStatus()); } catch (Throwable ignored) {}
        try { merged.setStatus(base.getStatus()); } catch (Throwable ignored) {}

        return merged;
    }

    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private String nullSafe(String s) { return s == null ? "" : s; }
}