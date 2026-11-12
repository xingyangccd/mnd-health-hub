package com.mnd.healthhub;

import com.mnd.healthhub.db.mapper.SysUserMapper;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest
class MndHealthHubApplicationTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void test01() {
        Set<String> permissions = sysUserMapper.selectPermissionsByUserId(1);
        permissions.forEach(System.out::println);
    }

    // 测试QLExpress规则引擎的使用。
    @Test
    void test02() throws Exception {
        // 创建 表达式运行器 对象（该对象专门用来执行表达式）
        ExpressRunner runner = new ExpressRunner();

        // 编写规则字符串。(表达式字符串)
        String rule = """
                temp=salary-3500;
                tax=0;
                if(temp<=0){
                    tax=0;
                }
                else if(temp<=1500){
                    tax=temp*0.03-0;
                }
                else if(temp<=4500){
                    tax=temp*0.10-105;
                }
                else if(temp<=9000){
                    tax=temp*0.20-555;
                }
                else if(temp<=35000){
                    tax=temp*0.25-1005;
                }
                else if(temp<=55000){
                    tax=temp*0.30-2755;
                }
                else if(temp<=80000){
                    tax=temp*0.35-5505;
                }
                else{
                    tax=temp*0.45-13505;
                }
                return tax;
                """;

        // 创建规则执行的上下文对象，用于存储变量。
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.put("salary", 5500);

        // 执行规则。
        Object result = runner.execute(
                rule,       // 要执行的QLExpress脚本表达式字符串
                context,    // 表达式执行所需的变量上下文
                null,       // 错误监听器，null表示使用默认的错误处理
                true,       // 是否开启缓存，true表示：缓存表达式的解析结果
                false);     // 是否输出跟踪信息，false表示不输出详细执行日志

        System.out.println(result);
    }

    @Test
    void test03() throws Exception {
        // 执行次件特惠方案的规则。
        // 创建规则执行器对象。
        ExpressRunner runner = new ExpressRunner();

        // 编写规则
        String rule = """
                import java.math.BigDecimal;
                p = new BigDecimal(price);
                n = new BigDecimal(number);
                result = n.multiply(p).subtract(new BigDecimal(number / 2).multiply(new BigDecimal("0.5")).multiply(p)).toString();
                """;
        // 创建上下文对象
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.put("number", 3);
        context.put("price", 1000);

        Object r = runner.execute(rule, context, null, true, false);
        System.out.println(r);
    }

}
