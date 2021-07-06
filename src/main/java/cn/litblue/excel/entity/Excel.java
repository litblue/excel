package cn.litblue.excel.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author: litblue
 * @since: 2019/12/23 16:47
 */

@Data
public class Excel implements Serializable {

    @Id
    private Integer id;

    @ExcelProperty(value ="name",index = 0)
    private String name;

    @ExcelProperty(value ="gender",index = 1)
    private String gender;

    @ExcelProperty(value ="age",index = 2)
    private Integer age;

    @ExcelProperty(value ="remark",index = 3)
    private String remark;

    private Timestamp upTime;
}
