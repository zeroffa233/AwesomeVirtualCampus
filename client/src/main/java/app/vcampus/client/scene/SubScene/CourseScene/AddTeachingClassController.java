package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.Course;
import app.vcampus.server.utility.Pair;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加教学班控制器。
 * 负责处理管理员添加新教学班的界面逻辑。
 */
public class AddTeachingClassController {
    @FXML private ComboBox<Course> courseComboBox;
    /**
     * 教师ID输入框。
     */
    @FXML private TextField teacherIdField;
    /**
     * 地点输入框。
     */
    @FXML private TextField placeField;
    /**
     * 容量输入框。
     */
    @FXML private TextField capacityField;
    /**
     * 课程表。
     */
    @FXML private TableView<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> scheduleTable;
    /**
     * 开始周列。
     */
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> startWeekColumn;
    /**
     * 结束周列。
     */
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> endWeekColumn;
    /**
     * 星期几列。
     */
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> weekdayColumn;
    /**
     * 开始节次列。
     */
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> startSectionColumn;
    /**
     * 结束节次列。
     */
    @FXML private TableColumn<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>, Integer> endSectionColumn;

    /**
     * 开始周输入框。
     */
    @FXML private TextField startWeekField;
    /**
     * 结束周输入框。
     */
    @FXML private TextField endWeekField;
    /**
     * 星期几输入框。
     */
    @FXML private TextField weekdayField;
    /**
     * 开始节次输入框。
     */
    @FXML private TextField startSectionField;
    /**
     * 结束节次输入框。
     */
    @FXML private TextField endSectionField;

    /**
     * 教务视图模型。
     */
    private TeachingAffairsViewModel viewModel;
    /**
     * 课程表数据。
     */
    private ObservableList<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> scheduleData = FXCollections.observableArrayList();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    public void initialize() {
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

    /**
     * 设置视图模型，并加载课程数据。
     *
     * @param viewModel 教务视图模型。
     */
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

    /**
     * 处理添加排课信息。
     */
    @FXML
    private void handleAddSchedule() {
        try {
            int startWeek = Integer.parseInt(startWeekField.getText().trim());
            int endWeek = Integer.parseInt(endWeekField.getText().trim());
            int weekday = Integer.parseInt(weekdayField.getText().trim());
            int startSection = Integer.parseInt(startSectionField.getText().trim());
            int endSection = Integer.parseInt(endSectionField.getText().trim());

            if (startWeek < 1 || endWeek < startWeek || weekday < 1 || weekday > 7 || startSection < 1 || endSection < startSection) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "请检查输入的排课信息");
                return;
            }

            Pair<Integer, Integer> weekRange = new Pair<>(startWeek, endWeek);
            Pair<Integer, Pair<Integer, Integer>> timeInfo = new Pair<>(weekday, new Pair<>(startSection, endSection));
            Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> scheduleItem = new Pair<>(weekRange, timeInfo);

            scheduleData.add(scheduleItem);

            startWeekField.clear();
            endWeekField.clear();
            weekdayField.clear();
            startSectionField.clear();
            endSectionField.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请输入有效的数字");
        }
    }

    /**
     * 处理移除排课信息。
     */
    @FXML
    private void handleRemoveSchedule() {
        Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> selectedItem = scheduleTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            scheduleData.remove(selectedItem);
        }
    }

    /**
     * 处理添加教学班。
     */
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

    /**
     * 显示警告框。
     *
     * @param type 警告类型。
     * @param title 标题。
     * @param message 消息。
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 清空所有输入字段。
     */
    private void clearFields() {
        teacherIdField.clear();
        placeField.clear();
        capacityField.clear();
        scheduleData.clear();
    }
}