package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Entity
@Data
@Table(name = "gpt")
@Slf4j
public class GptContext implements IEntity {
    @Id
    public Integer cardNumber = 0;

    @Lob
    @Column
    public String context;
}
