package cn.litblue.excel.mapper;

import cn.litblue.excel.entity.Excel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author: litblue
 * @since: 2019/12/23 16:52
 */

public interface ExcelMapper extends Mapper<Excel> {

    /**
     *  批量插入 数据
     * @param excelList
     */
    @Insert({
            "<script>",
            " insert into `excel`(`name`, `gender`, `age`,`remark`, `uptime`) ",
            " values",
            " <foreach collection='excelList' item='excel' separator=','> ",
            "     (#{excel.name}, #{excel.gender}, #{excel.age}, #{excel.remark}, now()) ",
            " </foreach>",
            "</script>"
    })
    void insertBatch(@Param("excelList") List<Excel> excelList);

}
