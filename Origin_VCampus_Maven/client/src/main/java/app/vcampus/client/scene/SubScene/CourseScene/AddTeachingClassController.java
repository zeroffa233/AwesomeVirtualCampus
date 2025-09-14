package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.Pair;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AddTeachingClassController {
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private TextField teacherIdField;
    @FXML private TextField placeField;
    @FXML private TextField capacityField;
    @FXML private TableView<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> scheduleTable;
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> startWeekColumn;
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> endWeekColumn;
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> weekdayColumn;
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> startSectionColumn;
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> endSectionColumn;

    @FXML private TextField startWeekField;
    @FXML private TextField endWeekField;
    @FXML private TextField weekdayField;
    @FXML private TextField startSectionField;
    @FXML private TextField endSectionField;

    private TeachingAffairsViewModel viewModel;
    private ObservableList<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> scheduleData = FXCollections.observableArrayList();

    public void initialize() {
        // 初始化课程下拉框
        courseComboBox.setCellFactory(param -> new ListCell<Course>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCourseName() + " (" + item.getCourseId() + ")");
                }
            }
        });

        courseComboBox.setButtonCell(new ListCell<Course>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCourseName() + " (" + item.getCourseId() + ")");
                }
            }
        });

        // 初始化课程表
        startWeekColumn.setCellValueFactory(cellData -> {
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> schedule = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(schedule.getFirst().getFirst()).asObject();
        });

        endWeekColumn.setCellValueFactory(cellData -> {
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> schedule = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(schedule.getFirst().getSecond()).asObject();
        });

        weekdayColumn.setCellValueFactory(cellData -> {
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> schedule = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(schedule.getSecond().getFirst()).asObject();
        });

        startSectionColumn.setCellValueFactory(cellData -> {
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> schedule = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(schedule.getSecond().getSecond().getFirst()).asObject();
        });

        endSectionColumn.setCellValueFactory(cellData -> {
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> schedule = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(schedule.getSecond().getSecond().getSecond()).asObject();
        });

        scheduleTable.setItems(scheduleData);
    }

    public void setViewModel(TeachingAffairsViewModel viewModel) {
        this.viewModel = viewModel;
        loadCourses();
    }

    private void loadCourses() {
        if (viewModel != null) {
            viewModel.myClasses.getSelectableCourses();
            viewModel.myClasses.allCourses.addListener((javafx.collections.ListChangeListener.Change<? extends Course> c) -> {
                Platform.runLater(() -> {
                    courseComboBox.setItems(viewModel.myClasses.allCourses);
                });
            });
        }
    }

    @FXML
    private void handleAddSchedule() {
        try {
            int startWeek = Integer.parseInt(startWeekField.getText().trim());
            int endWeek = Integer.parseInt(endWeekField.getText().trim());
            int weekday = Integer.parseInt(weekdayField.getText().trim());
            int startSection = Integer.parseInt(startSectionField.getText().trim());
            int endSection = Integer.parseInt(endSectionField.getText().trim());

            // 验证输入
            if (startWeek < 1 || endWeek < startWeek || weekday < 1 || weekday > 7 || startSection < 1 || endSection < startSection) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "请检查输入的排课信息");
                return;
            }

            Pair<Integer, Integer> weekRange = new Pair<>(startWeek, endWeek);
            Pair<Integer, Pair<Integer, Integer>> timeInfo = new Pair<>(weekday, new Pair<>(startSection, endSection));
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> scheduleItem = new Pair<>(weekRange, timeInfo);

            scheduleData.add(scheduleItem);

            // 清空输入框
            startWeekField.clear();
            endWeekField.clear();
            weekdayField.clear();
            startSectionField.clear();
            endSectionField.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请输入有效的数字");
        }
    }

    @FXML
    private void handleRemoveSchedule() {
        Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> selectedItem = scheduleTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            scheduleData.remove(selectedItem);
        }
    }

    @FXML
    private void handleAddTeachingClass() {
        try {
            if (viewModel == null) {
                showAlert(Alert.AlertType.ERROR, "错误", "ViewModel未初始化");
                return;
            }

            Course selectedCourse = courseComboBox.getValue();
            if (selectedCourse == null) {
                showAlert(Alert.AlertType.ERROR, "错误", "请选择一个课程");
                return;
            }

            int teacherId = Integer.parseInt(teacherIdField.getText().trim());
            String place = placeField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());

            if (place.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "请填写上课地点");
                return;
            }

            if (scheduleData.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "请至少添加一个排课信息");
                return;
            }

            List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> schedule = new ArrayList<>(scheduleData);

            viewModel.adminTools.addTeachingClass(selectedCourse.getUuid(), teacherId, place, capacity, schedule)
                    .thenAccept(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                showAlert(Alert.AlertType.INFORMATION, "成功", "教学班添加成功");
                                clearFields();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "错误", "教学班添加失败");
                            }
                        });
                    });
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "教师工号和容量必须是数字");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", "添加教学班时发生错误");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        teacherIdField.clear();
        placeField.clear();
        capacityField.clear();
        scheduleData.clear();
    }
}