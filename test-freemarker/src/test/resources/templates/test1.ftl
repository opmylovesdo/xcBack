<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
<!--test-->
<#--Hello ${name}!-->
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#--<#list stus as stu>
        <td>${stu_index + 1}</td>
        <td>${stu.name}</td>
        <td>${stu.age}</td>
        <td>${stu.mondy}</td>
        </tr>
    </#list>-->

    <#if stus??>
        <#list stus as stu>
            <tr <#if stu.name=='小明'>style="background: red"</#if>>
                <td>${stu_index + 1}</td>
                <td>${stu.name!}</td>
                <td>${stu.age}</td>
                <td>${stu.money}</td>
                <td>${(stu.birthday?string("yyyy年MM月"))!'没有'}</td>
            </tr>
        </#list>
    </#if>
    <#--<#assign text="{'bank':'工商银行','account':'10101920201920212'}" />-->
    <#--<#assign data=text?eval />-->
    <#--开户行：${data.bank}  账号：${data.account}-->

    <#--输出stu1的学生信息：<br/>-->
    <#--姓名：${stuMap['stu1'].name}<br/>-->
    <#--年龄：${stuMap['stu1'].age}<br/>-->
    <#--输出stu1的学生信息：<br/>-->
    <#--姓名：${stuMap.stu1.name}<br/>-->
    <#--年龄：${stuMap.stu1.age}<br/>-->

    <#--${stuMap['stu1'].name}<br>-->
    <#--${stuMap['stu1'].age}<br>-->

    <#--${stuMap.stu1.name}<br>-->
    <#--${stuMap.stu1.age}<br>-->

    <#--遍历输出两个学生信息：<br/>-->
    <#--<table>-->
        <#--&lt;#&ndash;<#list stuMap?keys as k>&ndash;&gt;-->
        <#--&lt;#&ndash;<tr>&ndash;&gt;-->
        <#--&lt;#&ndash;<td>${k_index + 1}</td>&ndash;&gt;-->
        <#--&lt;#&ndash;<td>${stuMap[k].name}</td>&ndash;&gt;-->
        <#--&lt;#&ndash;<td>${stuMap[k].age}</td>&ndash;&gt;-->
        <#--&lt;#&ndash;<td>${stuMap[k].mondy}</td>&ndash;&gt;-->
        <#--&lt;#&ndash;</tr>&ndash;&gt;-->
        <#--&lt;#&ndash;</#list>&ndash;&gt;-->


        <#--&lt;#&ndash;<#list stuMap?values as v>&ndash;&gt;-->
        <#--&lt;#&ndash;<tr>&ndash;&gt;-->
        <#--&lt;#&ndash;<td>${v}</td>&ndash;&gt;-->
        <#--&lt;#&ndash;</tr>&ndash;&gt;-->
        <#--&lt;#&ndash;</#list>&ndash;&gt;-->
    <#--</table>-->
</body>
</html>