package cn.litblue.excel.thread;

import cn.litblue.excel.entity.Excel;
import cn.litblue.excel.mapper.ExcelJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author zhoucm
 * @time 2021/7/5  17:19
 * @description
 */
public class ThreadQuery implements Callable<List<Excel>> {

    private final ExcelJdbcTemplate excelJdbcTemplate;

    /** 当前页数 */
    private final int start;

    /** 每页查询多少条 */
    private final int rows;


    /**
     * 初始化
     * @param jdbcTemplate 传入jdbcTemplate
     * @param start  查询开始页
     * @param rows   查询记录数
     */
    public ThreadQuery(JdbcTemplate jdbcTemplate, int start, int rows) {
        this.start = start;
        this.rows = rows;

        excelJdbcTemplate = new ExcelJdbcTemplate(jdbcTemplate);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public List<Excel> call() throws Exception {
        //分页查询数据库数据
        return excelJdbcTemplate.selectExcel(start, rows);
    }
}
