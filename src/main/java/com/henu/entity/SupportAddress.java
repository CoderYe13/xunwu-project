package com.henu.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "support_address")
@Getter
@Setter
public class SupportAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//上一级行政单位
    @Column(name = "belong_to")
    private String belongTo;

    @Column(name = "en_name")
    private String enName;

    @Column(name = "cn_name")
    private String cnName;

    private String level;
    /**
     * 行政级别定义
     */
    public enum Level{
        CITY("city"),
        REGION("region");
        private String value;

        Level(String value) {
            this.value = value;
        }
        public String getValue(){
            return value;
        }

        public static Level of(String value){
            for(Level level:Level.values()){
                if(level.getValue().equals(level)){
                    return level;
                }
            }
            throw  new IllegalArgumentException();
        }
    }
}
