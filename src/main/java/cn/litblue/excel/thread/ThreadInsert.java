package cn.litblue.excel.thread;

import cn.litblue.excel.entity.Excel;
import cn.litblue.excel.mapper.ExcelJdbcTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author: zhoucm
 * @time: 2021/7/5  20:31
 * @description 插入时不需要返回值， 使用实现 Runnable 接口的方式
 */

@Slf4j
public class ThreadInsert implements Runnable{

    private final ExcelJdbcTemplate excelJdbcTemplate;

    private final List<Excel> excelList;

    public ThreadInsert(JdbcTemplate jdbcTemplate, List<Excel> excelList){
        this.excelList = excelList;
        excelJdbcTemplate = new ExcelJdbcTemplate(jdbcTemplate);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        insertByBatch();
    }

    /**
     * 批量插入数据
     */
    private void insertByBatch(){
        excelJdbcTemplate.insertBatchByJdbcTemplate(excelList);
    }
}
