<template>
    <!-- 外层的div：存放404图片的 -->
    <div class="content">
        <!-- 内部div，显示一个提示信息：哎呦，页面走丢了~ -->
        <div class="title">
            哎呦，页面走丢了~
        </div>
    </div>
</template>

<script lang='ts' setup>
</script>

<style lang="less" scoped>

// 使用less语法编写css样式。
// 父控件
.content {
    // less语法允许嵌套（和页面元素关系对应上就行）
    // 在css中是不允许的。

    // 设置背景图片
    background-image: url('@/assets/404.jpg');
    // 让整个背景图片充满div父容器
    background-size: cover;
    // 设置div的高度
    height: 100%;

    .title { // 子控件可以自动继承父控件的样式(前提是：这些样式是支持继承的(text-align样式支持继承)。有一些样式不支持继承。)
        text-align: center;
        font-size: 28px;
        color: #888;
        padding-top: 150px;
    }
}

</style>