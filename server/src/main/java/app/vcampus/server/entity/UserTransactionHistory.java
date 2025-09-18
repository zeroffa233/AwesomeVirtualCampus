// 文件位置: server/src/main/java/app/vcampus/server/entity/UserTransactionHistory.java
package app.vcampus.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor // Lombok注解，生成无参构造函数
@Table(name = "user_transaction_history")
public class UserTransactionHistory implements IEntity {

    @Id
    @Column(name = "card_num", nullable = false)
    private Integer cardNum; // 用户一卡通号，作为主键

    @Lob // 表示这是一个大文本对象
    @Column(name = "history_json", nullable = false, columnDefinition = "TEXT")
    private String historyJson; // 存储交易历史列表的JSON字符串

    public UserTransactionHistory(Integer cardNum, String historyJson) {
        this.cardNum = cardNum;
        this.historyJson = historyJson;
    }
}