package com.mnd.healthhub.common;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体检报告Word文档生成工具
 * 功能：生成包含封面、致辞、个人信息、体检内容、体检结果的完整体检报告
 *
 * @author system
 */
@Slf4j
@Component
public class CheckupReportUtil {

    // ==================== 常量定义 ====================
    private static final String FONT_FAMILY = "Microsoft YaHei";
    private static final String HEADER_BG_COLOR = "f0f0f0";
    private static final int TABLE_WIDTH = 9850;
    private static final int ROW_HEIGHT = 550;
    private static final int HEADER_ROW_HEIGHT = 500;
    private static final int QR_CODE_SIZE = 150;

    // ==================== 通用工具方法 ====================

    /**
     * 设置段落行间距
     */
    private void setParagraphSpacing(XWPFParagraph paragraph, int line, int before, int after) {
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setLineRule(STLineSpacingRule.EXACT);
        ctSpacing.setLine(BigInteger.valueOf(line));
        if (before > 0) {
            ctSpacing.setBeforeLines(BigInteger.valueOf(before));
        }
        if (after > 0) {
            ctSpacing.setAfterLines(BigInteger.valueOf(after));
        }
    }

    /**
     * 创建标准表格
     */
    private XWPFTable createTable(XWPFDocument doc, int rows, int cols) {
        XWPFTable table = doc.createTable(rows, cols);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.getTblW().setType(STTblWidth.DXA);
        tblPr.getTblW().setW(BigInteger.valueOf(TABLE_WIDTH));
        return table;
    }

