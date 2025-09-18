package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 政治面貌枚举。
 */
@Getter
public enum PoliticalStatus implements LabelledEnum {
    /**
     * 中国共产党党员。
     */
    CommunistPartyOfChina("中国共产党党员"),

    /**
     * 中国共产党预备党员。
     */
    ProbationaryPartyMember("中国共产党预备党员"),

    /**
     * 中国共产主义青年团团员。
     */
    CommunistYouthLeagueMember("中国共产主义青年团团员"),

    /**
     * 群众。
     */
    Masses("群众"),

    /**
     * 民主党派。
     */
    MDCMember("民主党派");

    /**
     * 枚举的中文标签。
     */
    private String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    PoliticalStatus(String label) {
        this.label = label;
    }

}