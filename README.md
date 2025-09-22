# SEU - 2025 Summer School - VCampus


> [!NOTE]
>
> 本项目的代码中包含大量 AI 生成内容

本项目为东南大学 2023 级计算机科学与技术专业暑期学校专业技能实训项目 VCampus。

本项目参考了 https://github.com/JinBridger/SEU-SummerSchool-VCampus ，先驱的项目为我们节省了大量的时间。


## 环境要求

- `Java 17 (openjdk 17.0.16)`
- `MySQL 8.4.6 LTS`
- `JavaFX 17.0.16`
- `JFoenix for Java 9 `



> [!WARNING]
>
> 对于暑校的项目，如果可以，请使用 `Java 9` 之前的版本（比如 `Java 8` ），否则您可能会被一种叫做模块化的设计毒死。高版本的 `Java` 并没有节约我们时间的新功能，反倒徒增了我们的开发成本。

开发环境建议 `IntelliJ IDEA` + `Maven`。此外，我们不建议您在我们的屎山上继续开发。



## 如何打包

在项目根目录下运行  `mvn clean install package` 即可。更多信息可以参考 `doc/dev-guide` 下的文档。



## 示例：在 Arch Linux 中运行 release 中的 .jar 

> [!NOTE]
>
> 本章节仅供参考。本节的定位不是教程，所以不会交代全部细节。

> [!NOTE]
>
> 本节内容在 2025.9.22 被确认为有效。

### 环境

Kernel: Linux 6.16.7-arch1-1，DE: KDE Plasma 6.4.5，WM: KWin (Wayland)



### 配置MySQL

首先，根据 https://dev.mysql.com/downloads/mysql/ 和 https://dev.mysql.com/doc/refman/8.4/en/binary-installation.html 下载并部署 MySQL 服务，如果在执行 `<yourpath>/mysql/bin/mysql -u root -p` 的时候报错：

```bash
mysql: error while loading shared libraries: libncurses.so.6
```

且 `sudo pacman -S ncurses` 后没有解决问题的话，可以`sudo pacman -Syu ncurses`，而后根据查询到的信息挂一个软链接。对于我的设备，我执行 `sudo ln -s /usr/lib/libncursesw.so.6 /usr/lib/libncurses.so.6`。

配置好 MySQL 的 `root`  密码后，执行以下操作。

```bash
mysql -u root -p --default-character-set=utf8mb4 
# 这步与您是否配置了 mysql 的环境变量相关，若未配置，请用 <yourpath>/mysql/bin/mysql 替换 mysql
mysql> CREATE DATABASE vcampus; # 注意，这里数据库的名称必须是 vcampus 
mysql> USE vcampus;
mysql> SOURCE /path/to/file.sql;
mysql> SHOW TABLES; # 检查
```



### 安装 openjdk 和 JavaFX，下载 JFoenix 

`sudo pacman -S openjdk17-src`，而后参考 https://gluonhq.com/products/javafx/ ， https://openjfx.io/openjfx-docs/#install-javafx 安装和设置 `JavaFX` ，这里我选择 `Linux 17.0.16` 版本的 `JavaFX SDK`。

访问 https://github.com/sshahine/JFoenix?tab=readme-ov-file ，直接点击项目 README 中的 [download jar](https://search.maven.org/remotecontent?filepath=com/jfoenix/jfoenix/9.0.10/jfoenix-9.0.10.jar) ，下载 `JFoenix for Java 9 `。



### 运行 Server 和 Client 

请确保已经配置好 `mySQL`，在终端`java -jar vcampus-server-1.0.0.jar` 后，输入`mysql`的账号密码即可运行服务端。

对于客户端，请确保 `jfoenix-9.0.10.jar` 和 `vcampus-client-1.0.0.jar` 在同一个文件夹下。然后执行：

```bash
java --module-path $PATH_TO_FX:jfoenix-9.0.10.jar --add-modules javafx.controls,javafx.fxml --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED -cp vcampus-client-1.0.0.jar app.vcampus.client.Launcher
```

即可运行客户端。

您也许会注意到大量的 `--add-opens`，这是 `JFoenix` 的反射机制要求 `JavaFX` 的各类模块向它 *全部共享* 导致的。显然， `JFoenix` 是一个在四年前就停止维护的僵尸。若没有强制要求使用 `JavaFX` ，我建议您跳车至 `Jetpack Compose Desktop`。如果您很不幸的被强制要求使用 `JavaFX`，那么至少应该使用 `Java8` 而不是 `Java17` 。

客户端的管理员账号/密码为123456/123456。测试用账号密码还有213230000/123456（学生）和105000000/123456（教师）。
