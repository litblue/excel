package cn.litblue.excel.service;

import cn.litblue.excel.entity.Excel;
import cn.litblue.excel.utils.DataListListener;
import cn.litblue.excel.thread.ThreadQuery;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author litblue
 * @since 2019/12/23 16:52
 */

@Slf4j
@Service
public class ExcelService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;


    /**
     * 读取Excel，并写入数据库
     * @param file
     * @return 是否成功导入数据库
     */
    public boolean importExcelByEasyExcel(MultipartFile file){
        try {
            // 读取所有工作表
            EasyExcel.read(file.getInputStream(), Excel.class, new DataListListener(jdbcTemplate)).doReadAll();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 导出excel
     */
    public void exportExcelByEasyExcel(HttpServletResponse response, String filename) throws ExecutionException, InterruptedException {

        // 通过多线程方式查询所需导出的数据
        List<Excel> excelList = queryExcelByThread();

        try {
            // 将数据写入excel
            ExcelWriter writer = EasyExcel.write(getOutputStream(filename, response),Excel.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
            writer.write(excelList, writeSheet);

            // 千万别忘记finish 会帮忙关闭流
            writer.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出文件头信息 设置
     *
     * @param filename 文件名
     * @param response response
     * @return 返回流文件
     */
    public static OutputStream getOutputStream(String filename, HttpServletResponse response) throws Exception {
        try {
            filename = URLEncoder.encode(filename, "UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "no-store");
            response.addHeader("Cache-Control", "max-age=0");
            return response.getOutputStream();
        } catch (IOException e) {
            throw new Exception("导出excel表格失败!", e);
        }
    }


    /**
     * 查询数据
     * @return excel数据列表
     */
    public List<Excel> queryExcelByThread() throws InterruptedException, ExecutionException {
        List<Excel> excelList = new ArrayList<>();

        // 查询记录总数
        int count = 10;

        // 一次查询多少条
        int rows = 2;

        //需要查询的次数
        int times = count / rows;

        // 初始记录行的偏移量
        int start = 0;

        // 通过ThreadPoolExecutor构造函数自定义参数创建
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 目标数据开始放入数据的下标
        for (int i=0;i<times;i++){
            Callable<List<Excel>> listCallable = new ThreadQuery(jdbcTemplate,start,rows);
            Future<List<Excel>> future = executor.submit(listCallable);

            excelList.addAll(future.get());
            start += rows;
        }

        //终止线程池
        executor.shutdown();

        log.info("Finished all threads");

        return excelList;
    }
}
