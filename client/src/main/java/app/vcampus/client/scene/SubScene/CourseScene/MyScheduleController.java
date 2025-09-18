package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.scene.SubScene.CourseScene.ClassItemController;
import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.Pair;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.List;

public class MyScheduleController {

    @FXML private Label headingLabel;
    @FXML private Label captionLabel;
    @FXML private ToggleButton prevWeekBtn;
    @FXML private ToggleButton nextWeekBtn;
    @FXML private Label weekLabel;
    @FXML private GridPane timetableGrid;
    @FXML private ScrollPane outerScroll;

    // external viewmodel must be injected after FXMLLoader.load()
    private TeachingAffairsViewModel viewModel;

    // state
    private int currentWeek = 1;
    private final int MAX_WEEKS = 16;
    private final int TOTAL_SECTIONS = 13; // 第1-13节
    private final int WEEKDAYS = 7;

    public MyScheduleController() {
        System.out.println("[MySchedule] <CTOR> called on thread: " + Thread.currentThread().getName());
        // optionally load fonts here
    }

    @FXML
    public void initialize() {
        System.out.println("[MySchedule] initialize() called on thread: " + Thread.currentThread().getName());
        // build grid structure: first column for section numbers, rest 7 columns for weekdays
        buildGrid();
        prevWeekBtn.setOnAction(e -> {
            if (currentWeek > 1) {
                currentWeek--;
                updateWeekLabel();
                renderTable();
            }
        });
        nextWeekBtn.setOnAction(e -> {
            if (currentWeek < MAX_WEEKS) {
                currentWeek++;
                updateWeekLabel();
                renderTable();
            }
        });

        updateWeekLabel();
    }

    public void setViewModel(TeachingAffairsViewModel vm) {
        System.out.println("[MySchedule] setViewModel() called, vm=" + vm);
        this.viewModel = vm;
        // initialize data
        Platform.runLater(() -> {
            vm.myClasses.init();
            // add listener so re-render when selected courses change
            vm.myClasses.selected.addListener((ListChangeListener<? super TeachingClass>) c -> {
                Platform.runLater(this::renderTable);
            });
            // initial render
            renderTable();
        });
    }

    private void updateWeekLabel() {
        weekLabel.setText("第 " + currentWeek + " 周");
        prevWeekBtn.setDisable(currentWeek <= 1);
        nextWeekBtn.setDisable(currentWeek >= MAX_WEEKS);
    }

    private void buildGrid() {
        timetableGrid.getChildren().clear();
        timetableGrid.getColumnConstraints().clear();
        timetableGrid.getRowConstraints().clear();


        // First column (time labels)
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(12);
        col0.setHgrow(Priority.NEVER);
        timetableGrid.getColumnConstraints().add(col0);

        // 7 weekday columns
        for (int i = 0; i < WEEKDAYS; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth((88.0) / WEEKDAYS);
            cc.setHgrow(Priority.SOMETIMES);
            timetableGrid.getColumnConstraints().add(cc);
        }

        // header row + sections rows
        RowConstraints header = new RowConstraints();
        header.setPrefHeight(40);
        header.setVgrow(Priority.NEVER);
        timetableGrid.getRowConstraints().add(header);

        for (int r = 0; r < TOTAL_SECTIONS; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setMinHeight(40); // 给每行一个最小高度，避免被压成 0
            timetableGrid.getRowConstraints().add(rc);
        }

        // Populate header labels
        Label timeHeader = new Label("");
        timetableGrid.add(timeHeader, 0, 0);
        String[] weekdayNames = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
        for (int i = 0; i < WEEKDAYS; i++) {
            Label l = new Label(weekdayNames[i]);
            l.getStyleClass().add("timetable-weekday");
            timetableGrid.add(l, i + 1, 0);
            GridPane.setHalignment(l, HPos.CENTER);
        }

        // row numbers in first column
        for (int s = 1; s <= TOTAL_SECTIONS; s++) {
            Label sec = new Label("第 " + s + " 节");
            sec.getStyleClass().add("timetable-section");
            timetableGrid.add(sec, 0, s);
            GridPane.setValignment(sec, VPos.CENTER);
            GridPane.setHalignment(sec, HPos.CENTER);
        }

        // 给 GridPane 一个合理的首选尺寸（方便百分比/行高起作用）
        timetableGrid.setPrefHeight(800);
    }


