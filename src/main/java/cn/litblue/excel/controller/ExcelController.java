package cn.litblue.excel.controller;

import cn.litblue.excel.service.ExcelService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

/**
 * @author: litblue
 * @since: 2019/12/23 16:53
 */

@RestController
@RequestMapping("excel")
public class ExcelController {

    @Resource
    private ExcelService excelService;

    /**
     * 导入excel
     * @param file  文件
     * @return 导入结果
     */
    @PostMapping("import")
    public String importExcelByEasyExcel(@RequestParam(value = "file", required = false) MultipartFile file){

        if (file == null){
            return "请先上传文件";
        }

        // 调用 service 方法，返回导入是否成功的结果
        boolean flag = excelService.importExcelByEasyExcel(file);

        return flag ? "导入成功"  : "导入失败" ;

    }

    /**
     * 导出Excel
     * @param response response
     * @param filename  导出文件名，默认 xlsx 文件
     */
    @GetMapping("export")
    public void exportExcelByEasyExcel(HttpServletResponse response, String filename) throws ExecutionException, InterruptedException {
        excelService.exportExcelByEasyExcel(response,filename);
    }
}
