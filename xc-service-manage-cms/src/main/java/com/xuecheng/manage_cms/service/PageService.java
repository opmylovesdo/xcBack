package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.netflix.discovery.converters.Auto;
import com.netflix.ribbon.proxy.annotation.Http;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @Auther: LUOLUO
 * @Date: 2019/5/1
 * @Description: com.xuecheng.manage_cms.service
 * @version: 1.0
 */
@Service
@Transactional
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private HttpServletRequest request;

    /**
     * 页面发布
     *
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId) {
        //执行静态化 得到html字符串
        String pageHtml = this.getPageHtml(pageId);

        //保存静态化文件到gridfs
        saveHtml(pageId, pageHtml);

        //给MQ发送消息
        sendPostPage(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //发送格式{"pageId":""}
    private void sendPostPage(String pageId) {
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();

        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("pageId", pageId);
        //消息内容
        String msg = JSON.toJSONString(msgMap);
        //获取站点id作为routingkey
        String siteId = cmsPage.getSiteId();
        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, msg);
    }

    private CmsPage saveHtml(String pageId, String pageHtml) {
        //1.查询pageId对应页面是否存在
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        //getPageHtml方法中已经做过此判断
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        CmsPage cmsPage = optional.get();

        //2.判断页面是否已经存储过  有先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(htmlFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }


        //3.保存html文件到gridfs
        InputStream inputStream = null;
        try {
            inputStream = IOUtils.toInputStream(pageHtml, "utf-8");
            ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
            //文件id
            String fileId = objectId.toString();
            //4.重新设置CmsPage的htmlFileId
            //将文件id 存入cmspage中
            cmsPage.setHtmlFileId(fileId);
            cmsPageRepository.save(cmsPage);

            return cmsPage;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 页面静态化
     * 根据页面id生成 对应静态页面
     */
    public String getPageHtml(String pageId) {
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        String dataUrl = null;

        if (optional.isPresent()) {
            dataUrl = optional.get().getDataUrl();
        } else {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        Map model = this.getModelByPageId(dataUrl);

        if (model == null) {
            //获取页面模型数据为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //获取页面模板
        String templateId = optional.get().getTemplateId();
        String templateContent = this.getTemplateByTemplateId(templateId);
        if (StringUtils.isEmpty(templateContent)) {
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //执行静态化
        String html = this.generateHtml(templateContent, model);
        if (StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        return html;
    }

    /**
     * 根据pageId获取页面数据模型
     *
     * @param dataUrl
     * @return
     */
    private Map getModelByPageId(String dataUrl) {
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        /**
         * feign远程请求可以用feign拦截器 把jwt带到下一个微服务
         * restTemplate远程访问  参考AuthService处理
         */
        String jwt = request.getHeader("Authorization");
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("Authorization", jwt);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(null, multiValueMap);

        // 静态化程序远程请求DataUrl获取数据模型。
        ResponseEntity<Map> responseEntity = restTemplate.exchange(dataUrl, HttpMethod.GET, httpEntity, Map.class);
        Map map = responseEntity.getBody();

//        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
//        Map map = forEntity.getBody();
        return map;
    }

    // 静态化程序获取页面的模板信息
    private String getTemplateByTemplateId(String templateId) {
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        String templateFileId = null;

        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            templateFileId = cmsTemplate.getTemplateFileId();
        } else {
            //抛异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        if (StringUtils.isNotEmpty(templateFileId)) {
            //抛异常

            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());

            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);

            String content = null;
            try {
                content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }

        return null;
    }

    // 执行页面静态化
    private String generateHtml(String template, Map model) {
        try {
            //生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template", template);

            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);

            Template template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public QueryResponseResult findList(Integer page, Integer size, QueryPageRequest request) {
        //判断页码和页大小是否>0
        page = (page <= 0 ? 1 : page);
        size = (size <= 0 ? 10 : size);

        CmsPage cmsPage = new CmsPage();
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains());


        //判断条件对象不为空
        if (null == request) {
            request = new QueryPageRequest();
        }

        //设置查询条件
        if (StringUtils.isNotEmpty(request.getPageAliase())) {
            cmsPage.setPageAliase(request.getPageAliase());
        }
        if (StringUtils.isNotEmpty(request.getSiteId())) {
            cmsPage.setSiteId(request.getSiteId());
        }
        if (StringUtils.isNotEmpty(request.getTemplateId())) {
            cmsPage.setTemplateId(request.getTemplateId());
        }
        if (StringUtils.isNotEmpty(request.getPageName())) {
            cmsPage.setPageName(request.getPageName());
        }
        if (StringUtils.isNotEmpty(request.getPageType())) {
            cmsPage.setPageType(request.getPageType());
        }

        Example<CmsPage> example = Example.of(cmsPage, matcher);

        QueryResult<CmsPage> queryResult = new QueryResult<>();
        Page<CmsPage> pageList = cmsPageRepository.findAll(example, PageRequest.of(page - 1, size));

        queryResult.setList(pageList.getContent());
        queryResult.setTotal(pageList.getTotalElements());

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);


        return queryResponseResult;
    }

    public CmsPageResult add(CmsPage cmsPage) {

//        if(null == cmsPage){
//            ExceptionCast.cast(CmsCode.);
//        }

        //先根据网站id/页面名称/页面访问路径共同来判断此页面是否存在
        CmsPage page = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(
                cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());

        //不存在 则添加
        if (null == page) {
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        } else {
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //否则返回添加失败
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public CmsPageResult update(String id, CmsPage cmsPage) {

        CmsPage one = this.findById(id);

        if (null != one) {
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());

            CmsPage save = cmsPageRepository.save(one);

            if (null != save) {
                return new CmsPageResult(CommonCode.SUCCESS, one);
            }
        }

        return new CmsPageResult(CommonCode.FAIL, null);
    }

    public ResponseResult delete(String id) {

        CmsPage one = this.findById(id);

        if (null != one) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }

        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 保存页面  有则更新 没有添加
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面是否存在
        CmsPage page = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(),
                cmsPage.getPageName(), cmsPage.getPageWebPath());

        if (page == null) {
            return this.add(cmsPage);
        }

        return this.update(page.getPageId(), cmsPage);
    }

    /**
     * 一键发布页面
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        CmsPageResult save = this.save(cmsPage);

        if(!save.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }


        //要发布的页面id
        String pageId = save.getCmsPage().getPageId();

        //发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }

        //拼装页面的url
        //页面url=站点域名+站点webpath+页面webpath+页面名称
        String siteId = save.getCmsPage().getSiteId();
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        String siteWebPath = cmsSite.getSiteWebPath();
        String siteDomain = cmsSite.getSiteDomain();

        String pageWebPath = save.getCmsPage().getPageWebPath();
        String pageName = save.getCmsPage().getPageName();

        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;

        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);

    }

    private CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optionalCmsSite = cmsSiteRepository.findById(siteId);
        if(optionalCmsSite.isPresent()){
            return optionalCmsSite.get();
        }
        return null;
    }
}
