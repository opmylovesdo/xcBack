package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/9
 * @Description: com.xuecheng.manage_course.service
 * @version: 1.0
 */
@Service
@Transactional
public class CourseService {
    @Autowired
    private TeachplanRepository teachplanRepository;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CoursePicRepository coursePicRepository;

    @Autowired
    private CourseMarketRepository courseMarketRepository;

    @Autowired
    private CmsPageClient cmsPageClient;

    @Autowired
    private CoursePubRepository coursePubRepository;

    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    /**
     * 判断上级结点 选择了正常添加
     * 没有选择  要根据课程id(url中) 查询该课程的根结点的id
     * 查不到 要自动添加课程的根结点
     *
     * @param teachplan
     * @return
     */
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (null == teachplan || StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        String courseid = teachplan.getCourseid();
        /**
         * 获取教学计划父id 判断是否为空
         * 没有选择  要根据课程id(url中) 查询该课程的根结点的id
         * 查不到 要自动添加课程的根结点
         */

        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            parentid = getTeachplanRoot(courseid);
        }

        //查询父结点 设置现在子节点属性  此处parentId可能为空(已在getTeachplanRoot处理)
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplanParent = optional.get();

        String gradeParent = teachplanParent.getGrade();

        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setParentid(parentid);
        if ("1".equals(gradeParent)) {
            teachplanNew.setGrade("2");
        } else {
            teachplanNew.setGrade("3");
        }
        teachplanNew.setStatus("0");
        Teachplan save = teachplanRepository.save(teachplanNew);
        System.out.println(save);
        ResponseResult responseResult = new ResponseResult(CommonCode.SUCCESS);
        return responseResult;
    }

    private String getTeachplanRoot(String courseid) {
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseid);
        if (!optionalCourseBase.isPresent()) {
//            return null;
            ExceptionCast.cast(CourseCode.COURSE_NOT_EXIST);
        }

        CourseBase courseBase = optionalCourseBase.get();

        List<Teachplan> list = teachplanRepository.findByCourseidAndParentid(courseid, "0");
        if (list == null || list.size() == 0) {
            //查不到 要自动添加课程的根结点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseid);
            teachplan.setGrade("1");
            teachplan.setOrderby(1);
            teachplan.setParentid("0");
            teachplan.setPname(courseBase.getName());
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }

        Teachplan teachplan = list.get(0);
        return teachplan.getId();
    }

    public QueryResponseResult findAll(int page, int size, CourseListRequest courseListRequest) {

        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }

        PageHelper.startPage(page, size);
        Page<CourseInfo> pageBean = courseMapper.findAll(courseListRequest);
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setTotal(pageBean.getTotal());
        queryResult.setList(pageBean.getResult());
        QueryResponseResult<CourseInfo> responseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return responseResult;
    }

    public AddCourseResult addCourse(CourseBase courseBase) {
        courseBase.setStatus("202001");//未发布 制作中
        courseBase = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, courseBase.getId());
    }

    public CourseBase findCourseById(String courseId) {
        if (StringUtils.isNotEmpty(courseId)) {
            Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                ExceptionCast.cast(CourseCode.COURSE_NOT_EXIST);
            }
        }
        return null;
    }

    public ResponseResult updateCourse(String courseId, CourseBase courseBase) {
        CourseBase courseById = this.findCourseById(courseId);
        courseById.setName(courseBase.getName());
        courseById.setUsers(courseBase.getUsers());
        courseById.setMt(courseBase.getMt());
        courseById.setSt(courseBase.getSt());
        courseById.setGrade(courseBase.getGrade());
        courseById.setStudymodel(courseBase.getStudymodel());
        courseById.setTeachmode(courseBase.getTeachmode());
        courseById.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(courseById);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 添加课程图片
     *
     * @param courseId
     * @param pic
     * @return
     */
    public ResponseResult addCoursePic(String courseId, String pic) {
        Optional<CoursePic> picOptional = coursePicRepository.findById(pic);
        CoursePic coursePic = null;
        if (picOptional.isPresent()) {
            coursePic = picOptional.get();
        }
        //没有课程图片则创建对象
        if (null == coursePic) {
            coursePic = new CoursePic();
        }

        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);

        //保存图片
        coursePicRepository.save(coursePic);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程图片
     *
     * @param courseId
     * @return
     */
    public CoursePic finCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * 删除课程图片
     *
     * @param courseId
     * @return
     */
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }

        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据课程id 返回课程页面信息
     *
     * @param courseId
     * @return
     */
    public CourseView getCourseView(String courseId) {
        CourseView courseView = new CourseView();
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
//        if(courseMarketOptional.isPresent()){
//            courseView.setCourseMarket(courseMarketOptional.get());
//        }
        courseMarketOptional.ifPresent(courseView::setCourseMarket);
        //查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        courseBaseOptional.ifPresent(courseView::setCourseBase);

        //查询课程图片信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        coursePicOptional.ifPresent(courseView::setCoursePic);

        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    public CoursePublishResult preview(String courseId) {
        //查询课程
        CourseBase courseById = this.findCourseById(courseId);
        //请求cms添加页面
        //准备cmsPage信息R
        CmsPage cmsPage = prepareCmsPage(courseId, courseById);

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();

        String url = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    /**
     * 课程发布
     *
     * @param courseId
     * @return
     */
    public CoursePublishResult publish(String courseId) {
        //查询课程信息
        CourseBase courseById = this.findCourseById(courseId);

        CmsPage cmsPage = prepareCmsPage(courseId, courseById);
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);

        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //更新课程的信息为已发布
        CourseBase courseBase = this.saveCoursePubState(courseId);
        if (null == courseBase) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //保存课程索引信息
        //...
        //创建coursePub对象
        CoursePub coursePub = createCoursePub(courseId, courseBase);
        //将coursePub保存到数据库
        saveCoursePub(courseId, coursePub);

        //缓存课程的信息
        //...
        //保存课程计划媒资信息到索引表
        this.saveTeachplanMediaPub(courseId);

        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //保存课程计划媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //根据courseId删除teachplanMediaPub表中数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);

    }
    
    //将coursePub保存到数据库
    private CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
        final CoursePub coursePubNew;
        //根据课程id查询CoursePub
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
        if (coursePubOptional.isPresent()) {
            coursePubNew = coursePubOptional.get();
        } else {
            coursePubNew = new CoursePub();
        }

        BeanUtils.copyProperties(coursePub, coursePubNew);
        coursePubNew.setId(courseId);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeString = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(timeString);
        return coursePubRepository.save(coursePubNew);
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String courseId, CourseBase courseBase) {
        CoursePub coursePub = new CoursePub();
        BeanUtils.copyProperties(courseBase, coursePub);

        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(courseId);
        if (marketOptional.isPresent()) {
            BeanUtils.copyProperties(marketOptional.get(), coursePub);
        }

        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        if (coursePicOptional.isPresent()) {
            BeanUtils.copyProperties(coursePicOptional.get(), coursePub);
        }

        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);

        return coursePub;
    }


    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseById = this.findCourseById(courseId);
        courseById.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseById);
        return save;
    }


    private CmsPage prepareCmsPage(String courseId, CourseBase courseById) {
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);//数据模型url
        cmsPage.setPageName(courseId + ".html");//页面名称
        cmsPage.setPageAliase(courseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        return cmsPage;
    }

    /**
     * 保存课程计划和媒资文件的关联
     *
     * @param teachplanMedia
     * @return
     */
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //课程计划id
        String teachplanId = teachplanMedia.getTeachplanId();

        //查询课程计划
        Optional<Teachplan> optionalTeachplan = teachplanRepository.findById(teachplanId);
        if (!optionalTeachplan.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        Teachplan teachplan = optionalTeachplan.get();
        //只允许叶子节点计划选择视频
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !"3".equals(grade)) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        TeachplanMedia one = null;
        Optional<TeachplanMedia> optionalTeachplanMedia = teachplanMediaRepository.findById(teachplanId);
        if (optionalTeachplanMedia.isPresent()) {
            one = optionalTeachplanMedia.get();
        } else {
            one = new TeachplanMedia();
        }

        //保存媒资信息与课程计划关联信息
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
