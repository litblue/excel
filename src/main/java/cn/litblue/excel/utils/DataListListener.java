package cn.litblue.excel.utils;

import cn.litblue.excel.entity.Excel;
import cn.litblue.excel.mapper.ExcelJdbcTemplate;
import cn.litblue.excel.mapper.ExcelMapper;
import cn.litblue.excel.thread.ThreadInsert;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 根据作者所说：
 * 这里的 DataListListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
 *
 * @author: litblue
 * @since: 2019/12/23 18:42
 */

@Slf4j
public class DataListListener extends AnalysisEventListener<Excel> {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;

    /** 通过ThreadPoolExecutor构造函数自定义参数创建 */
    private ThreadPoolExecutor executor;

    /**
     * 每隔 ${BATCH_LIMIT} 条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_LIMIT = 5;

    /** 每一批次插入的数据 */
    private final static int BATCH_SIZE = 2;

    /**
     * 待插入的数据
     * 由于每次读都是新new DataListListener 的，所以这个list不会存在线程安全问题
     */
    private final List<Excel> excelList = new ArrayList<>();


    private final JdbcTemplate jdbcTemplate;


    /**
     * 自动注入的是null，所以通过构造器初始化 jdbcTemplate
     * @param jdbcTemplate
     */
    public DataListListener(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param excel   表格内字段映射实体
     * @param analysisContext  分析器
     */
    @Override
    public void invoke(Excel excel, AnalysisContext analysisContext) {
        excelList.add(excel);

        // 达到 ${BATCH_LIMIT} 了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (excelList.size() >= BATCH_LIMIT) {
            saveExcelByThreadPool();

            // 存储完成，清理list
            excelList.clear();
        }
    }

    /**
     * 所有数据解析完成了，会来调用
     * 这里处理的是分批剩下的最后一批数据.
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveExcelByThreadPool();

        log.info("所有数据处理完成~");
    }

    /**
     * 通过线程池插入数据
     */
    private void saveExcelByThreadPool(){
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        log.info("----{},   ---{}", Thread.currentThread().getName(), excelList);

        // 批量插入的次数
        int times = (int)Math.ceil((double) excelList.size() / BATCH_SIZE) ;

        for (int i = 0; i<times; i++){
            int start = i * BATCH_SIZE;
            int end = Math.min((i + 1) * BATCH_SIZE, excelList.size());

            executor.execute(new ThreadInsert(jdbcTemplate, excelList.subList(start, end)));

            log.info("执行完 ---- {}--线程", Thread.currentThread().getName());
        }

        // 关闭线程池
       executor.shutdown();

       log.info("Finished all threads");
    }
}
