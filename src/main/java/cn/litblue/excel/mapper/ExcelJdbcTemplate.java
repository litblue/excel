package cn.litblue.excel.mapper;

import cn.litblue.excel.entity.Excel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author litblue
 * @since 2019/12/23 19:31
 */

@Slf4j
@Repository
public class ExcelJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 初始化
     * @param jdbcTemplate
     */
    public ExcelJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 批量插入
     * @param excelList
     */
    public void insertBatchByJdbcTemplate(List<Excel> excelList){
        String prefix = "insert into `excel`(`name`, `gender`, `age`, `remark`, `uptime`) values ";

        StringBuilder suffix  = new StringBuilder();


        try {
            for (Excel excel : excelList) {
                // 需要注意根据 字段的类型修改 单引号 '
                suffix .append("('").append(excel.getName()).append("','");
                suffix .append(excel.getGender()).append("',");
                suffix .append(excel.getAge()).append(",'");
                suffix .append(excel.getRemark()).append("',");
                suffix .append("now()),");
            }
        } catch (java.util.ConcurrentModificationException e){
            e.printStackTrace();
        }

        // 需要去除最后一个 ','
        jdbcTemplate.batchUpdate(prefix+suffix.substring(0,suffix.length()-1));
    }


    /**
     * 分页查询
     * @param start
     * @param rows
     * @return
     */
    public List<Excel> selectExcel(Integer start, Integer rows){

        String sql = "select `name`,`gender`,`age`,`remark` from `excel` " +
                "limit ?,? ";

        return jdbcTemplate.query(sql,new Object[]{start,rows}, new BeanPropertyRowMapper<>(Excel.class));
    }
}
