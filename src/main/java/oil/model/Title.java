package oil.model;

import lombok.Data;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by  waiter on 18-9-18  上午10:51.
 *
 * @author waiter
 */
@Entity
@Data
public class Title {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String mainTitle;
    private String subtitle;
    /**
     * 简介
     */
    private String introduction;
}
