import html2Canvas from "html2canvas";
import jsPDF from "jspdf";

/**
 * 生成PDF（将指定DOM元素截图导出为PDF文件）
 * @param elementId - 要截图的DOM元素的ID（不含#号）
 */
export const getPdf = (elementId: string) => {
  // 1. 获取要截图的DOM元素
  const imageWrapper = document.querySelector(`#${elementId}`) as HTMLElement;
  if (!imageWrapper) {
    console.error(`未找到 #${elementId} 元素`);
    return;
  }

  // 2. 重置页面滚动条（避免因滚动导致截图内容不完整）
  document.documentElement.scrollTop = 0;
  document.documentElement.scrollLeft = 0;
  document.body.scrollTop = 0;
  document.body.scrollLeft = 0;

  // 3. 延迟执行，确保DOM渲染完成
  setTimeout(() => {
    // 4. 将DOM元素转换为Canvas（截图）
    html2Canvas(imageWrapper, {
      allowTaint: true, // 允许跨域图片
      taintTest: false, // 不测试跨域
      useCORS: true, // 使用CORS加载图片
      dpi: window.devicePixelRatio * 4, // 提高DPI，提升清晰度
      scale: 4, // 4倍缩放，保证高清输出
    } as any)
      .then((canvas) => {
        // 5. 创建PDF实例（A4纸，纵向，单位mm）
        const pdf = new jsPDF("p", "mm", "a4");
        // 6. 将Canvas转为JPEG图片数据（质量1.0最高）
        const imgData = canvas.toDataURL("image/jpeg", 1.0);

        // 7. 计算图片在PDF中的尺寸（左右各留10mm边距，宽190mm）
        const pdfWidth = 190;
        const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
        const pageHeight = pdf.internal.pageSize.height;

        // 8. 分页处理：如果图片高度超过一页，自动分页
        let heightLeft = pdfHeight;
        let position = 0;

        // 第一页
        pdf.addImage(imgData, "JPEG", 10, position, pdfWidth, pdfHeight);
        heightLeft -= pageHeight;

        // 后续页（如果内容超过一页）
        while (heightLeft > 0) {
          position = heightLeft - pdfHeight;
          pdf.addPage(); // 新增空白页
          pdf.addImage(imgData, "JPEG", 10, position, pdfWidth, pdfHeight);
          heightLeft -= pageHeight;
        }

        // 9. 获取文件名并保存
        const title = imageWrapper.getAttribute("name") || "体检报告";
        pdf.save(`${title}.pdf`);
      })
      .catch((error) => {
        console.error("PDF生成失败：", error);
      });
  }, 100);
};