    private void renderTable() {
        if (viewModel == null) return;

        // DEBUG: 打印当前 selected 长度
        System.out.println("[MySchedule] renderTable() called. currentWeek=" + currentWeek
                + " selectedCount=" + (viewModel.myClasses.selected == null ? 0 : viewModel.myClasses.selected.size()));

        // 保留 header(row==0) 和第一列的节标签 (col==0 && row>=1)，移除其它所有单元格内容
        timetableGrid.getChildren().removeIf(node -> {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            int c = (col == null) ? 0 : col;
            int r = (row == null) ? 0 : row;
            // keep header row (r==0) and first column section labels (c==0 && r>=1)
            return !(r == 0 || (c == 0 && r >= 1));
        });

        List<TeachingClass> classes = viewModel.myClasses.selected;
        if (classes == null || classes.isEmpty()) {
            System.out.println("[MySchedule] no classes to render.");
            return;
        }

        for (TeachingClass tc : classes) {
            if (tc == null) continue;
            if (tc.getSchedule() == null) continue;
            for (Object raw : tc.getSchedule()) {
                if (!(raw instanceof Pair)) continue;
                Pair<?,?> p = (Pair<?,?>) raw;

                // week range
                int weekStart = 1, weekEnd = 1;
                Object first = p.getFirst();
                if (first instanceof Pair) {
                    Pair<?,?> wk = (Pair<?,?>) first;
                    if (wk.getFirst() instanceof Number) weekStart = ((Number) wk.getFirst()).intValue();
                    if (wk.getSecond() instanceof Number) weekEnd = ((Number) wk.getSecond()).intValue();
                }

                if (currentWeek < weekStart || currentWeek > weekEnd) continue;

                Object second = p.getSecond();
                if (!(second instanceof Pair)) continue;
                Pair<?,?> dayTime = (Pair<?,?>) second;

                int weekday = -1;
                if (dayTime.getFirst() instanceof Number) weekday = ((Number) dayTime.getFirst()).intValue();

                Object timePairObj = dayTime.getSecond();
                if (!(timePairObj instanceof Pair)) continue;
                Pair<?,?> timePair = (Pair<?,?>) timePairObj;

                int start = -1, end = -1;
                if (timePair.getFirst() instanceof Number) start = ((Number) timePair.getFirst()).intValue();
                if (timePair.getSecond() instanceof Number) end = ((Number) timePair.getSecond()).intValue();

                // DEBUG 打印解析结果，方便检查 coordinate 是否正确
                System.out.printf("[MySchedule] class=%s teacher=%s place=%s -> weekday=%d start=%d end=%d week[%d-%d]%n",
                        tc.getCourse() == null ? "?" : tc.getCourse().getCourseName(),
                        tc.getTeacherName(), tc.getPlace(), weekday, start, end, weekStart, weekEnd);

                // 验证值是否在期望范围 (weekday:1..7, start:1..TOTAL_SECTIONS)
                if (weekday < 1 || weekday > WEEKDAYS || start < 1 || start > TOTAL_SECTIONS || end < start) {
                    System.out.println("[MySchedule] schedule entry out of range, skipped.");
                    continue;
                }

                int colIndex = weekday; // 1..7 -> col 1..7
                int rowIndex = start;   // 1..TOTAL_SECTIONS -> row 1..TOTAL_SECTIONS
                int rowSpan = Math.min(TOTAL_SECTIONS - start + 1, end - start + 1);

                try {
                    // 资源路径使用相对同包查找（ClassItem.fxml 与此类在同一包）
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ClassItem.fxml"));
                    Node item = loader.load();
                    // 保护性判断 controller
                    Object controller = loader.getController();
                    if (controller instanceof ClassItemController) {
                        ClassItemController cic = (ClassItemController) controller;
                        String courseName = tc.getCourse() == null ? "?" : tc.getCourse().getCourseName();
                        cic.setData(courseName, tc.getTeacherName() == null ? "" : tc.getTeacherName(), tc.getPlace() == null ? "" : tc.getPlace());
                    }

                    // 放置节点
                    timetableGrid.add(item, colIndex, rowIndex, 1, rowSpan);
                    GridPane.setHalignment(item, HPos.CENTER);
                    GridPane.setValignment(item, VPos.CENTER);
                } catch (IOException e) {
                    System.err.println("[MySchedule] failed to load ClassItem.fxml: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception ex) {
                    System.err.println("[MySchedule] render item exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
}