    /**
     * 设置单元格文本（表头样式）
     */
    private void setHeaderCellText(XWPFTableCell cell, String text, int width) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor(HEADER_BG_COLOR);
        XWPFParagraph paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily(FONT_FAMILY);
        run.setText(text);
        if (width > 0) {
            CTTcPr ctTcPr = cell.getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(width));
        }
    }

    /**
     * 设置单元格文本（居中样式）
     */
    private void setCenterCellText(XWPFTableCell cell, String text) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily(FONT_FAMILY);
        run.setText(text);
    }

    // ==================== 封面页 ====================

    /**
     * 创建报告封面页
     * 封面包含机构名称、报告标题、二维码、个人信息四要素，二维码用于报告唯一标识
     */
    private void createCover(XWPFDocument doc, HashMap<String, Object> param) throws Exception {
        log.info("开始生成封面页");

        // 1. 机构名称（中文）
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(20);
        run.setFontFamily(FONT_FAMILY);
        run.setUnderline(UnderlinePatterns.THICK);
        run.setText("天津美年大健康体检中心");
        setParagraphSpacing(paragraph, 480, 0, 0);

        // 2. 机构名称（英文）
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily(FONT_FAMILY);
        run.setText("Tianjin Meinian Health Checkup Center");
        setParagraphSpacing(paragraph, 280, 0, 0);

        // 3. 主标题
        paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(26);
        run.setFontFamily(FONT_FAMILY);
        run.setText("体检报告书");
        setParagraphSpacing(paragraph, 0, 800, 800);

        // 4. 二维码
        QrConfig qrConfig = new QrConfig();
        qrConfig.setWidth(QR_CODE_SIZE);
        qrConfig.setHeight(QR_CODE_SIZE);
        qrConfig.setMargin(2);
        String uuid = MapUtil.getStr(param, "uuid");
        BufferedImage bufferedImage = QrCodeUtil.generate(uuid, qrConfig);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", bout);
        InputStream input = new ByteArrayInputStream(bout.toByteArray());

        paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        setParagraphSpacing(paragraph, 0, 0, 400);
        run = paragraph.createRun();
        run.addPicture(input, Document.PICTURE_TYPE_JPEG, "", Units.pixelToEMU(bufferedImage.getWidth()), Units.pixelToEMU(bufferedImage.getHeight()));

        // 5. 个人信息表格（无边框）
        XWPFTable table = doc.createTable(4, 2);
        table.setTableAlignment(TableRowAlign.CENTER);
        table.getCTTbl().getTblPr().unsetTblBorders();

        @SuppressWarnings("unchecked") ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) param.get("item");
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            String label = MapUtil.getStr(map, "label");
            String value = MapUtil.getStr(map, "value");

            XWPFTableRow row = table.getRow(i);
            row.setHeight(600);
            List<XWPFTableCell> cells = row.getTableCells();

            // 第二列宽度
            CTTcPr ctTcPr = cells.get(1).getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(2800));

            // 标签
            XWPFParagraph p = cells.get(0).getParagraphArray(0);
            run = p.createRun();
            run.setFontFamily(FONT_FAMILY);
            run.setText(label + "    ");

            // 值（带下划线）
            p = cells.get(1).getParagraphArray(0);
            p.setBorderBottom(Borders.BABY_RATTLE);
            run = p.createRun();
            run.setFontFamily(FONT_FAMILY);
            run.setText("    " + value);
        }

        log.info("封面页生成完成");
    }

    // ==================== 欢迎致辞页 ====================

    /**
     * 创建欢迎致辞页面
     * 个性化称呼体现人文关怀，首行缩进符合正式文书规范
     */
    private void createWelcome(XWPFDocument doc, HashMap<String, Object> param) {
        log.info("开始生成欢迎致辞页");

        // 标题
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(18);
        run.setFontFamily(FONT_FAMILY);
        run.setText("健康体检报告");
        setParagraphSpacing(paragraph, 420, 2000, 200);

        // 个性化称呼
        String name = MapUtil.getStr(param, "name");
        String sex = MapUtil.getStr(param, "sex");
        String honorific = "男".equals(sex) ? "先生" : "女士";
        String greeting = "尊敬的" + name + honorific + "，您好！";

        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily(FONT_FAMILY);
        run.setText(greeting);
        setParagraphSpacing(paragraph, 380, 0, 50);

        // 正文
        paragraph = doc.createParagraph();
        paragraph.setIndentationFirstLine(600);
        run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily(FONT_FAMILY);
        run.setText("感谢您到天津美年大健康中心体检。现将您的体检结果汇总如下，请您认真阅读体检结果和建议。如有疑问，请您来院或者致电本中心服务电话022-88888888，我们将安排专业人员为您答疑解惑。欢迎对我们的工作提出批评和建议。祝您健康！");
        setParagraphSpacing(paragraph, 380, 0, 100);

        log.info("欢迎致辞页生成完成");
    }

    // ==================== 体检人信息表 ====================

    /**
     * 创建体检人信息表格
     * 4行6列布局，清晰展示体检人完整档案，灰色背景提升表头可读性
     */
    private void createCustomerInfo(XWPFDocument doc, HashMap<String, Object> param) {
        log.info("开始生成体检人信息表");

        // 标题
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily(FONT_FAMILY);
        run.setText("体检人信息");
        setParagraphSpacing(paragraph, 0, 200, 100);

        // 表格
        XWPFTable table = createTable(doc, 4, 6);

        // 行1：姓名、性别、出生日期
        XWPFTableRow row = table.getRow(0);
        row.setHeight(ROW_HEIGHT);
        List<XWPFTableCell> cells = row.getTableCells();

        String[] labels1 = {"姓名", "性别", "出生日期"};
        String[] values1 = {MapUtil.getStr(param, "name"), MapUtil.getStr(param, "sex"), MapUtil.getStr(param, "birthday")};

        for (int i = 0; i < 3; i++) {
            setHeaderCellText(cells.get(i * 2), labels1[i], 1500);
            setCenterCellText(cells.get(i * 2 + 1), values1[i]);
        }

        // 行2：电话、年龄、体检日期
        row = table.getRow(1);
        row.setHeight(ROW_HEIGHT);
        cells = row.getTableCells();

        String[] labels2 = {"电话", "年龄", "体检日期"};
        String[] values2 = {MapUtil.getStr(param, "tel"), MapUtil.getStr(param, "age"), MapUtil.getStr(param, "date")};

        for (int i = 0; i < 3; i++) {
            setHeaderCellText(cells.get(i * 2), labels2[i], 0);
            setCenterCellText(cells.get(i * 2 + 1), values2[i]);
        }

        // 行3：单位名称（跨列合并）
        row = table.getRow(2);
        row.setHeight(ROW_HEIGHT);
        cells = row.getTableCells();

        setHeaderCellText(cells.get(0), "单位名称", 0);
        XWPFTableCell cell = cells.get(1);
        cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph p = cell.getParagraphArray(0);
        p.setAlignment(ParagraphAlignment.LEFT);
        run = p.createRun();
        run.setFontSize(9);
        run.setFontFamily(FONT_FAMILY);
        run.setText("    " + param.get("company"));
        for (int i = 2; i <= 5; i++) {
            cells.get(i).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
        }

        // 行4：体检套餐（跨列合并）
        row = table.getRow(3);
        row.setHeight(600);
        cells = row.getTableCells();

        setHeaderCellText(cells.get(0), "体检套餐", 0);
        cell = cells.get(1);
        cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        p = cell.getParagraphArray(0);
        p.setAlignment(ParagraphAlignment.LEFT);
        run = p.createRun();
        run.setFontSize(9);
        run.setFontFamily(FONT_FAMILY);
        run.setText("    " + param.get("goods"));
        for (int i = 2; i <= 5; i++) {
            cells.get(i).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
        }

        log.info("体检人信息表生成完成");
    }

    // ==================== 体检内容表 ====================

    /**
     * 创建体检内容表格
     * 相同科室纵向合并，清晰呈现各科室下的体检项目清单
     */
    private void createCheckup(XWPFDocument doc, List<Map<String, String>> list) {
        if (list == null || list.isEmpty()) {
            log.warn("体检内容为空，跳过生成");
            return;
        }

        log.info("开始生成体检内容表，共{}项", list.size());

        // 标题
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily(FONT_FAMILY);
        run.setText("体检内容");
        setParagraphSpacing(paragraph, 0, 200, 100);

        // 表格
        XWPFTable table = createTable(doc, list.size() + 1, 3);

        // 表头
        XWPFTableRow row = table.getRow(0);
        row.setHeight(HEADER_ROW_HEIGHT);
        List<XWPFTableCell> cells = row.getTableCells();
        String[] headers = {"序号", "体检科室", "体检内容"};
        for (int i = 0; i < headers.length; i++) {
            setHeaderCellText(cells.get(i), headers[i], 0);
        }

        // 数据行
        String lastPlace = null;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> one = list.get(i);
            String place = MapUtil.getStr(one, "place");
            String item = MapUtil.getStr(one, "item");

            row = table.getRow(i + 1);
            row.setHeight(ROW_HEIGHT);
            cells = row.getTableCells();

            STMerge.Enum mergeVal;
            if (!place.equals(lastPlace)) {
                index++;
                lastPlace = place;
                mergeVal = STMerge.RESTART;
            } else {
                mergeVal = STMerge.CONTINUE;
            }

            // 序号（纵向合并）
            XWPFTableCell cell = cells.get(0);
            cell.getCTTc().addNewTcPr().addNewVMerge().setVal(mergeVal);
            setCenterCellText(cell, String.valueOf(index));

            // 科室（纵向合并）
            cell = cells.get(1);
            cell.getCTTc().addNewTcPr().addNewVMerge().setVal(mergeVal);
            setCenterCellText(cell, place);

            // 体检内容
            setCenterCellText(cells.get(2), item);
        }

        log.info("体检内容表生成完成");
    }

    // ==================== 体检结果模板1 ====================

    /**
     * 模板1：标准体检结果表格
     * 适用于数值型检查结果，包含检查项目、结果、单位、参考值四列
     */
    private void createCheckupResultByTemplate1(XWPFDocument doc, Map<String, Object> param) {
        String place = MapUtil.getStr(param, "place");
        @SuppressWarnings("unchecked") List<Map<String, String>> item = (List<Map<String, String>>) param.get("item");
        String doctor = MapUtil.getStr(param, "doctorName");
        String date = MapUtil.getStr(param, "date");

        if (item == null || item.isEmpty()) {
            log.warn("【{}】体检结果数据为空，跳过生成", place);
            return;
        }

        log.info("模板1生成【{}】体检结果，共{}项指标", place, item.size());

        // 标题
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setBorderBottom(Borders.BABY_RATTLE);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily(FONT_FAMILY);
        run.setTextHighlightColor("lightGray");
        run.setText("【" + place + "体检结果】");
        setParagraphSpacing(paragraph, 0, 250, 150);

        // 表格
        XWPFTable table = createTable(doc, item.size() + 1, 5);

        // 表头
        XWPFTableRow row = table.getRow(0);
        row.setHeight(HEADER_ROW_HEIGHT);
        List<XWPFTableCell> cells = row.getTableCells();

        String[][] headerConfig = {{"序号", "800"}, {"检查项目", "2200"}, {"检查结果", "3000"}, {"单位", "1200"}, {"参考值", "1300"}};

        for (int i = 0; i < headerConfig.length; i++) {
            XWPFTableCell cell = cells.get(i);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            cell.setColor(HEADER_BG_COLOR);
            XWPFParagraph p = cell.getParagraphArray(0);
            if (i != 2) {
                p.setAlignment(ParagraphAlignment.CENTER);
            } else {
                p.setIndentationLeft(200);
            }
            run = p.createRun();
            run.setFontSize(9);
            run.setFontFamily(FONT_FAMILY);
            run.setText(headerConfig[i][0]);

            CTTcPr ctTcPr = cell.getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(Integer.parseInt(headerConfig[i][1])));
        }

        // 数据行
        for (int i = 0; i < item.size(); i++) {
            Map<String, String> map = item.get(i);
            String name = MapUtil.getStr(map, "name");
            String value = MapUtil.getStr(map, "value");
            String unit = MapUtil.getStr(map, "unit", "");
            String standard = MapUtil.getStr(map, "standard", "");

            String[] data = {String.valueOf(i + 1), name, value, unit, standard};
            row = table.getRow(i + 1);
            row.setHeight(ROW_HEIGHT);
            cells = row.getTableCells();

            for (int j = 0; j < data.length; j++) {
                XWPFTableCell cell = cells.get(j);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                XWPFParagraph p = cell.getParagraphArray(0);
                if (j != 2) {
                    p.setAlignment(ParagraphAlignment.CENTER);
                } else {
                    p.setIndentationLeft(200);
                }
                run = p.createRun();
                run.setFontSize(9);
                run.setFontFamily(FONT_FAMILY);
                run.setText(data[j]);
            }
        }

        // 医生签名
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily(FONT_FAMILY);
        run.setText("体检医生：" + doctor + "\t\t\t\t\t\t\t\t\t日期：" + date);
        setParagraphSpacing(paragraph, 0, 100, 150);

        log.info("【{}】模板1生成完成", place);
    }

    // ==================== 体检结果模板2 ====================

    /**
     * 模板2：带图片的体检结果表格
     * 适用于影像学检查，图文并茂展示诊断结果，结果支持#分隔换行
     */
    @SneakyThrows
    private void createCheckupResultByTemplate2(XWPFDocument doc, Map<String, Object> param) {
        String place = MapUtil.getStr(param, "place");
        @SuppressWarnings("unchecked") List<Map<String, String>> item = (List<Map<String, String>>) param.get("item");
        String doctor = MapUtil.getStr(param, "doctorName");
        String date = MapUtil.getStr(param, "date");
        String image = MapUtil.getStr(param, "image");

        if (item == null || item.isEmpty()) {
            log.warn("【{}】体检结果数据为空，跳过生成", place);
            return;
        }

        log.info("模板2生成【{}】体检结果，共{}项描述", place, item.size());

        // 标题
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setBorderBottom(Borders.BABY_RATTLE);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily(FONT_FAMILY);
        run.setTextHighlightColor("lightGray");
        run.setText("【" + place + "体检结果】");
        setParagraphSpacing(paragraph, 0, 250, 150);

        // 插入图片
        if (image != null && !image.isEmpty()) {
            log.debug("加载影像图片: {}", image);
            URL url = new URL(image);
            BufferedImage bufferedImage = ImageIO.read(url);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            double scaling = 1.0;
            if (width > 72 * 9.13) {
                scaling = (72.0 * 9.13) / width;
            }

            paragraph = doc.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            setParagraphSpacing(paragraph, 0, 0, 100);

            try (InputStream in = url.openStream()) {
                run = paragraph.createRun();
                run.addPicture(in, Document.PICTURE_TYPE_JPEG, "", Units.pixelToEMU((int) (width * scaling)), Units.pixelToEMU((int) (height * scaling)));
            }
        }

        // 表格
        XWPFTable table = createTable(doc, item.size() + 1, 5);

        // 表头
        XWPFTableRow row = table.getRow(0);
        row.setHeight(HEADER_ROW_HEIGHT);
        List<XWPFTableCell> cells = row.getTableCells();

        String[][] headerConfig = {{"序号", "800"}, {"检查项目", "2200"}, {"检查结果", "3000"}, {"单位", "1200"}, {"参考值", "1300"}};

        for (int i = 0; i < headerConfig.length; i++) {
            XWPFTableCell cell = cells.get(i);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            cell.setColor(HEADER_BG_COLOR);
            XWPFParagraph p = cell.getParagraphArray(0);
            if (i != 2) {
                p.setAlignment(ParagraphAlignment.CENTER);
            } else {
                p.setIndentationLeft(200);
            }
            run = p.createRun();
            run.setFontSize(9);
            run.setFontFamily(FONT_FAMILY);
            run.setText(headerConfig[i][0]);

            CTTcPr ctTcPr = cell.getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(Integer.parseInt(headerConfig[i][1])));
        }

        // 数据行
        for (int i = 0; i < item.size(); i++) {
            Map<String, String> map = item.get(i);
            String name = MapUtil.getStr(map, "name");
            String value = MapUtil.getStr(map, "value");
            String unit = MapUtil.getStr(map, "unit", "");
            String standard = MapUtil.getStr(map, "standard", "");

            row = table.getRow(i + 1);
            row.setHeight(ROW_HEIGHT);
            cells = row.getTableCells();

            setCenterCellText(cells.get(0), String.valueOf(i + 1));
            setCenterCellText(cells.get(1), name);

            // 检查结果（支持#换行）
            XWPFTableCell cell = cells.get(2);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            XWPFParagraph p = cell.getParagraphArray(0);
            p.setAlignment(ParagraphAlignment.LEFT);
            p.setIndentationLeft(200);
            run = p.createRun();
            run.setFontSize(9);
            run.setFontFamily(FONT_FAMILY);
            String[] lines = value.split("#");
            for (int k = 0; k < lines.length; k++) {
                run.setText(lines[k]);
                if (k < lines.length - 1) {
                    run.addBreak();
                }
            }

            setCenterCellText(cells.get(3), unit);
            setCenterCellText(cells.get(4), standard);
        }

        // 医生签名
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily(FONT_FAMILY);
        run.setText("体检医生：" + doctor + "\t\t\t\t\t\t\t\t\t日期：" + date);
        setParagraphSpacing(paragraph, 0, 100, 150);

        log.info("【{}】模板2生成完成", place);
    }

    // ==================== 核心入口方法 ====================

    /**
     * 生成Word体检报告
     * 核心入口，协调各模块按顺序组装完整报告
     *
     * @param map 报告数据
     * @return Word文档对象
     */
    public XWPFDocument createReport(HashMap<String, Object> map) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("========== 开始生成体检报告 ==========");

        // 1. 创建文档
        XWPFDocument doc = new XWPFDocument();

        // 2. 设置页面边距
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setLeft(BigInteger.valueOf(1200));
        pageMar.setRight(BigInteger.valueOf(1200));
        pageMar.setTop(BigInteger.valueOf(1000));
        pageMar.setBottom(BigInteger.valueOf(1000));

        // 3. 设置页脚页码
        XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph footerParagraph = footer.createParagraph();
        footerParagraph.setAlignment(ParagraphAlignment.CENTER);
        footerParagraph.getCTP().addNewFldSimple().setInstr("PAGE \\* MERGEFORMAT");
        footerParagraph.createRun();

        // 4. 提取数据
        String uuid = MapUtil.getStr(map, "uuid");
        String name = MapUtil.getStr(map, "name");
        String sex = MapUtil.getStr(map, "sex");
        String tel = MapUtil.getStr(map, "tel");
        String birthday = MapUtil.getStr(map, "birthday");
        String age = MapUtil.getStr(map, "age");
        String company = MapUtil.getStr(map, "company");
        String date = MapUtil.getStr(map, "date");
        String goods = MapUtil.getStr(map, "goods");
        @SuppressWarnings("unchecked") List<Map<String, String>> checkup = (List<Map<String, String>>) map.get("checkup");
        @SuppressWarnings("unchecked") List<Map<String, Object>> result = (List<Map<String, Object>>) map.get("result");

        log.info("报告数据 - 姓名: {}, 体检日期: {}, 体检项目数: {}, 结果科室数: {}", name, date, checkup != null ? checkup.size() : 0, result != null ? result.size() : 0);

        // 5. 构建封面参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("uuid", uuid);
        param.put("name", name);
        param.put("sex", sex);
        param.put("birthday", birthday);
        param.put("age", age);
        param.put("tel", tel);
        param.put("date", date);
        param.put("company", company);
        param.put("goods", goods);

        List<HashMap<String, String>> itemList = new ArrayList<>();
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("label", "姓    名：");
        item1.put("value", name);
        itemList.add(item1);

        HashMap<String, String> item2 = new HashMap<>();
        item2.put("label", "性    别：");
        item2.put("value", sex);
        itemList.add(item2);

        HashMap<String, String> item3 = new HashMap<>();
        item3.put("label", "单    位：");
        item3.put("value", company);
        itemList.add(item3);

        HashMap<String, String> item4 = new HashMap<>();
        item4.put("label", "日    期：");
        item4.put("value", date);
        itemList.add(item4);

        param.put("item", itemList);

        // 6. 按顺序构建报告
        createCover(doc, param);
        createWelcome(doc, param);
        createCustomerInfo(doc, param);
        createCheckup(doc, checkup);

        // 7. 生成体检结果
        if (result != null && !result.isEmpty()) {
            for (Map<String, Object> one : result) {
                String template = MapUtil.getStr(one, "template");
                if ("模板1".equals(template)) {
                    createCheckupResultByTemplate1(doc, one);
                } else if ("模板2".equals(template)) {
                    createCheckupResultByTemplate2(doc, one);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("========== 体检报告生成完成，耗时: {}ms ==========", (endTime - startTime));
        return doc;
    }

    // ==================== 测试入口 ====================

    public static void main(String[] args) {
        try {
            log.info("========== 开始测试 ==========");

            // 构建测试数据
            HashMap<String, Object> reportData = new HashMap<>();
            reportData.put("uuid", "1234567890");
            reportData.put("name", "张三");
            reportData.put("sex", "男");
            reportData.put("tel", "13800138000");
            reportData.put("birthday", "1985-05-15");
            reportData.put("age", "38");
            reportData.put("company", "极课未来");
            reportData.put("date", "2025-01-15");
            reportData.put("goods", "精英体检套餐");

            // 体检内容
            List<Map<String, String>> checkupList = new ArrayList<>();
            Map<String, String> c1 = new HashMap<>();
            c1.put("place", "内科");
            c1.put("item", "心肺听诊、血压测量");
            checkupList.add(c1);

            Map<String, String> c2 = new HashMap<>();
            c2.put("place", "内科");
            c2.put("item", "腹部触诊");
            checkupList.add(c2);

            Map<String, String> c3 = new HashMap<>();
            c3.put("place", "外科");
            c3.put("item", "甲状腺检查");
            checkupList.add(c3);

            Map<String, String> c4 = new HashMap<>();
            c4.put("place", "眼科");
            c4.put("item", "视力检查、眼底检查");
            checkupList.add(c4);

            reportData.put("checkup", checkupList);

            // 体检结果 - 模板1
            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> r1 = new HashMap<>();
            r1.put("place", "血常规");
            r1.put("doctorName", "李医生");
            r1.put("date", "2025-01-15");
            r1.put("template", "模板1");

            List<Map<String, String>> items1 = new ArrayList<>();
            Map<String, String> i1 = new HashMap<>();
            i1.put("name", "白细胞计数");
            i1.put("value", "6.5");
            i1.put("unit", "×10^9/L");
            i1.put("standard", "4.0-10.0");
            items1.add(i1);

            Map<String, String> i2 = new HashMap<>();
            i2.put("name", "红细胞计数");
            i2.put("value", "4.8");
            i2.put("unit", "×10^12/L");
            i2.put("standard", "4.3-5.8");
            items1.add(i2);

            Map<String, String> i3 = new HashMap<>();
            i3.put("name", "血红蛋白");
            i3.put("value", "145");
            i3.put("unit", "g/L");
            i3.put("standard", "130-175");
            items1.add(i3);

            r1.put("item", items1);
            resultList.add(r1);

            // 体检结果 - 模板2
            Map<String, Object> r2 = new HashMap<>();
            r2.put("place", "超声检查");
            r2.put("doctorName", "王医生");
            r2.put("date", "2025-01-15");
            r2.put("template", "模板2");
            r2.put("image", "https://n.sinaimg.cn/spider20230206/110/w390h520/20230206/87ec-45ac7736db91b1d07891e0fb99e2c915.png");

            List<Map<String, String>> items2 = new ArrayList<>();
            Map<String, String> j1 = new HashMap<>();
            j1.put("name", "肝脏");
            j1.put("value", "形态正常#大小正常#回声均匀");
            j1.put("unit", "");
            j1.put("standard", "");
            items2.add(j1);

            Map<String, String> j2 = new HashMap<>();
            j2.put("name", "胆囊");
            j2.put("value", "壁光滑#腔内未见异常回声");
            j2.put("unit", "");
            j2.put("standard", "");
            items2.add(j2);

            r2.put("item", items2);
            resultList.add(r2);

            reportData.put("result", resultList);

            // 生成报告
            CheckupReportUtil reportUtil = new CheckupReportUtil();
            XWPFDocument document = reportUtil.createReport(reportData);

            String filePath = "d:/体检报告.doc";
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
                log.info("体检报告生成成功！文件保存位置：{}", filePath);
            }

            document.close();
            log.info("========== 测试完成 ==========");

        } catch (Exception e) {
            log.error("生成体检报告时发生错误", e);
        }
    }
}